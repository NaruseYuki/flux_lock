package com.yushin.flux_lock.exception

/**
 基底のエラークラス
 */

sealed class BaseException(override val message: String, val errorCode: Int) : Exception(message) {
    // 接続エラー
    class ConnectionException(message: String) : BaseException(message, 1001)

    // 登録失敗エラー
    class RegistrationException(message: String) : BaseException(message, 1002)

    // スマートロック操作エラー
    class SmartLockOperationException(message: String) : BaseException(message, 1003)

    /**
     * これより下は今のところ使わないが、今後拡張する際に使用する
     */

    // タイムアウトエラー
    class TimeoutException(message: String) : BaseException(message, 1004)

    // 認証エラー
    class AuthenticationException(message: String) : BaseException(message, 1005)

    // データ整合性エラー
    class DataIntegrityException(message: String) : BaseException(message, 1006)

    // 未知のエラー
    class UnknownException(message: String) : BaseException(message, 9999)
}
