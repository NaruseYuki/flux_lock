package com.yushin.flux_lock.exception

/**
 基底のエラークラス
 */

sealed class BaseException(override val message: String, val errorCode: Int) : Exception(message) {
    // 接続エラー
    data object ConnectionException : BaseException(CONNECTION_EXCEPTION_TEXT, CONNECTION_ERROR_CODE)

    // 登録失敗エラー
    data object RegistrationException : BaseException(REGISTRATION_EXCEPTION_TEXT, REGISTRATION_ERROR_CODE)

    // スマートロック操作エラー
    data object SmartLockOperationException : BaseException(SMART_LOCK_OPERATION_EXCEPTION_TEXT, SMART_LOCK_OPERATION_ERROR_CODE)

    data object FirmwareVersionUpException : BaseException(FIRMWARE_VERSION_UP_EXCEPTION_TEXT, FIRMWARE_VERSION_UP_ERROR_CODE)

    /**
     * これより下は今のところ使わないが、今後拡張する際に使用する
     */

    // タイムアウトエラー
    data object TimeoutException : BaseException(TIME_OUT_EXCEPTION_TEXT, TIME_OUT_ERROR_CODE)

    // 認証エラー
    data object AuthenticationException : BaseException(AUTHENTICATION_EXCEPTION_TEXT, AUTHENTICATION_ERROR_CODE)

    // データ整合性エラー
    data object DataIntegrityException : BaseException(DATA_INTEGRITY_EXCEPTION_TEXT, DATA_INTEGRITY_ERROR_CODE)

    // 未知のエラー
    data object UnknownException : BaseException(UNKNOWN_EXCEPTION_TEXT, UNKNOWN_ERROR_CODE)

    // ネットワーク/BLE
    data object NetworkBLEErrorException : BaseException(NETWORK_ERROR_EXCEPTION_TEXT, NETWORK_ERROR_ERROR_CODE)

    companion object{
        /**
         * エラーメッセージ
         */
        const val CONNECTION_EXCEPTION_TEXT = "接続に失敗しました。\nしばらく待ってから、再度お試しください"
        const val REGISTRATION_EXCEPTION_TEXT = "登録に失敗しました。\nしばらく待ってから、再度お試しください"
        const val SMART_LOCK_OPERATION_EXCEPTION_TEXT = "ロックの操作に失敗しました。\nしばらく待ってから、再度お試しください"
        const val TIME_OUT_EXCEPTION_TEXT = "タイムアウトエラー"
        const val AUTHENTICATION_EXCEPTION_TEXT = "認証エラー"
        const val DATA_INTEGRITY_EXCEPTION_TEXT = "データ整合性エラー"
        const val UNKNOWN_EXCEPTION_TEXT = "不明なエラー"
        const val NETWORK_ERROR_EXCEPTION_TEXT = "ネットワークまたはBLEエラーが発生しました。インターネット/BLEに接続されているかを確認してください。"
        const val FIRMWARE_VERSION_UP_EXCEPTION_TEXT = "バージョンアップエラー"

        /**
         * エラーコード
         */
        const val CONNECTION_ERROR_CODE = 1001
        const val REGISTRATION_ERROR_CODE = 1002
        const val SMART_LOCK_OPERATION_ERROR_CODE = 1003
        const val TIME_OUT_ERROR_CODE = 1004
        const val AUTHENTICATION_ERROR_CODE = 1005
        const val DATA_INTEGRITY_ERROR_CODE = 1006
        const val NETWORK_ERROR_ERROR_CODE = 1007
        const val FIRMWARE_VERSION_UP_ERROR_CODE = 1008
        const val UNKNOWN_ERROR_CODE = 9999

    }
}
