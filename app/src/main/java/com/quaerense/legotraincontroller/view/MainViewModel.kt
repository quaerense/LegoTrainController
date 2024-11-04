package com.quaerense.legotraincontroller.view

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.quaerense.legotraincontroller.R
import com.quaerense.legotraincontroller.connection.BtConnection
import com.quaerense.legotraincontroller.connection.RemoteDevice

class MainViewModel : ViewModel() {

    private var btConnection: BtConnection? = null
    private var btAdapter: BluetoothAdapter? = null

    private val _pairedDevicesLiveData = MutableLiveData<List<RemoteDevice>>()
    val pairedDevicesLiveData: LiveData<List<RemoteDevice>>
        get() = _pairedDevicesLiveData

    private val _connectionStatusLiveData = MutableLiveData<Int>()
    val connectionStatusLiveData: LiveData<Int>
        get() = _connectionStatusLiveData

    fun initBtAdapter(bluetoothManager: BluetoothManager) {
        btAdapter = bluetoothManager.adapter
    }

    fun sendMessage(message: Int) {
        btConnection?.sendMessage(message)
    }

    fun showPairedDevices() {
        val remoteDevices = mutableListOf<RemoteDevice>()
        btAdapter?.bondedDevices?.forEach { device ->
            remoteDevices.add(RemoteDevice(device.address, device.name))
        }
        _pairedDevicesLiveData.value = remoteDevices
    }

    fun connect(mac: String) {
        btAdapter?.let {
            btConnection = BtConnection(
                adapter = it,
                onConnectionStarted = { showStatus(R.string.connection_started) },
                onConnected = { showStatus(R.string.connected) },
                onConnectionClosed = { showStatus(R.string.connection_closed) },
                onConnectionFailed = { showStatus(R.string.connection_failed) },
            )

            btConnection?.connect(mac)
        }
    }

    private fun showStatus(@StringRes status: Int) {
        _connectionStatusLiveData.postValue(status)
    }
}