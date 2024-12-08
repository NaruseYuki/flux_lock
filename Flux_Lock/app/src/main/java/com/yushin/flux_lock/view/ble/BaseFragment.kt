package com.yushin.flux_lock.view.ble

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.yushin.flux_lock.action_creator.BLEActionCreator
import com.yushin.flux_lock.store.BLEStore
import com.yushin.flux_lock.utils.SharedPreferencesHelper
import com.yushin.flux_lock.utils.Utils.addTo
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
open class BaseFragment : Fragment() {
      var disposable : CompositeDisposable = CompositeDisposable()
     @Inject
     lateinit var bleStore: BLEStore
     @Inject
     lateinit var bleActionCreator: BLEActionCreator
     lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }

    override fun onResume() {
        super.onResume()
        if(disposable.isDisposed){
            disposable = CompositeDisposable()
        }
        subscribeConnectedDevice()
        subscribeDeviceInitResult()
    }

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
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
    protected fun isConnect(index:Int):Boolean
        = (bleStore.getConnectedDevice().value == bleStore.getRegisteredDevices().value?.get(index))

    private fun subscribeConnectedDevice(){
        bleStore.getConnectionComplete()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                createToast("${bleStore.getConnectedDevice()
                    .value?.deviceId?.let { it1 ->
                        sharedPreferencesHelper.getDeviceName(
                            it1) }}"
                        + "\n" +
                        getString(com.yushin.flux_lock.R.string.connect_completed_text))
        }.addTo(disposable)
    }

    private fun subscribeDeviceInitResult(){
        bleStore.getDeviceInitResult()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                val text = if(!result){
                    "ロックをアプリから削除しました。\nロックを初期化できませんでした\nロックのボタンを長押しして初期化してください"
                } else{
                    "ロックをアプリから削除し、ロックを初期化しました"
                }
                createToast(text)
            }
            .addTo(disposable)
    }

    protected fun createToast(message:String)
        = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}