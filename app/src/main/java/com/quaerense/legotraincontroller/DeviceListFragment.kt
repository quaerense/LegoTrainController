package com.quaerense.legotraincontroller

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.quaerense.legotraincontroller.adapter.RemoteDeviceAdapter
import com.quaerense.legotraincontroller.databinding.FragmentDeviceListBinding

class DeviceListFragment : DialogFragment() {

    private lateinit var binding: FragmentDeviceListBinding
    private lateinit var remoteDeviceAdapter: RemoteDeviceAdapter

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        remoteDeviceAdapter = RemoteDeviceAdapter { mac ->
            mainViewModel.connect(mac)
        }
        rvDevices.adapter = remoteDeviceAdapter
        showPairedDevices()
    }

    private fun showPairedDevices() {
        val remoteDevices = mutableListOf<RemoteDevice>()
        mainViewModel.btAdapter?.bondedDevices?.forEach { device ->
            remoteDevices.add(RemoteDevice(device.address, device.name))
        }
        remoteDeviceAdapter.submitList(remoteDevices)
    }
}