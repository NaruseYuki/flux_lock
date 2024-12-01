package com.yushin.flux_lock.viewholder

import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.action_creator.BLEActionCreator
import com.yushin.flux_lock.databinding.SettingItemUnlockBinding
import com.yushin.flux_lock.utils.LockState

class UnlockViewHolder(
    private val binding: SettingItemUnlockBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(device: CHDevices, bleActionCreator: BLEActionCreator) {
        binding.unlockPosition.setOnClickListener {
            bleActionCreator.configureLockPosition(device, LockState.Unlocked)
        }
    }
}
