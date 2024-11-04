package com.quaerense.legotraincontroller.connection

import android.bluetooth.BluetoothSocket
import java.io.IOException
import java.io.OutputStream

class MessageSender(bSocket: BluetoothSocket) {

    private var outStream: OutputStream? = null

    init {
        try {
            outStream = bSocket.outputStream
        } catch (e: IOException) {

        }
    }

    fun sendMessage(message: Int) {
        try {
            outStream?.write(message + 48)
        } catch (e: IOException) {

        }
    }
}