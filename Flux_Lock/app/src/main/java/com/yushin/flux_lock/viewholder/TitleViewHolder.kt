package com.yushin.flux_lock.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.yushin.flux_lock.databinding.SettingItemTitleCellBinding
import com.yushin.flux_lock.model.ViewTypeCell

class TitleViewHolder(
    private val binding: SettingItemTitleCellBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: ViewTypeCell.TitleText) {
        binding.setting.text = data.text
    }
}
