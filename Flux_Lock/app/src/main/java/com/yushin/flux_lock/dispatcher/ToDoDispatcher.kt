package com.yushin.flux_lock.dispatcher

import com.yushin.flux_lock.action.ToDoAction
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * ActionをStoreに配信するDispatcherクラス（シングルトン）
 */
object ToDoDispatcher {
    private val actionProcessor = PublishSubject.create<ToDoAction>()

    /**
     * actionを送出する
     */
    fun dispatch(action: ToDoAction){
        actionProcessor.onNext(action)
    }

    /**
     * actionProcessorを呼び出す
     */
    fun onAction(): PublishSubject<ToDoAction> = actionProcessor
}