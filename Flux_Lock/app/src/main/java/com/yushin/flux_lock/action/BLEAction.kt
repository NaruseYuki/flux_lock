package com.yushin.flux_lock.action

import co.candyhouse.sesame.open.device.CHDeviceStatus
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.exception.BaseException

/**
 * アプリ内のアクションを定義するクラス
 * BLEに関わるアクションを定義する
 */

sealed class BLEAction {

    // ローディング開始
    data object StartLoading:BLEAction()

    // ローディング終了
    data object FinishLoading:BLEAction()

    // トグル操作
    data class Toggle(val device: CHDevices) : BLEAction()

    // 未登録デバイスの取得
    data class LoadUnregisteredDevices(val devices: List<CHDevices>) : BLEAction()

    // 登録済デバイスの取得
    data class LoadRegisteredDevices(val devices: List<CHDevices>) : BLEAction()

    // デバイススキャン
    data object ScanDevices:BLEAction()

    // デバイススキャンの停止
    data object StopScanDevices:BLEAction()

    // 接続実行
    data class ConnectDevice(val device: CHDevices) : BLEAction()

    // デバイスの状態を確認する
    data class CheckDeviceStatus(val device: CHDevices) : BLEAction()

    data class RegisterDevice(val device: CHDevices):BLEAction()

    data class ChangeBleStatus(val status: CHDeviceStatus):BLEAction()

    data class DisconnectDevice(val device: CHDevices) : BLEAction()

    data class Reset(val result: Boolean) : BLEAction()

    data class DropKey(val device: CHDevices) : BLEAction()

    data class ThrowException(val exception: BaseException) : BLEAction()


}