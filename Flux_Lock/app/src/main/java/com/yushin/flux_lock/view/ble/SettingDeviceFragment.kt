package com.yushin.flux_lock.view.ble

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import com.yushin.flux_lock.adapter.BLEAdapter
import com.yushin.flux_lock.adapter.MultiLayoutRecyclerAdapter
import com.yushin.flux_lock.databinding.FragmentSettingDeviceBinding
import com.yushin.flux_lock.model.ViewTypeCell
import com.yushin.flux_lock.utils.Utils.addTo
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class SettingDeviceFragment : BaseFragment() {
    private var recyclerView: RecyclerView? = null
    private lateinit var binding: FragmentSettingDeviceBinding
    @Inject
    lateinit var adapterFactory: MultiLayoutRecyclerAdapter.Factory
    private lateinit var multiAdapter: MultiLayoutRecyclerAdapter

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

        val deviceName = deviceData.let {
            when (it) {
                is CHSesame2 -> it.productModel.deviceModelName()
                is CHSesame5 -> it.productModel.deviceModelName()
                else -> ""
            }
        }
        val items: List<ViewTypeCell> = listOf(
            ViewTypeCell.TitleText("デバイス名を入力してください"),
            ViewTypeCell.EditText(deviceName),
            ViewTypeCell.TitleText("デバイスの施錠と解錠の位置を設定してください"),
            ViewTypeCell.AngleView(deviceData)
        )
        // Factoryを使ってアダプターを作成
        multiAdapter = adapterFactory.create(items, deviceData)

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
//                val unLockPosition = (device as CHSesame2).mechSetting?.unlockPosition ?:0
//                device.mechStatus?.let { it1 -> (device as CHSesame5).configureLockPosition(it1.position, unLockPosition){
//
//                    }
//                }
            }.addTo(disposable)
    }
}
