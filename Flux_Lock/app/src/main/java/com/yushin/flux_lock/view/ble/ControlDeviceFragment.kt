package com.yushin.flux_lock.view.ble

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import co.candyhouse.sesame.open.device.CHDeviceStatus
import com.bumptech.glide.Glide
import com.yushin.flux_lock.R
import com.yushin.flux_lock.databinding.FragmentControlDeviceBinding
import com.yushin.flux_lock.utils.Utils.addTo
import com.yushin.flux_lock.view.BLEActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class ControlDeviceFragment : BaseFragment() {
    private lateinit var binding: FragmentControlDeviceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentControlDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addDevicesListener()
        editDeviceSetting()
        setLockImageListener()
    }

    override fun onResume() {
        super.onResume()
        subscribeDeviceStatus()
        connectFirstDevice()
    }

    /**
     * デバイスの状態を監視し、画像を更新する
     */
    private fun subscribeDeviceStatus() {
       bleStore.bleStatusSubject
           .observeOn(AndroidSchedulers.mainThread())
           .subscribe { status ->
               Log.d("BLE","@@@"+ status.toString())
               when(status){
                   CHDeviceStatus.Locked -> binding.lockImage.setImageResource(R.drawable.ic_lock)
                   CHDeviceStatus.Unlocked -> binding.lockImage.setImageResource(R.drawable.ic_unlock)
                   CHDeviceStatus.NoBleSignal -> {
                       binding.lockImage.setImageResource(R.drawable.ic_no_signal)
                   }
                   else -> {
                       Glide.with(requireActivity())
                           .load(R.raw.cycle_move)
                           .into(binding.lockImage)
                   }
               }
           }
           .addTo(disposable)
    }

    private fun setLockImageListener() {
        binding.lockImage.setOnClickListener {
            bleStore.getConnectedDevice().value
                .let {
                    if (it != null) {
                        bleActionCreator.toggle(it)
                    }
                }
         }
    }

    /**
     * リストの最初のデバイスに接続する
     */
    private fun connectFirstDevice() {
        // この画面を表示しているときに他動線からデバイスの変更なければこのままで良い
         bleStore.getRegisteredDevices().value?.
            let { devices ->
             binding.spinner.adapter =
                 ArrayAdapter(
                    requireContext(),
                    R.layout.item_device,
                    devices.map {
                        it.productModel.deviceModelName()
                    }
                )
             // 接続開始
             bleActionCreator.connectDevice(devices[0])
             binding.spinner.setSelection(0)
        }
    }

    private fun addDevicesListener() {
        binding.addDevices.setOnClickListener {
            // UnregisterDevicesFragmentを表示する
            (activity as BLEActivity).navigateFragment(
                R.id.container_main_fragment,
                UnregisterDevicesFragment()
            )
        }
    }

    private fun editDeviceSetting(){
        // EditDeviceSettingFragmentを表示する
        binding.settingButton.setOnClickListener {
            (activity as BLEActivity).navigateFragment(
                R.id.container_main_fragment,
                SettingDeviceFragment()
            )
        }
    }
}