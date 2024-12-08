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
    private var conCnt = 0 //接続試行回数
    private var toggleCnt = 0 // ロック操作試行回数
    private var registerCnt = 0 // 登録試行回数

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
                dispatcher.dispatch(BLEAction.LoadRegisteredDevices(it.data))
                dispatcher.dispatch(BLEAction.FinishLoading)
            }
            it.onFailure {
                dispatcher.dispatch(BLEAction.FinishLoading)
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
            override fun onBleDeviceStatusChanged(device: CHDevices, status: CHDeviceStatus, shadowStatus: CHDeviceStatus?) {
                dispatcher.dispatch(BLEAction.ChangeBleStatus(status))
                // 2.のタイミングだと失敗するケースがあるため、以下のタイミングで接続するようにする
                if (status == CHDeviceStatus.ReceivedAdV) {
                    if(!isConnected) {
                        device.connect {
                            it.onSuccess {
                                conCnt = 0
                                Log.d("BLE", "Device connected: $it")
                                dispatcher.dispatch(BLEAction.ConnectDevice(device))
                            }
                            it.onFailure {
                                Log.d("BLE", "Device connect failed: $it")
                                // 再試行する
                                if(conCnt < CONNECT_MAX){
                                    conCnt += 1
                                    firstConnectDevice(device)
                                }else{
                                    dispatcher.dispatch(BLEAction.ThrowException(
                                        BaseException.ConnectionException("Device connect failed")))
                                    disconnect(device)
                                    conCnt = 0
                                }
                            }
                        }
                    }
                }
                if (status == CHDeviceStatus.ReadyToRegister) {
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

    private fun configureFirstLockPosition(device: CHDevices) {
        when(device){
                //CHSesame2はデフォルトで角度が設定されていないので、明示的に設定する
            is CHSesame2 -> device.configureLockPosition(
                0, 256){ it ->
                it.onSuccess {
                    Log.d("BLE", "configureLockPosition successes: $it")
                    // 成功したらonMechStatusからパラメータを受け取る
                }
                it.onFailure {
                    Log.d("BLE", "configureLockPosition failed: $it")
                    // TODO 失敗したらエラーを流すようにしたい
                }
            }
            //CHSesame5はデフォルトで角度が設定されている
            is CHSesame5 -> {}
            else -> Log.d("BLE", "not support device")
        }
    }

    // デバイス名を登録するアクションを生成し、ディスパッチ
    private fun registerDevice(device: CHDevices) {
        device.register {
            it.onSuccess {
                registerCnt = 0
                // configureFirstLockPosition(device)
                //  登録成功
                Log.d("BLE", "registerDevice: $it")
                // 再読み込み
                loadRegisteredDevices()
                // ↓ ログ出すだけ
                dispatcher.dispatch(BLEAction.RegisterDevice(device))
            }
            it.onFailure {
                Log.d("BLE", "registerDevice failed: $it")
                //  登録失敗
                if(registerCnt < REGISTER_MAX ){
                    registerCnt += 1
                    registerDevice(device)
                }else{
                    registerCnt = 0
                    dispatcher.dispatch(BLEAction.ThrowException(
                        BaseException.RegistrationException("registerDevice failed")))
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
                                conCnt = 0
                                Log.d("BLE", "Device connected: $it")
                                dispatcher.dispatch(BLEAction.ConnectDevice(device))
                            }
                            it.onFailure {
                                Log.d("BLE", "Device connect failed: $it")
                                // 再試行する
                                if(conCnt < CONNECT_MAX){
                                    conCnt += 1
                                    subscribeDeviceStatus(device)
                                }else{
                                    dispatcher.dispatch(BLEAction.ThrowException(
                                        BaseException.ConnectionException("Device connect failed")))
                                    disconnect(device)
                                    conCnt = 0
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
                    toggleCnt = 0
                    Log.d("BLE", "toggle: $it")
                    dispatcher.dispatch(BLEAction.Toggle(device))
                }
                it.onFailure {
                    Log.d("BLE", "toggle failed: $it")
                    // 再試行する
                    if(toggleCnt < TOGGLE_MAX){
                        toggleCnt += 1
                        toggle(device)
                    }else{
                        dispatcher.dispatch(BLEAction.ThrowException(
                            BaseException.SmartLockOperationException("toggle failed")))
                        disconnect(device)
                        toggleCnt = 0
                    }
                }

            }
            is CHSesame2 -> device.toggle {
                it.onSuccess {
                    toggleCnt = 0
                    Log.d("BLE", "toggle: $it")
                    dispatcher.dispatch(BLEAction.Toggle(device))
                }
                it.onFailure {
                    Log.d("BLE", "toggle failed: $it")
                    // 再試行する
                    if(toggleCnt < TOGGLE_MAX){
                        toggleCnt += 1
                        toggle(device)
                    }else{
                        dispatcher.dispatch(BLEAction.ThrowException(
                            BaseException.SmartLockOperationException("toggle failed")))
                        disconnect(device)
                        toggleCnt = 0
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

    companion object {
        const val CONNECT_MAX = 10 // 接続試行回数 max
        const val TOGGLE_MAX = 10 // TOGGLE試行回数 max
        const val REGISTER_MAX = 10 // 登録試行回数 max
    }
}
