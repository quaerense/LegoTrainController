package com.quaerense.legotraincontroller.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.quaerense.legotraincontroller.connection.RemoteDevice
import com.quaerense.legotraincontroller.databinding.RemoteDeviceItemBinding

class RemoteDeviceAdapter(
    private val onDeviceClick: (String) -> Unit
) : ListAdapter<RemoteDevice, RemoteDeviceViewHolder>(
    RemoteDeviceItemCallback
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemoteDeviceViewHolder {
        val binding = RemoteDeviceItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return RemoteDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RemoteDeviceViewHolder, position: Int
    ) = with(holder.binding) {
        val device = getItem(position)
        tvName.text = device.name
        tvMac.text = device.mac
        root.setOnClickListener {
            onDeviceClick(device.mac)
        }
    }
}