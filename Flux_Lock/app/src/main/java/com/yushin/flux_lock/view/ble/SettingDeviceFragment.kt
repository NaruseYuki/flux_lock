package com.yushin.flux_lock.view.ble

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.yushin.flux_lock.adapter.MultiLayoutRecyclerAdapter
import com.yushin.flux_lock.databinding.FragmentSettingDeviceBinding
import com.yushin.flux_lock.model.ViewTypeCell
import com.yushin.flux_lock.utils.Utils.addTo
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class SettingDeviceFragment : BaseFragment() {
    private lateinit var binding: FragmentSettingDeviceBinding
    @Inject
    lateinit var adapterFactory: MultiLayoutRecyclerAdapter.Factory

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

        val deviceData = bleStore.getConnectedDevice().value
        val deviceName = deviceData?.productModel?.deviceModelName().toString()

        val items: List<ViewTypeCell> = listOf(
                ViewTypeCell.TitleText("デバイス名を入力してください"),
                ViewTypeCell.EditText(deviceName),
                ViewTypeCell.TitleText("デバイスの施錠と解錠の位置を設定してください"),
                ViewTypeCell.AngleView
        )
        // Factoryを使ってアダプターを作成
        val adapter = adapterFactory.create(items)

        binding.settingRecycler.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }



    override fun onResume() {
        super.onResume()
        disposable = CompositeDisposable()
        bleStore.angleSubject.subscribe {

        }.addTo(disposable)
        // TODO:角度を入力する
    }
}
