package com.quaerense.legotraincontroller.view

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_SINT32
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _connectionStatusStateFlow = MutableStateFlow<ConnectionState>(ConnectionState.None)
    val connectionStatusStateFlow = _connectionStatusStateFlow.asStateFlow()

    private var btAdapter: BluetoothAdapter? = null
    private var btGatt: BluetoothGatt? = null
    private var btGattService: BluetoothGattService? = null
    private var btGattCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceUuid: UUID? = null

    private val messageHandler = Handler(Looper.getMainLooper())
    private val scanHandler = Handler(Looper.getMainLooper())

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            _connectionStatusStateFlow.value = when (newState) {
                BluetoothProfile.STATE_DISCONNECTED -> ConnectionState.Disconnected
                BluetoothProfile.STATE_CONNECTING -> ConnectionState.Connecting
                BluetoothProfile.STATE_CONNECTED -> ConnectionState.Connected
                BluetoothProfile.STATE_DISCONNECTING -> ConnectionState.Disconnecting
                else -> throw IllegalStateException("No such connection state")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                btGattService = btGatt?.getService(deviceUuid)
                btGatt?.getService(deviceUuid)
                btGattCharacteristic = btGattService?.characteristics?.get(0)
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val uuid = result.scanRecord?.serviceUuids?.get(0)?.uuid
            if (uuid.toString() == UUID) {
                stopScan()
                btGatt = device.connectGatt(application, false, bluetoothGattCallback)
                deviceUuid = uuid
                scanHandler.postDelayed(
                    { btGatt?.discoverServices() },
                    2000
                )
            }
        }
    }

    fun startScan() {
        if (connectionStatusStateFlow.value == ConnectionState.Connected) {
            btGatt?.disconnect()
        } else {
            btAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
        }
    }

    private fun stopScan() {
        btAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
    }

    fun initBtAdapter(bluetoothManager: BluetoothManager) {
        btAdapter = bluetoothManager.adapter
    }

    fun sendMessage(message: Int) {
        btGattCharacteristic?.let {
            messageHandler.removeCallbacksAndMessages(null)
            messageHandler.postDelayed(
                {
                    it.setValue(message, FORMAT_SINT32, 0)
                    btGatt?.writeCharacteristic(it)
                },
                50
            )
        }
    }

    companion object {

        private const val UUID = "19b10000-e8f2-537e-4f6c-d104768a1214"
    }
}