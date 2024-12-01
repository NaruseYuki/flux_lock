package com.yushin.flux_lock.view.ble

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yushin.flux_lock.R
import com.yushin.flux_lock.databinding.FragmentNoDevicesBinding
import com.yushin.flux_lock.databinding.FragmentRegisterCompletedBinding
import com.yushin.flux_lock.view.BLEActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable

class RegisterCompletedFragment : BaseFragment() {
    private lateinit var binding: FragmentRegisterCompletedBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterCompletedBinding.inflate(inflater,container,false)
        return binding.root
    }
}