package com.yushin.flux_lock.store

import android.bluetooth.BluetoothDevice
import android.util.Log
import co.candyhouse.sesame.open.CHResult
import co.candyhouse.sesame.open.CHResultState
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
import com.yushin.flux_lock.exception.BaseException
import com.yushin.flux_lock.utils.Utils.addTo
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.core.Observable
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
    private val registerCompleteSubject = BehaviorRelay.create<Unit>()

    // 接続デバイス
    private val connectedSubject = BehaviorRelay.create<CHDevices?>()
    private val connectionComplete = PublishRelay.create<Unit>()

    // 初期化処理結果
    private var deviceInitResetResult = PublishRelay.create<Boolean>()
    private var deviceInitDropKeyResult = PublishRelay.create<Unit>()

    // エラー通知
    private val errorSubject = PublishRelay.create<BaseException>()

    // デバイスステータス
    private var bleStatusSubject = BehaviorRelay.create<CHDeviceStatus>()

    // ローディング中フラグ
    private val loadingSubject = PublishSubject.create<Boolean>()

    // バージョンタグ
    private val versionTagSubject = BehaviorRelay.create<CHResultState<String>>()

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
            is BLEAction.CheckDeviceStatus -> checkDeviceStatus(action.device)
            is BLEAction.RegisterDevice -> registerDevice(action.device)
            is BLEAction.ChangeBleStatus -> changeBleStatus(action.status)
            is BLEAction.Toggle -> toggle(action.device)
            is BLEAction.DisconnectDevice -> disconnectDevice(action.device)
            is BLEAction.Reset -> reset(action.result)
            is BLEAction.DropKey -> dropKey(action.device)
            is BLEAction.GetVersionTag -> getVersionTag(action.status)
            is BLEAction.ThrowException -> throwError(action.exception)
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
        registerCompleteSubject.accept(Unit)
        Log.d("BLE", "registerDevice: $device")
    }

    private fun checkDeviceStatus(device: CHDevices) {
        connectedSubject.accept(device)
        Log.d("BLE", "@@@_$connectedSubject")
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

    private fun reset(result: Boolean) {
        if(result){
            Log.d("BLE", "セサミのリセットに成功した")

        }else{
            Log.d("BLE", "セサミのリセットに失敗した")
        }
        deviceInitResetResult.accept(result)
    }

    private fun dropKey(device: CHDevices) {
        //登録成功したデバイスをストアに反映する
        registeredDevices.remove(device)
        registeredSubject.accept(registeredDevices)
        connectedSubject.accept(DummyDevice())
        deviceInitDropKeyResult.accept(Unit)
    }

    private fun getVersionTag(status: CHResultState<String>) {
        versionTagSubject.accept(status)
    }

    private fun throwError(error:BaseException){
        Log.d("BLE", "throwError: ${error.message}")
        errorSubject.accept(error)
    }

    // 未登録デバイスのリストを取得する
    fun getUnregisteredDevices(): BehaviorRelay<MutableList<CHDevices>> = unRegisteredSubject

    // 登録デバイスのリストを取得する
    fun getRegisteredDevices(): BehaviorRelay<MutableList<CHDevices>> = registeredSubject

    // 接続デバイスを取得する
    fun getConnectedDevice() = connectedSubject

    // 接続が完了したことを通知する
    fun getConnectionComplete() = connectionComplete

    fun getError() = errorSubject

    // デバイスの初期化が完了した
    fun getDeviceInitResult() = Observable.zip(
        deviceInitResetResult,
        deviceInitDropKeyResult
    ) { resetResult, _ ->
        resetResult // dropKeyResult を無視
    }

    fun getRegisterCompleteSubject() = registerCompleteSubject

    fun getBleStatus() = bleStatusSubject

    fun getVersionTag() = versionTagSubject
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