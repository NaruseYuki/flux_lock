package com.yushin.flux_lock.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.candyhouse.sesame.open.device.CHDevices
import com.yushin.flux_lock.R

class BLEAdapter(
    private var devices: MutableList<CHDevices>, //dispList: MutableList<Any>,
    private val deviceClickListener: (CHDevices) -> Unit = {},
) : RecyclerView.Adapter<BLEAdapter.DevicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DevicesViewHolder(view)
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.bind(devices[position], deviceClickListener)
    }

    override fun getItemCount(): Int = devices.size

    fun updateDevices(newDevices: List<CHDevices>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    class DevicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceTitle = itemView.findViewById<TextView>(R.id.deviceTitle)
        fun bind(device: CHDevices, clickListener: (CHDevices) -> Unit) {
            deviceTitle.text = device.productModel.deviceModelName()
            itemView.setOnClickListener { clickListener(device) }
        }
    }
}
