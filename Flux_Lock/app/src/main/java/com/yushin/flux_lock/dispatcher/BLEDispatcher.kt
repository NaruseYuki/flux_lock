package com.yushin.flux_lock.dispatcher
import com.yushin.flux_lock.action.BLEAction
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ActionをStoreに配信するDispatcherクラス（シングルトン）
 */
@Singleton
class BLEDispatcher @Inject constructor() {
    private val actionProcessor = PublishSubject.create<BLEAction>()

    /**
     * actionを送出する
     */
    fun dispatch(action: BLEAction){
        actionProcessor.onNext(action)
    }

    /**
     * actionProcessorを呼び出す
     */
    fun onAction(): PublishSubject<BLEAction> = actionProcessor
}