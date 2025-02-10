package com.quaerense.legotraincontroller.view

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.quaerense.legotraincontroller.R
import com.quaerense.legotraincontroller.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory(application)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            svTrainPower.onChangeListener = { progress ->
                val speed = progress - 255
                tvSpeedometer.text = speed.toString()
                mainViewModel.sendMessage(speed)
            }
            btnMenu.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            nvMenu.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.item_connect -> requestBluetooth()
                }

                true
            }
            btnStop.setOnClickListener {
                svTrainPower.setProgress(255)
                mainViewModel.sendMessage(777)
            }
            mainViewModel.initBtAdapter(getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            lifecycleScope.launch {
                mainViewModel.connectionStatusStateFlow.flowWithLifecycle(
                    lifecycle,
                    Lifecycle.State.CREATED
                ).collect { status ->
                    when (status) {
                        ConnectionState.Disconnected -> connectionStatusChanged(
                            R.string.connect,
                            R.drawable.ic_bluetooth_searching,
                            R.drawable.ic_led_red
                        )

                        ConnectionState.Connecting -> connectionStatusChanged(
                            R.string.connect,
                            R.drawable.ic_bluetooth_searching,
                            R.drawable.ic_led_yellow
                        )

                        ConnectionState.Connected -> connectionStatusChanged(
                            R.string.connected,
                            R.drawable.ic_bluetooth_connected,
                            R.drawable.ic_led_green
                        )

                        ConnectionState.Disconnecting -> connectionStatusChanged(
                            R.string.connected,
                            R.drawable.ic_bluetooth_connected,
                            R.drawable.ic_led_yellow
                        )

                        ConnectionState.None -> {}
                    }
                }
            }
        }
    }

    private fun connectionStatusChanged(
        @StringRes btStringId: Int,
        @DrawableRes btDrawableId: Int,
        @DrawableRes ledDrawableId: Int
    ) {
        val connectItem = binding.nvMenu.menu.findItem(R.id.item_connect)
        connectItem.setTitle(btStringId)
        connectItem.setIcon(btDrawableId)
        binding.ivConnectionStatus.setImageDrawable(ContextCompat.getDrawable(this, ledDrawableId))
    }
}