package com.yushin.flux_lock.viewholder

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yushin.flux_lock.databinding.SettingItemCellBinding
import com.yushin.flux_lock.model.ViewTypeCell

class EditTextViewHolder(
    private val binding: SettingItemCellBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: ViewTypeCell.EditText) {
        (binding.setting as TextView).text = data.text
    }
}
