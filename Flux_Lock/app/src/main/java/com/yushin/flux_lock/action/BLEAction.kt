package com.yushin.flux_lock.action

import co.candyhouse.sesame.open.device.CHDeviceStatus
import co.candyhouse.sesame.open.device.CHDevices

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

    // 施錠操作
    // TODO:引数のクラスを決定する
    data object LockDevice:BLEAction()

    // 開錠操作
    // TODO:引数のクラスを決定する
    data object UnlockDevice:BLEAction()

    // デバイスの状態を確認する
    data class CheckDeviceStatus(val device: CHDevices) : BLEAction()

    // デバイスをDBに登録する
    data object RegisterDevice:BLEAction()

    data class ChangeBleStatus(val status: CHDeviceStatus):BLEAction()

    data class DisconnectDevice(val device: CHDevices) : BLEAction()

}