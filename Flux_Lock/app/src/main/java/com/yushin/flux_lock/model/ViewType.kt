package com.yushin.flux_lock.model

import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.R

sealed class ViewTypeCell {
    data class TitleText(val text: String) : ViewTypeCell()
    data class EditText(var text: String) : ViewTypeCell()
    data class AngleView(var device: CHDevices) : ViewTypeCell()
    data object LockView : ViewTypeCell()
    data object UnlockView: ViewTypeCell()
    data object LockButtonRow : ViewTypeCell()
    data class OKButton(
        val text: String,
        val onClick: () -> Unit = {}) : ViewTypeCell()
}

sealed class ViewType(val layoutId:Int) {
    data object Key:ViewType(R.layout.setting_item_title_cell)
    data object Value:ViewType(R.layout.setting_item_cell)
    data object Angle:ViewType(R.layout.setting_angle_cell)
    data object Lock:ViewType(R.layout.setting_item_lock)
    data object Unlock:ViewType(R.layout.setting_item_unlock)
    data object LockButtonRow:ViewType(R.layout.setting_item_lock_row)
    data object OKButton:ViewType(R.layout.setting_item_ok_button)

    companion object {
        fun fromCell(cell: ViewTypeCell): ViewType {
            return when (cell) {
                is ViewTypeCell.TitleText -> Key
                is ViewTypeCell.EditText -> Value
                is ViewTypeCell.AngleView -> Angle
                is ViewTypeCell.LockView -> Lock
                is ViewTypeCell.UnlockView -> Unlock
                is ViewTypeCell.LockButtonRow -> LockButtonRow
                is ViewTypeCell.OKButton -> OKButton
            }
        }
    }
}



