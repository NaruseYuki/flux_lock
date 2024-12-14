package com.yushin.flux_lock.action_creator

import android.util.Log
import co.candyhouse.sesame.open.CHBleManager
import co.candyhouse.sesame.open.CHBleManagerDelegate
import co.candyhouse.sesame.open.CHDeviceManager
import co.candyhouse.sesame.open.device.CHDeviceStatus
import co.candyhouse.sesame.open.device.CHDeviceStatusDelegate
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import com.yushin.flux_lock.action.BLEAction
import com.yushin.flux_lock.dispatcher.BLEDispatcher
import com.yushin.flux_lock.exception.BaseException
import com.yushin.flux_lock.utils.LockState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BLEActionのActionCreatorクラス
 * アクションを生成してDispatcherに渡す役割を持つ
 */
@Singleton
class BLEActionCreator @Inject constructor (private val dispatcher: BLEDispatcher) {

    private var currentDevice: CHDevices? = null // 現在接続中のデバイスを追跡
    private var attemptCnt = 0 //試行回数

    // 未登録デバイスの取得アクションを生成し、ディスパッチ
    fun loadUnregisteredDevices() {
        CHBleManager.delegate = object : CHBleManagerDelegate {
            override fun didDiscoverUnRegisteredCHDevices(devices: List<CHDevices>) {
                dispatcher.dispatch(BLEAction.LoadUnregisteredDevices(devices.filter { it.rssi != null }))
            }
        }
    }

    // 登録済デバイスの取得アクションを生成し、ディスパッチ
    fun loadRegisteredDevices() {
        dispatcher.dispatch(BLEAction.StartLoading)
        CHDeviceManager.getCandyDevices { it ->
            it.onSuccess {
                Log.d("BLE", "loadRegisteredDevices : $it")
                attemptCnt = 0
                dispatcher.dispatch(BLEAction.FinishLoading)
                dispatcher.dispatch(BLEAction.LoadRegisteredDevices(it.data))
            }
            it.onFailure {
                dispatcher.dispatch(BLEAction.FinishLoading)
                Log.d("BLE", "loadRegisteredDevices failed: $it")
                // 再試行する
                if(attemptCnt < LOAD_MAX){
                    attemptCnt += 1
                    loadRegisteredDevices()
                }else{
                    Log.d("BLE", "loadRegisteredDevices failed2: $it")
                    dispatcher.dispatch(BLEAction.ThrowException(
                        BaseException.RegistrationException))
                    currentDevice?.let { it1 -> resetLock(it1) }
                    attemptCnt = 0
                }
            }
        }
    }

    // デバイススキャンアクションを生成し、ディスパッチ
    fun scanDevices() {
        CHBleManager.enableScan {
            dispatcher.dispatch(BLEAction.ScanDevices)
        }
    }

    // デバイススキャン停止アクションを生成し、ディスパッチ
    fun stopScanDevices() {
        CHBleManager.disableScan {
            dispatcher.dispatch(BLEAction.StopScanDevices)
        }
    }

    // デバイス接続アクションを生成し、ディスパッチ
    fun firstConnectDevice(device: CHDevices) {
        // 1. 以前のデバイスの接続を解除
        currentDevice?.let { previousDevice ->
            previousDevice.delegate = null // 古いデバイスのデリゲートを解除
            disconnect(previousDevice)
        }

        // 2. 現在のデバイスを更新
        currentDevice = device

        // 明示的に接続する
        var isConnected = false
        currentDevice?.connect {
            dispatcher.dispatch(BLEAction.ChangeBleStatus(currentDevice?.deviceStatus?:return@connect))
            it.onSuccess {
                Log.d("BLE", "Device connected: $it")
                dispatcher.dispatch(BLEAction.ConnectDevice(currentDevice?:return@onSuccess))
                isConnected = true
            }
            it.onFailure {
                Log.d("BLE", "Device connect failed: $it")
                isConnected = false
                // ここで失敗してもなにもしない
            }
        }

        // 4. デリゲートを設定
        currentDevice?.delegate = object : CHDeviceStatusDelegate {
            override fun onMechStatus(device: CHDevices) {
                super.onMechStatus(device)
                dispatcher.dispatch(BLEAction.CheckDeviceStatus(device))
            }

            override fun onBleDeviceStatusChanged(device: CHDevices, status: CHDeviceStatus, shadowStatus: CHDeviceStatus?) {
                dispatcher.dispatch(BLEAction.ChangeBleStatus(status))
                // 2.のタイミングだと失敗するケースがあるため、以下のタイミングで接続するようにする
                if (status == CHDeviceStatus.ReceivedAdV) {
                    if(!isConnected) {
                        device.connect {
                            it.onSuccess {
                                attemptCnt = 0
                                Log.d("BLE", "Device connected: $it")
                                dispatcher.dispatch(BLEAction.ConnectDevice(device))
                            }
                            it.onFailure {
                                Log.d("BLE", "Device connect failed: $it")
                                // 再試行する
                                if(attemptCnt < CONNECT_MAX){
                                    attemptCnt += 1
                                    firstConnectDevice(device)
                                }else{
                                    dispatcher.dispatch(BLEAction.ThrowException(
                                        BaseException.ConnectionException))
                                    attemptCnt = 0
                                }
                            }
                        }
                    }
                }
                if (status == CHDeviceStatus.ReadyToRegister) {
                    Log.d("BLE", "Device ReadyToRegister")
                    registerDevice(device)
                }
            }
        }
    }


    // デバイス接続アクションを生成し、ディスパッチ
    fun connectDevice(device:CHDevices) {
        subscribeDeviceStatus(device)
    }

    // ユーザー設定を送信するアクションを生成し、ディスパッチ
    fun configureLockPosition(device: CHDevices,lockState: LockState) {
        if (lockState == LockState.Locked){
            when(device){
                is CHSesame2 -> device.configureLockPosition(
                    device.mechStatus?.position?:0,
                    device.mechSetting?.unlockPosition?:0){ it ->
                    it.onSuccess {
                        Log.d("BLE", "configureLockPosition successes: $it")
                        // 成功したらonMechStatusからパラメータを受け取る
                    }
                    it.onFailure {
                        Log.d("BLE", "configureLockPosition failed: $it")
                        // TODO 失敗したらエラーを流すようにしたい
                    }
                }
                is CHSesame5 -> device.configureLockPosition(
                    device.mechStatus?.position?:0,
                    device.mechSetting?.unlockPosition?:0){
                    it.onSuccess {
                        Log.d("BLE", "configureLockPosition successes: $it")
                        // 成功したらonMechStatusからパラメータを受け取る
                    }
                    it.onFailure {
                        Log.d("BLE", "configureLockPosition failed: $it")
                        // TODO 失敗したらエラーを流すようにしたい
                    }
                }
                else -> Log.d("BLE", "not support device")
            }
        } else {
            when(device){
                is CHSesame2 -> device.configureLockPosition(
                    device.mechSetting?.lockPosition?:0,
                    device.mechStatus?.position?:0){ it ->
                    it.onSuccess {
                        Log.d("BLE", "configureLockPosition successes: $it")
                        // 成功したらonMechStatusからパラメータを受け取る
                    }
                    it.onFailure {
                        Log.d("BLE", "configureLockPosition failed: $it")
                        // TODO 失敗したらエラーを流すようにしたい
                    }
                }
                is CHSesame5 -> device.configureLockPosition(
                    device.mechSetting?.lockPosition?:0,
                    device.mechStatus?.position?:0){
                    it.onSuccess {
                        Log.d("BLE", "configureLockPosition successes: $it")
                        // 成功したらonMechStatusからパラメータを受け取る
                    }
                    it.onFailure {
                        Log.d("BLE", "configureLockPosition failed: $it")
                        // TODO 失敗したらエラーを流すようにしたい
                    }
                }
                else -> Log.d("BLE", "not support device")
            }

        }
    }

    // デバイスの状態確認アクションを生成し、ディスパッチ
    fun disconnect(device:CHDevices) {
        device.disconnect {
            it.onSuccess {
                Log.d("BLE", "device disconnected: $it")
                dispatcher.dispatch(BLEAction.ChangeBleStatus(CHDeviceStatus.NoBleSignal))
                dispatcher.dispatch(BLEAction.DisconnectDevice(device))
            }
            it.onFailure {
                Log.d("BLE", "device disconnect failed: $it")
                dispatcher.dispatch(BLEAction.ChangeBleStatus(CHDeviceStatus.NoBleSignal))
                dispatcher.dispatch(BLEAction.DisconnectDevice(device))
            }
        }
    }

    // デバイス名を登録するアクションを生成し、ディスパッチ
     fun registerDevice(device: CHDevices) {
        when(device){
            is CHSesame2 ->
                registerSesame2(device)

            is CHSesame5 ->
                registerSesame5(device)
        }
    }

    private fun registerSesame5(device: CHDevices) {
        device.register {
            it.onSuccess {
                // ↓ ログ出すだけ
                dispatcher.dispatch(BLEAction.RegisterDevice(device))
                attemptCnt = 0
                //  登録成功
                Log.d("BLE", "registerDevice: $it")
                // 再読み込み
                loadRegisteredDevices()
            }
            it.onFailure {
                Log.d("BLE", "registerDevice failed: $it")
                //  登録失敗
                if (attemptCnt < REGISTER_MAX) {
                    attemptCnt += 1
                    Thread.sleep(100) //Busyになるため
                    registerDevice(device)
                } else {
                    attemptCnt = 0
                    // 接続済みなので初期化する
                    resetLock(device)
                    dispatcher.dispatch(
                        BLEAction.ThrowException(
                            BaseException.RegistrationException
                        )
                    )
                }
            }
        }
    }

    private fun registerSesame2(device: CHDevices) {
        device.register {
            it.onSuccess {
                // ↓ ログ出すだけ
                dispatcher.dispatch(BLEAction.RegisterDevice(device))
                attemptCnt = 0
                //  登録成功
                Log.d("BLE", "registerDevice: $it")
                // 再読み込み
                loadRegisteredDevices()

                // SESAME2のみ初期角度登録する
                startFirstSetting(device)
            }
            it.onFailure {
                Log.d("BLE", "registerDevice failed: $it")
                //  登録失敗
                if (attemptCnt < REGISTER_MAX) {
                    attemptCnt += 1
                    Thread.sleep(100) //Busyになるため
                    registerDevice(device)
                } else {
                    attemptCnt = 0
                    // 接続済みなので初期化する
                    resetLock(device)
                    dispatcher.dispatch(
                        BLEAction.ThrowException(
                            BaseException.RegistrationException
                        )
                    )
                }
            }
        }
    }

    private fun subscribeDeviceStatus(device: CHDevices) {
        // 1. 前のデバイスを破棄
        currentDevice?.let { previousDevice ->
            previousDevice.delegate = null // デリゲートを解除
            disconnect(previousDevice)
        }

        // 2. 新しいデバイスを追跡
        currentDevice = device

        // 明示的に接続する
        var isConnected = false
        currentDevice?.connect {
            dispatcher.dispatch(BLEAction.ChangeBleStatus(currentDevice?.deviceStatus?:return@connect))
            it.onSuccess {
                isConnected = true
                Log.d("BLE", "Device connected: $it")
                dispatcher.dispatch(BLEAction.ConnectDevice(currentDevice?:return@onSuccess))

                // SESAME2のみ設定してなければ初期角度登録する
                startFirstSetting(device)
            }
            it.onFailure {
                isConnected = false
                Log.d("BLE", "Device connect failed: $it")
                // ここでは失敗してもエラーを流さない
            }
        }

        // 3. 新しいデバイスにデリゲートを設定
        currentDevice?.delegate = object : CHDeviceStatusDelegate {
            override fun onBleDeviceStatusChanged(device: CHDevices, status: CHDeviceStatus, shadowStatus: CHDeviceStatus?) {
                dispatcher.dispatch(BLEAction.ChangeBleStatus(status))
                // 2.のタイミングだと失敗するケースがあるため、以下のタイミングで接続するようにする
                if (status == CHDeviceStatus.ReceivedAdV) {
                    if(!isConnected){
                        device.connect {
                            it.onSuccess {
                                attemptCnt = 0
                                Log.d("BLE", "Device connected: $it")
                                dispatcher.dispatch(BLEAction.ConnectDevice(device))
                                // SESAME2のみ設定してなければ初期角度登録する
                                startFirstSetting(device)
                            }
                            it.onFailure {
                                Log.d("BLE", "Device connect failed: $it")
                                // 再試行する
                                if(attemptCnt < CONNECT_MAX){
                                    attemptCnt += 1
                                    subscribeDeviceStatus(device)
                                }else{
                                    dispatcher.dispatch(BLEAction.ThrowException(
                                        BaseException.ConnectionException))
                                    attemptCnt = 0
                                }
                            }
                        }
                    }
                }
            }

            override fun onMechStatus(device: CHDevices) {
                super.onMechStatus(device)
                dispatcher.dispatch(BLEAction.CheckDeviceStatus(device))
            }
        }
    }

     fun toggle(device: CHDevices){
        when(device){
            is CHSesame5 -> device.toggle {
                it.onSuccess {
                    attemptCnt = 0
                    Log.d("BLE", "toggle: $it")
                    dispatcher.dispatch(BLEAction.Toggle(device))
                }
                it.onFailure {
                    Log.d("BLE", "toggle failed: $it")
                    // 再試行する
                    if(attemptCnt < TOGGLE_MAX){
                        attemptCnt += 1
                        toggle(device)
                    }else{
                        dispatcher.dispatch(BLEAction.ThrowException(
                            BaseException.SmartLockOperationException))
                        disconnect(device)
                        attemptCnt = 0
                    }
                }

            }
            is CHSesame2 -> device.toggle {
                it.onSuccess {
                    attemptCnt = 0
                    Log.d("BLE", "toggle: $it")
                    dispatcher.dispatch(BLEAction.Toggle(device))
                }
                it.onFailure {
                    Log.d("BLE", "toggle failed: $it")
                    // 再試行する
                    if(attemptCnt < TOGGLE_MAX){
                        attemptCnt += 1
                        toggle(device)
                    }else{
                        dispatcher.dispatch(BLEAction.ThrowException(
                            BaseException.SmartLockOperationException))
                        disconnect(device)
                        attemptCnt = 0
                    }
                }
            }
        }
    }

    fun resetLock(device: CHDevices){
        currentDevice?.reset {
            it.onSuccess {
                dispatcher.dispatch(BLEAction.Reset(true))
            }
            it.onFailure {
                dispatcher.dispatch(BLEAction.Reset(false))
            }
            dropKey(device)
        }
    }

    /**
     * SesameSDKの内部データベースに保存されてるこのセサミデバイスの鍵を破棄する
     * 再度何処かから同じ鍵を取ってこれば再度使える状態になる
     */
    private fun dropKey(device: CHDevices){
        device.dropKey {
            it.onSuccess {
                Log.d("BLE", "dropKey: $it")
            }
            it.onFailure {
                Log.d("BLE", "dropKey failed: $it")
            }
        }
        dispatcher.dispatch(BLEAction.DropKey(device))
        loadRegisteredDevices()
    }

    /**
     * 設定してなければ初期設定を行う
     * CHSESAME2のみ。
     */
    private fun startFirstSetting(device: CHDevices){
        when(device){
            is CHSesame2 ->
                if(device.mechSetting?.isConfigured == false){
                    device.configureLockPosition(0,256){
                        it.onSuccess {
                            Log.d("BLE", "configureLockPosition successes: $it")
                            // 成功したらonMechStatusからパラメータを受け取る
                        }
                        it.onFailure {
                            Log.d("BLE", "configureLockPosition failed: $it")
                            // TODO 失敗したらエラーを流すようにしたい
                        }
                    }
                }
            else -> return
        }
    }

     fun getVersionTag(device: CHDevices){
        device.getVersionTag {
            it.onSuccess {
                tag ->
                Log.d("BLE", "getVersionTag: $tag")
                dispatcher.dispatch(BLEAction.GetVersionTag(tag))
            }
            it.onFailure {
                tag ->
                Log.d("BLE", "getVersionTag failed: $tag")
                // 失敗しても何もしない
            }
        }
    }
    /**
     * NWまたはBLEの例外エラーを投げる
     */
     fun handleNetworkAndBluetoothState(){
         dispatcher.dispatch(BLEAction.ThrowException(BaseException.NetworkBLEErrorException))
     }

    companion object {
        const val CONNECT_MAX = 10 // 接続試行回数 max
        const val TOGGLE_MAX = 10 // TOGGLE試行回数 max
        const val REGISTER_MAX = 10 // 登録試行回数 max
        const val LOAD_MAX = 3
    }
}
