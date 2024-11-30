package com.yushin.flux_lock.viewholder

import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.yushin.flux_lock.databinding.SettingItemOkButtonBinding
import com.yushin.flux_lock.model.ViewTypeCell

class OKButtonViewHolder(
    private val binding: SettingItemOkButtonBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: ViewTypeCell.OKButton) {
        binding.okButton.text = data.text
        binding.okButton.setOnClickListener {
            data.onClick()
        }
    }
}
