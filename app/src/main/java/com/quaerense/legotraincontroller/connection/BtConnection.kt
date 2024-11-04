package com.quaerense.legotraincontroller.connection

import android.bluetooth.BluetoothAdapter

class BtConnection(
    private val adapter: BluetoothAdapter,
    private val onConnectionStarted: () -> Unit,
    private val onConnected: () -> Unit,
    private val onConnectionClosed: () -> Unit,
    private val onConnectionFailed: () -> Unit
) {

    private lateinit var connectionThread: ConnectionThread

    fun connect(mac: String) {
        if (adapter.isEnabled && mac.isNotEmpty()) {
            val device = adapter.getRemoteDevice(mac)
            device?.let {
                connectionThread = ConnectionThread(
                    it,
                    onConnectionStarted,
                    onConnected,
                    onConnectionClosed,
                    onConnectionFailed
                )
                connectionThread.start()
            }
        }
    }

    fun sendMessage(message: Int) {
        connectionThread.messageSender?.sendMessage(message)
    }
}