package com.quaerense.legotraincontroller.view

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.quaerense.legotraincontroller.databinding.FragmentDeviceListBinding
import com.quaerense.legotraincontroller.view.adapter.RemoteDeviceAdapter

class DeviceListFragment : DialogFragment() {

    private lateinit var binding: FragmentDeviceListBinding
    private lateinit var remoteDeviceAdapter: RemoteDeviceAdapter

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )[MainViewModel::class.java]
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[BLUETOOTH_SCAN] == true && permissions[BLUETOOTH_CONNECT] == true && permissions[ACCESS_FINE_LOCATION] == true) {
            mainViewModel.startScan()
        }
    }

    private val requestEnableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            mainViewModel.startScan()
        } else {
            // denied
        }
    }

    private fun requestBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT, ACCESS_FINE_LOCATION)
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBluetooth.launch(enableBtIntent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        requestBluetooth()
        remoteDeviceAdapter = RemoteDeviceAdapter { mac ->
//            mainViewModel.connect(mac)
        }
        rvDevices.adapter = remoteDeviceAdapter
        mainViewModel.pairedDevicesLiveData.observe(viewLifecycleOwner) { devices ->
            remoteDeviceAdapter.submitList(devices)
        }
    }
}