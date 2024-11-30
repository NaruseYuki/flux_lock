package com.yushin.flux_lock.viewholder

import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import com.yushin.flux_lock.databinding.SettingAngleCellBinding

class AngleViewHolder(
    private val binding: SettingAngleCellBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(device: CHDevices) {
        when (device) {
            is CHSesame5 -> binding.ssmView.setLock(device)
            is CHSesame2 -> binding.ssmView.setLock(device)
            else -> {
                // 何もしない
            }
        }
    }
}
