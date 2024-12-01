package com.yushin.flux_lock.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.action_creator.BLEActionCreator
import com.yushin.flux_lock.factory.ViewHolderFactory
import com.yushin.flux_lock.model.ViewType
import com.yushin.flux_lock.model.ViewTypeCell
import com.yushin.flux_lock.viewholder.AngleViewHolder
import com.yushin.flux_lock.viewholder.EditTextViewHolder
import com.yushin.flux_lock.viewholder.LockTextRowViewHolder
import com.yushin.flux_lock.viewholder.LockViewHolder
import com.yushin.flux_lock.viewholder.OKButtonViewHolder
import com.yushin.flux_lock.viewholder.TitleViewHolder
import com.yushin.flux_lock.viewholder.UnlockViewHolder
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject


class MultiLayoutRecyclerAdapter
@AssistedInject constructor(
    @Assisted private val items: List<ViewTypeCell>,
    @Assisted private val device: CHDevices, // Assistedで外部から渡される引数
    @Assisted private var bleActionCreator: BLEActionCreator
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderFactory.create(viewType, viewGroup)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.isFocusable = true
        viewHolder.itemView.isFocusableInTouchMode = true
        val cell = items[position]
        when (viewHolder) {
            is TitleViewHolder -> viewHolder.bind(cell as ViewTypeCell.TitleText)
            is EditTextViewHolder -> viewHolder.bind(cell as ViewTypeCell.EditText){ updatedText ->
                // リアルタイムでアイテムを更新
                (items[position] as ViewTypeCell.EditText).text = updatedText
            }
            is AngleViewHolder -> viewHolder.bind(device)
            is UnlockViewHolder -> viewHolder.bind(device, bleActionCreator)
            is LockViewHolder -> viewHolder.bind(device, bleActionCreator)
            is LockTextRowViewHolder -> viewHolder.bind(device, bleActionCreator)
            is OKButtonViewHolder -> viewHolder.bind(cell as ViewTypeCell.OKButton)
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

    @AssistedFactory
    interface Factory {
        fun create(items: List<ViewTypeCell>,
                   device: CHDevices,
                   bleActionCreator: BLEActionCreator): MultiLayoutRecyclerAdapter
    }
}


