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
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
            binding.btnBtConnect.isEnabled = false
            mainViewModel.startScan()
        }
    }

    private val requestEnableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            binding.btnBtConnect.isEnabled = false
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
            sbTrainPower.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    val speed = progress - 255
                    tvSpeedometer.text = speed.toString()
                    mainViewModel.sendMessage(speed)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            btnBtConnect.setOnClickListener {
                requestBluetooth()
            }
            btnStop.setOnClickListener {
                sbTrainPower.progress = 255
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
                            R.drawable.ic_bluetooth_searching,
                            R.drawable.ic_led_red
                        )

                        ConnectionState.Connecting -> connectionStatusChanged(
                            R.drawable.ic_bluetooth_searching,
                            R.drawable.ic_led_yellow
                        )

                        ConnectionState.Connected -> connectionStatusChanged(
                            R.drawable.ic_bluetooth_connected,
                            R.drawable.ic_led_green
                        )

                        ConnectionState.Disconnecting -> connectionStatusChanged(
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
        @DrawableRes btDrawableId: Int,
        @DrawableRes ledDrawableId: Int
    ) {
        binding.btnBtConnect.setImageDrawable(ContextCompat.getDrawable(this, btDrawableId))
        binding.ivConnectionStatus.setImageDrawable(ContextCompat.getDrawable(this, ledDrawableId))
    }
}