package com.yushin.flux_lock.viewholder

import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.action_creator.BLEActionCreator
import com.yushin.flux_lock.databinding.SettingItemLockBinding
import com.yushin.flux_lock.utils.LockState

class LockViewHolder(
    private val binding: SettingItemLockBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(device: CHDevices, bleActionCreator: BLEActionCreator) {
        binding.lockPosition.setOnClickListener {
            bleActionCreator.configureLockPosition(device, LockState.Locked)
        }
    }
}
