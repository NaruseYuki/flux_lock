package com.yushin.flux_lock.store

import android.bluetooth.BluetoothDevice
import android.util.Log
import co.candyhouse.sesame.open.CHResult
import co.candyhouse.sesame.open.device.CHDeviceStatus
import co.candyhouse.sesame.open.device.CHDeviceStatusDelegate
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHProductModel
import co.candyhouse.sesame.open.device.CHSesameProtocolMechStatus
import co.candyhouse.sesame.server.dto.CHEmpty
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import com.yushin.flux_lock.action.BLEAction
import com.yushin.flux_lock.dispatcher.BLEDispatcher
import com.yushin.flux_lock.utils.Utils.addTo
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BLEStore @Inject constructor(
    private val bleDispatcher:BLEDispatcher
) {
    // 未登録デバイス
    private val unRegisteredDevices = mutableListOf<CHDevices>()
    private val unRegisteredSubject = BehaviorRelay.createDefault(unRegisteredDevices)

    // 登録デバイス
    private val registeredDevices = mutableListOf<CHDevices>()
    private val registeredSubject = BehaviorRelay.createDefault(registeredDevices)

    // 接続デバイス
    private var connectedSubject = BehaviorRelay.create<CHDevices?>()
    private var connectionComplete = PublishRelay.create<Unit>()

    // デバイスステータス
    var bleStatusSubject = BehaviorRelay.create<CHDeviceStatus>()

    // ローディング中フラグ
    val loadingSubject = PublishSubject.create<Boolean>()

    private var disposables:CompositeDisposable = CompositeDisposable()

    init {
        bleDispatcher.onAction()
            .subscribe {
                action -> on(action)
            }.addTo(disposables)
    }

    private fun on(action: BLEAction) {
        when (action) {
            is BLEAction.StartLoading -> startLoading()
            is BLEAction.FinishLoading -> finishLoading()
            is BLEAction.LoadUnregisteredDevices -> loadUnregisteredDevices(action.devices)
            is BLEAction.LoadRegisteredDevices -> loadRegisteredDevices(action.devices)
            is BLEAction.ScanDevices -> scanDevices()
            is BLEAction.StopScanDevices -> stopScanDevices()
            is BLEAction.ConnectDevice -> connectDevice(action.device)
            is BLEAction.LockDevice -> lockDevice()
            is BLEAction.UnlockDevice -> unlockDevice()
            is BLEAction.CheckDeviceStatus -> checkDeviceStatus(action.device)
            is BLEAction.RegisterDevice -> registerDevice(action.device)
            is BLEAction.ChangeBleStatus -> changeBleStatus(action.status)
            is BLEAction.Toggle -> toggle(action.device)
            is BLEAction.DisconnectDevice ->disconnectDevice(action.device)
        }
    }

    private fun disconnectDevice(device: CHDevices) {
        // nullを入れると例外エラーが投げられるので、切断時はdummyを入れておく
        val dummy:CHDevices = DummyDevice()
        Log.d("BLE", "disconnect: $device")
    }

    private fun toggle(device: CHDevices) {
        Log.d("BLE", "toggle: $device")
    }

    private fun loadRegisteredDevices(devices:List<CHDevices>) {
        registeredDevices.clear()
        registeredDevices.addAll(devices)
        registeredSubject.accept(registeredDevices)
    }

    private fun loadUnregisteredDevices(devices:List<CHDevices>) {
        unRegisteredDevices.clear()
        unRegisteredDevices.addAll(devices)
        unRegisteredSubject.accept(unRegisteredDevices)
    }

    private fun registerDevice(device: CHDevices) {
        //登録成功したデバイスをストアに反映する
        registeredDevices.add(device)
        registeredSubject.accept(registeredDevices)
        Log.d("BLE", "registerDevice: $registeredDevices")

    }

    private fun checkDeviceStatus(device: CHDevices) {
        connectedSubject.accept(device)
        Log.d("BLE", "@@@_$connectedSubject")
    }

    private fun unlockDevice() {
        TODO("Not yet implemented")
    }

    private fun lockDevice() {
        TODO("Not yet implemented")
    }

    private fun sendUserConfig() {
        TODO("Not yet implemented")
    }

    private fun connectDevice(device: CHDevices) {
        connectedSubject.accept(device)
        connectionComplete.accept(Unit)
        Log.d("BLE", "@@@_$connectedSubject")

    }

    private fun scanDevices() {
        Log.d("BLE", "finished scanDevices")
    }

    private fun stopScanDevices() {
    Log.d("BLE", "finished stopScanDevices")
    }

     fun onDestroy(){
        disposables.dispose()
    }
    private fun startLoading() {
        loadingSubject.onNext(true)
    }

    private fun finishLoading() {
        loadingSubject.onNext(false)
    }

    private fun changeBleStatus(status: CHDeviceStatus) {
        bleStatusSubject.accept(status)
    }

    // 未登録デバイスのリストを取得する
    fun getUnregisteredDevices(): BehaviorRelay<MutableList<CHDevices>> = unRegisteredSubject

    // 登録デバイスのリストを取得する
    fun getRegisteredDevices(): BehaviorRelay<MutableList<CHDevices>> = registeredSubject

    // 接続デバイスを取得する
    fun getConnectedDevice() = connectedSubject

    fun getConnectionComplete() = connectionComplete

}

// ダミーデバイスのクラス定義
class DummyDevice : CHDevices {
    override var mechStatus: CHSesameProtocolMechStatus? = null
    override var deviceTimestamp: Long? = null
    override var loginTimestamp: Long? = null
    override var delegate: CHDeviceStatusDelegate? = null
    override var deviceStatus: CHDeviceStatus = CHDeviceStatus.NoBleSignal // 初期ステータス
    override var deviceShadowStatus: CHDeviceStatus? = null
    override var rssi: Int? = -100 // デフォルトの低いRSSI値
    override var deviceId: UUID? = UUID.fromString("00000000-0000-0000-0000-000000000000") // ダミーID
    override var isRegistered: Boolean = false
    override var productModel: CHProductModel = CHProductModel.SS5 // 仮モデル

    // メソッドをダミー実装
    override fun connect(result: CHResult<CHEmpty>) {
    }

    override fun disconnect(result: CHResult<CHEmpty>) {
    }

    override fun dropKey(result: CHResult<CHEmpty>) {
    }

    override fun getVersionTag(result: CHResult<String>) {
    }

    override fun register(result: CHResult<CHEmpty>) {
    }

    override fun reset(result: CHResult<CHEmpty>) {
    }

    override fun updateFirmware(onResponse: CHResult<BluetoothDevice>) {
    }
}