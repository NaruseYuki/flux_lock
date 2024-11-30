package com.yushin.flux_lock.viewholder

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yushin.flux_lock.databinding.SettingItemCellBinding
import com.yushin.flux_lock.model.ViewTypeCell

class EditTextViewHolder(
    val binding: SettingItemCellBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(data: ViewTypeCell.EditText, onTextChanged: (String) -> Unit) {
        binding.setting.setText(data.text)
        binding.setting.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
