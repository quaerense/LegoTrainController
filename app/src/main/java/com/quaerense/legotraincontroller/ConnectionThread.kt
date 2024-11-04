package com.quaerense.legotraincontroller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.util.UUID

class ConnectionThread(
    device: BluetoothDevice,
    private val onConnectionStarted: () -> Unit,
    private val onConnected: () -> Unit,
    private val onConnectionClosed: () -> Unit,
    private val onConnectionFailed: () -> Unit
) : Thread() {

    private val uuid = "00001101-0000-1000-8000-00805f9b34fb"
    private var mSocket: BluetoothSocket? = null
    var messageSender: MessageSender? = null

    init {
        try {
            mSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid))
        } catch (e: IOException) {
            onConnectionFailed()
        }
    }

    override fun run() {
        try {
            onConnectionStarted()
            mSocket?.connect()
            onConnected()
            mSocket?.let {
                messageSender = MessageSender(it)
            }
        } catch (e: IOException) {
            onConnectionClosed()
            closeConnection()
        }
    }

    private fun closeConnection() {
        try {
            mSocket?.close()
        } catch (e: IOException) {

        }
    }
}