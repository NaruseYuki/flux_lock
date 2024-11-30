package com.yushin.flux_lock.view.ble

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.yushin.flux_lock.action_creator.BLEActionCreator
import com.yushin.flux_lock.store.BLEStore
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment : Fragment() {

     lateinit var disposable : CompositeDisposable

     @Inject
     lateinit var bleStore: BLEStore
     @Inject
     lateinit var bleActionCreator: BLEActionCreator

     override fun onDestroyView() {
         disposable.dispose()
         super.onDestroyView()
     }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }

    override fun onResume() {
        super.onResume()
        disposable = CompositeDisposable()
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
         setOnBackPressed()
     }

     private fun setOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                back()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    protected fun back() {
        val fragmentManager = activity?.supportFragmentManager
        if (fragmentManager != null && fragmentManager.backStackEntryCount > 1) {
            // バックスタックにフラグメントがある場合は、通常の戻る操作を行う
            fragmentManager.popBackStack()
        } else {
            // バックスタックが空の場合、アクティビティを終了する
            activity?.finish()
        }
    }
}