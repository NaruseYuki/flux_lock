package com.yushin.flux_lock.utils

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

object Utils {
     fun Disposable.addTo(compositeDisposable: CompositeDisposable): Disposable {
        compositeDisposable.add(this)
        return this
    }
}