package com.yushin.flux_lock.factory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yushin.flux_lock.databinding.*
import com.yushin.flux_lock.viewholder.*
import com.yushin.flux_lock.R

object ViewHolderFactory {
    fun create(viewType: Int, parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.setting_item_title_cell -> {
                val binding = SettingItemTitleCellBinding.inflate(inflater, parent, false)
                TitleViewHolder(binding)
            }
            R.layout.setting_item_cell -> {
                val binding = SettingItemCellBinding.inflate(inflater, parent, false)
                EditTextViewHolder(binding)
            }
            R.layout.setting_angle_cell -> {
                val binding = SettingAngleCellBinding.inflate(inflater, parent, false)
                AngleViewHolder(binding)
            }
            R.layout.setting_item_lock -> {
                val binding = SettingItemLockBinding.inflate(inflater, parent, false)
                LockViewHolder(binding)
            }
            R.layout.setting_item_unlock -> {
                val binding = SettingItemUnlockBinding.inflate(inflater, parent, false)
                UnlockViewHolder(binding)
            }
            R.layout.setting_item_lock_row -> {
                val binding = SettingItemLockRowBinding.inflate(inflater, parent, false)
                LockTextRowViewHolder(binding)
            }
            R.layout.setting_item_ok_button -> {
                val binding = SettingItemOkButtonBinding.inflate(inflater, parent, false)
                OKButtonViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
}
