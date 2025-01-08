package com.quaerense.legotraincontroller.view

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.quaerense.legotraincontroller.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(application)
        )[MainViewModel::class.java]
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
                    val speed = when (progress) {
                        1, 7 -> 90
                        2, 6 -> 60
                        3, 5 -> 30
                        else -> 0
                    }
                    tvSpeedometer.text = speed.toString()
                    mainViewModel.sendMessage(progress.toByte())
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            btnSearchDevices.setOnClickListener {
                DeviceListFragment().show(supportFragmentManager, null)
            }
            btnStop.setOnClickListener {
                simulateClick(sbTrainPower)
            }
            btnDoors.setOnClickListener {

            }

            mainViewModel.initBtAdapter(getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            mainViewModel.connectionStatusLiveData.observe(this@MainActivity) { status ->
                runOnUiThread {
                    Toast.makeText(this@MainActivity, status, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun simulateClick(view: View) {
        val centerX = 103.75
        val centerY = 193.0

        val downEvent = MotionEvent.obtain(
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            MotionEvent.ACTION_DOWN,
            centerX.toFloat(),
            centerY.toFloat(),
            0
        )

        view.dispatchTouchEvent(downEvent)

        val upEvent = MotionEvent.obtain(
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            MotionEvent.ACTION_UP,
            centerX.toFloat(),
            centerY.toFloat(),
            0
        )

        view.dispatchTouchEvent(upEvent)

        downEvent.recycle()
        upEvent.recycle()
    }
}