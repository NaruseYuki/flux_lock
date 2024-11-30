package com.yushin.flux_lock.viewholder

import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.action_creator.BLEActionCreator
import com.yushin.flux_lock.databinding.SettingItemLockRowBinding
import com.yushin.flux_lock.utils.LockState

class LockTextRowViewHolder(
    private val binding: SettingItemLockRowBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(device: CHDevices, bleActionCreator: BLEActionCreator) {
        binding.lockPositionRow.setOnClickListener {
            bleActionCreator.configureLockPosition(device, LockState.Locked)
        }
        binding.unlockPositionRow.setOnClickListener {
            bleActionCreator.configureLockPosition(device, LockState.Unlocked)
        }
    }
}
