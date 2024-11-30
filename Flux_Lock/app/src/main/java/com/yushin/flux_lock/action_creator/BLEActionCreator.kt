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
import com.yushin.flux_lock.utils.LockState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BLEActionのActionCreatorクラス
 * アクションを生成してDispatcherに渡す役割を持つ
 */
@Singleton
class BLEActionCreator @Inject constructor (private val dispatcher: BLEDispatcher) {

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
                /**
                 TODO:
                 登録時（registerDevice）するタイミングでsharedPreferenceに
                 登録名を保存する
                 */
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
    fun firstConnectDevice(device:CHDevices) {
        device.connect{
            it.onSuccess {
                Log.d("BLE", "device connected: $it")
                dispatcher.dispatch(BLEAction.ConnectDevice(device))
            }
            it.onFailure {
                Log.d("BLE", "device connect failed: $it")
            }
        }
        device.delegate = object : CHDeviceStatusDelegate {
            override fun onBleDeviceStatusChanged(device: CHDevices, status: CHDeviceStatus, shadowStatus: CHDeviceStatus?) {
                if (status == CHDeviceStatus.ReadyToRegister) {
                    registerDevice(device)
                }
                dispatcher.dispatch(BLEAction.ChangeBleStatus(status))
            }
        }

    }

    // デバイス接続アクションを生成し、ディスパッチ
    fun connectDevice(device:CHDevices) {
        subscribeDeviceStatus(device)
    }

    // ユーザー設定を送信するアクションを生成し、ディスパッチ
    // TODO: 実際の設定データを引数として受け取る
    fun configureLockPosition(device: CHDevices,lockState: LockState) {
        if (lockState == LockState.Locked){
            when(device){
                is CHSesame2 -> device.configureLockPosition(device.mechStatus?.position?:0,device.mechSetting?.unlockPosition?:0){ it ->
                    it.onSuccess {
                        Log.d("BLE", "configureLockPosition successes: $it")
                        // 成功したらonMechStatusからパラメータを受け取る
                    }
                    it.onFailure {
                        Log.d("BLE", "configureLockPosition failed: $it")
                        // TODO 失敗したらエラーを流すようにしたい
                    }
                }
                is CHSesame5 -> device.configureLockPosition(device.mechStatus?.position?:0,device.mechSetting?.unlockPosition?:0){
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
                is CHSesame2 -> device.configureLockPosition(device.mechSetting?.lockPosition?:0,device.mechStatus?.position?:0){ it ->
                    it.onSuccess {
                        Log.d("BLE", "configureLockPosition successes: $it")
                        // 成功したらonMechStatusからパラメータを受け取る
                    }
                    it.onFailure {
                        Log.d("BLE", "configureLockPosition failed: $it")
                        // TODO 失敗したらエラーを流すようにしたい
                    }
                }
                is CHSesame5 -> device.configureLockPosition(device.mechSetting?.lockPosition?:0,device.mechStatus?.position?:0){
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

    // 施錠操作のアクションを生成し、ディスパッチ
    // TODO: 必要な引数を決定する
    fun lockDevice() {
        dispatcher.dispatch(BLEAction.LockDevice)
    }

    // 開錠操作のアクションを生成し、ディスパッチ
    // TODO: 必要な引数を決定する
    fun unlockDevice() {
        dispatcher.dispatch(BLEAction.UnlockDevice)
    }

    // デバイスの状態確認アクションを生成し、ディスパッチ
    // TODO: 必要な引数を決定する
    fun disconnect(device:CHDevices) {
        device.disconnect {
            it.onSuccess {
                Log.d("BLE", "device disconnected: $it")
                dispatcher.dispatch(BLEAction.DisconnectDevice(device))
            }
            it.onFailure {
                Log.d("BLE", "device disconnect failed: $it")
                dispatcher.dispatch(BLEAction.DisconnectDevice(device))
            }
        }
    }

    // デバイス名を登録するアクションを生成し、ディスパッチ
    private fun registerDevice(device: CHDevices) {
        device.register {
            it.onSuccess {
                //  登録成功
                Log.d("BLE", "registerDevice: $it")
                dispatcher.dispatch(BLEAction.RegisterDevice)
            }
            it.onFailure {
                //  登録失敗
                // TODO 失敗したらエラーを流すようにしたい
            }
        }
    }

    private fun subscribeDeviceStatus(device: CHDevices) {
        device.delegate = object : CHDeviceStatusDelegate {
            override fun onBleDeviceStatusChanged(device: CHDevices, status: CHDeviceStatus, shadowStatus: CHDeviceStatus?) {
                if(status == CHDeviceStatus.ReceivedAdV){
                    device.connect{
                        it.onSuccess {
                            Log.d("BLE", "device connected: $it")
                            dispatcher.dispatch(BLEAction.ConnectDevice(device))
                        }
                        it.onFailure {
                            Log.d("BLE", "device connect failed: $it")
                            // TODO 失敗したらエラーを流すようにしたい
                        }
                    }
                }
                dispatcher.dispatch(BLEAction.ChangeBleStatus(status))
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
                    Log.d("BLE", "toggle: $it")
                    dispatcher.dispatch(BLEAction.Toggle(device))
                }
                it.onFailure {
                    Log.d("BLE", "toggle failed: $it")
                    // TODO 失敗したらエラーを流すようにしたい
                }
            }
            is CHSesame2 -> device.toggle {
                it.onSuccess {
                    Log.d("BLE", "toggle: $it")
                    dispatcher.dispatch(BLEAction.Toggle(device))
                }
                it.onFailure {
                    Log.d("BLE", "toggle failed: $it")
                    // TODO 失敗したらエラーを流すようにしたい
                }
            }
        }
    }
}
