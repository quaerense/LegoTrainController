package com.quaerense.legotraincontroller.adapter

import androidx.recyclerview.widget.DiffUtil
import com.quaerense.legotraincontroller.RemoteDevice

object RemoteDeviceItemCallback : DiffUtil.ItemCallback<RemoteDevice>() {
    override fun areItemsTheSame(oldItem: RemoteDevice, newItem: RemoteDevice): Boolean {
        return oldItem.mac == newItem.mac
    }

    override fun areContentsTheSame(oldItem: RemoteDevice, newItem: RemoteDevice): Boolean {
        return oldItem.name == newItem.name
    }
}