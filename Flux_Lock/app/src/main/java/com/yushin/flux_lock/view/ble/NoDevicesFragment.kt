package com.yushin.flux_lock.view.ble

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yushin.flux_lock.R
import com.yushin.flux_lock.databinding.FragmentNoDevicesBinding
import com.yushin.flux_lock.view.BLEActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable

class NoDevicesFragment : BaseFragment() {
    private lateinit var binding: FragmentNoDevicesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoDevicesBinding.inflate(inflater,container,false)
        addDevicesListener()
        return binding.root
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

    override fun onResume() {
        super.onResume()
        disposable = CompositeDisposable()
    }
}