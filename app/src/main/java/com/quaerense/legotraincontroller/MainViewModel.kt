package com.quaerense.legotraincontroller

import android.bluetooth.BluetoothAdapter
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private var btConnection: BtConnection? = null
    var btAdapter: BluetoothAdapter? = null

    private val _connectionStatusLiveData = MutableLiveData<Int>()
    val connectionStatusLiveData: LiveData<Int>
        get() = _connectionStatusLiveData

    fun showStatus(@StringRes status: Int) {
        _connectionStatusLiveData.postValue(status)
    }

    fun sendMessage(message: Int) {
        btConnection?.sendMessage(message)
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
}