package com.yushin.flux_lock.view.ble

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import com.yushin.flux_lock.R
import com.yushin.flux_lock.adapter.BLEAdapter
import com.yushin.flux_lock.adapter.MultiLayoutRecyclerAdapter
import com.yushin.flux_lock.databinding.FragmentSettingDeviceBinding
import com.yushin.flux_lock.model.ViewTypeCell
import com.yushin.flux_lock.utils.SharedPreferencesHelper
import com.yushin.flux_lock.utils.Utils.addTo
import com.yushin.flux_lock.viewholder.EditTextViewHolder
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.logging.Logger
import javax.inject.Inject

@AndroidEntryPoint
class SettingDeviceFragment : BaseFragment() {
    private var recyclerView: RecyclerView? = null
    private lateinit var binding: FragmentSettingDeviceBinding
    @Inject
    lateinit var adapterFactory: MultiLayoutRecyclerAdapter.Factory
    private lateinit var multiAdapter: MultiLayoutRecyclerAdapter
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSettingDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dispSettingView()
    }

    private fun dispSettingView() {
        val deviceData: CHDevices = bleStore.getConnectedDevice().value ?: return
        // sharedPrefsからデバイス名を取得
        val deviceName = deviceData.deviceId?.let { sharedPreferencesHelper.getDeviceName(it) } ?:""

        val items = mutableListOf(
            ViewTypeCell.TitleText(getString(R.string.input_device_name_text)),
            ViewTypeCell.EditText(deviceName),
            ViewTypeCell.TitleText(getString(R.string.input_device_angle_text)),
            ViewTypeCell.AngleView(deviceData),
            ViewTypeCell.TitleText(getString(R.string.input_device_button_text)),
            ViewTypeCell.LockButtonRow
        )
        items.add(
            ViewTypeCell.OKButton(getString(R.string.input_device_setting_ok)){
                val name = (items[1] as ViewTypeCell.EditText).text
                saveDeviceNameIfChanged(name, deviceData)
                Toast.makeText(requireContext(),
                    getString(R.string.input_device_setting_complete),
                    Toast.LENGTH_SHORT).show()
                // 操作画面へ遷移
                back()
            }
        )

        // Factoryを使ってアダプターを作成
        multiAdapter = adapterFactory.create(items, deviceData,bleActionCreator)

        this.recyclerView = binding.settingRecycler.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
            adapter = multiAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        disposable = CompositeDisposable()

        bleStore.getConnectedDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { device ->
                multiAdapter.updateItems(device)
            }.addTo(disposable)
    }

    private fun saveDeviceNameIfChanged(name: String, deviceData: CHDevices) {
        val oldName = deviceData.deviceId?.let { sharedPreferencesHelper.getDeviceName(it) }
        if (name != oldName) {
            deviceData.deviceId?.let { sharedPreferencesHelper.saveDeviceName(it, name) }
        }
    }
}
