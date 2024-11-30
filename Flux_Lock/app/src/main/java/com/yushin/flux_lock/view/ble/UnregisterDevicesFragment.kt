package com.yushin.flux_lock.view.ble

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.R
import com.yushin.flux_lock.utils.Utils.addTo
import com.yushin.flux_lock.adapter.BLEAdapter
import com.yushin.flux_lock.databinding.FragmentUnregisteredDevicesBinding
import com.yushin.flux_lock.view.BLEActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class UnregisterDevicesFragment : BaseFragment() {
    private var recyclerView: RecyclerView? = null
    private lateinit var bleAdapter: BLEAdapter
    private lateinit var binding: FragmentUnregisteredDevicesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentUnregisteredDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bleAdapter = BLEAdapter(mutableListOf(), ::onDeviceClicked)

        this.recyclerView = binding.containerRecyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(context)
            adapter = bleAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        // BLEStoreを監視し、変更があったらUIを更新する
        bleStore.getUnregisteredDevices()
            .skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ devices ->
                bleAdapter.updateDevices(devices)
            }.addTo(disposable)

        // 初期データをロードする
        bleActionCreator.loadUnregisteredDevices()
    }


    private fun onDeviceClicked(device: CHDevices) {
        Log.d("UnregisterDevicesFragment", "onDeviceClicked: $device")
        // 接続を実行する
        bleActionCreator.firstConnectDevice(device)
        (activity as BLEActivity).navigateFragment(
            R.id.container_main_fragment,
            RegisterCompletedFragment() // 名前の設定画面へ
        )
    }
}