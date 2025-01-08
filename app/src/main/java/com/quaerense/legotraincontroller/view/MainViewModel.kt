package com.quaerense.legotraincontroller.view

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quaerense.legotraincontroller.R
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _connectionStatusLiveData = MutableLiveData<Int>()
    val connectionStatusLiveData: LiveData<Int>
        get() = _connectionStatusLiveData

    private var btAdapter: BluetoothAdapter? = null
    private var btGatt: BluetoothGatt? = null
    private var btGattService: BluetoothGattService? = null
    private var btGattCharacteristic: BluetoothGattCharacteristic? = null
    private var deviceUuid: UUID? = null

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            _connectionStatusLiveData.postValue(
                when (newState) {
                    BluetoothProfile.STATE_DISCONNECTED -> R.string.connection_closed
                    BluetoothProfile.STATE_CONNECTING -> R.string.connection_started
                    BluetoothProfile.STATE_CONNECTED -> R.string.connected
                    BluetoothProfile.STATE_DISCONNECTING -> R.string.connection_closing
                    else -> throw IllegalStateException("No such connection state")
                }
            )
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
            if (device.address == "9C:9E:6E:F7:86:16") {
                stopScan()
                btGatt = device.connectGatt(application, false, bluetoothGattCallback)
                deviceUuid = result.scanRecord?.serviceUuids?.get(0)?.uuid
                Handler(Looper.getMainLooper()).postDelayed({ btGatt?.discoverServices() }, 2000)
            }
        }
    }

    fun startScan() {
        btAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
    }

    private fun stopScan() {
        btAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
    }

    fun initBtAdapter(bluetoothManager: BluetoothManager) {
        btAdapter = bluetoothManager.adapter
    }

    fun sendMessage(message: Byte) {
        btGattCharacteristic?.let {
            it.setValue(byteArrayOf(message))
            btGatt?.writeCharacteristic(it)
        }
    }
}