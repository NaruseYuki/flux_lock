package com.yushin.flux_lock.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import co.candyhouse.sesame.open.device.CHSesame2
import co.candyhouse.sesame.open.device.CHSesame5
import com.yushin.flux_lock.R
import com.yushin.flux_lock.databinding.SettingAngleCellBinding
import com.yushin.flux_lock.databinding.SettingItemCellBinding
import com.yushin.flux_lock.databinding.SettingItemLockBinding
import com.yushin.flux_lock.databinding.SettingItemTitleCellBinding
import com.yushin.flux_lock.databinding.SettingItemUnlockBinding
import com.yushin.flux_lock.model.ViewType
import com.yushin.flux_lock.model.ViewTypeCell
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject


class MultiLayoutRecyclerAdapter
@AssistedInject constructor(
    @Assisted private val items: List<ViewTypeCell>,
    @Assisted private val device: CHDevices // Assistedで外部から渡される引数
) : RecyclerView.Adapter<MultiLayoutRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        return when (viewType) {
            R.layout.setting_item_title_cell -> {
                val binding = SettingItemTitleCellBinding.inflate(inflater, viewGroup, false)
                ViewHolder.TitleViewHolder(binding)
            }
            R.layout.setting_item_cell -> {
                val binding = SettingItemCellBinding.inflate(inflater, viewGroup, false)
                ViewHolder.EditTextViewHolder(binding)
            }
            R.layout.setting_angle_cell -> {
                val binding = SettingAngleCellBinding.inflate(inflater, viewGroup, false)
                ViewHolder.AngleViewHolder(binding)
            }
            R.layout.setting_item_lock -> {
                val binding = SettingItemLockBinding.inflate(inflater, viewGroup, false)
                ViewHolder.LockViewHolder(binding)
            }
            R.layout.setting_item_unlock -> {
                val binding = SettingItemUnlockBinding.inflate(inflater, viewGroup, false)
                ViewHolder.UnlockViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val cell = items[position]
        when (viewHolder) {
            is ViewHolder.TitleViewHolder -> viewHolder.bind(cell as ViewTypeCell.TitleText)
            is ViewHolder.EditTextViewHolder -> viewHolder.bind(cell as ViewTypeCell.EditText)
            is ViewHolder.AngleViewHolder -> viewHolder.bind(device)
            is ViewHolder.UnlockViewHolder -> viewHolder.bind(device)
            is ViewHolder.LockViewHolder -> viewHolder.bind(device)
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return ViewType.fromCell(items[position]).layoutId
    }

    fun updateItems(device: CHDevices) {
        items.forEachIndexed { index, it ->
            if (it is ViewTypeCell.AngleView) {
                it.device = device // ViewTypeCell.AngleViewにデバイス情報を持たせる

                notifyItemChanged(index)
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class TitleViewHolder(private val binding: SettingItemTitleCellBinding) :
            ViewHolder(binding.root) {
            fun bind(data: ViewTypeCell.TitleText) {
                binding.setting.text = data.text
            }
        }

        class EditTextViewHolder(private val binding: SettingItemCellBinding) :
            ViewHolder(binding.root) {
            fun bind(data: ViewTypeCell.EditText) {
                (binding.setting as TextView).text = data.text
            }
        }

        class AngleViewHolder(
            private val binding: SettingAngleCellBinding
        ) : ViewHolder(binding.root) {

            fun bind(device: CHDevices) {
                when (device) {
                    is CHSesame5 -> binding.ssmView.setLock(device)
                    is CHSesame2 -> binding.ssmView.setLock(device)
                    else -> {
                        // デフォルト処理やエラーハンドリング
                    }
                }
            }
        }
        class UnlockViewHolder(
            private val binding: SettingItemUnlockBinding
        ) : ViewHolder(binding.root) {
            fun bind(device: CHDevices) {
                binding.unlockPosition.setOnClickListener{

                }
            }
        }

        class LockViewHolder(
            private val binding: SettingItemLockBinding
        ) : ViewHolder(binding.root) {
            fun bind(device: CHDevices) {
                binding.lockPosition.setOnClickListener{

                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(items: List<ViewTypeCell>, device: CHDevices): MultiLayoutRecyclerAdapter
    }
}


