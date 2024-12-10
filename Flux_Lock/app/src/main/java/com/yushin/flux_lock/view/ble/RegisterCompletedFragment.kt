package com.yushin.flux_lock.view.ble

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yushin.flux_lock.R
import com.yushin.flux_lock.databinding.FragmentNoDevicesBinding
import com.yushin.flux_lock.databinding.FragmentRegisterCompletedBinding
import com.yushin.flux_lock.utils.Utils.addTo
import com.yushin.flux_lock.view.BLEActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.completeTextTrans.setOnClickListener {
            bleActionCreator.loadRegisteredDevices()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.VISIBLE
        binding.completeText.visibility = View.GONE
        binding.completeTextTrans.visibility = View.GONE
        bleStore.getRegisterCompleteSubject()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.completeText.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.completeTextTrans.visibility = View.VISIBLE
            }
            .addTo(disposable)
    }
}