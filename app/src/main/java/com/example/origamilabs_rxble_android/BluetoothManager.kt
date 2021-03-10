package com.example.origamilabs_rxble_android

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import timber.log.Timber


class BluetoothManager(private val context: Context) {
    private val bluetoothHelper by lazy {
        BluetoothHelper(context)
    }

    private val bleHelper: BleHelper by lazy {
        BleHelper(context)
    }

    private var device: BluetoothDevice? = null

    private val bluetoothBondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

            val newBondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
            val prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

            Timber.d("Bond state changed: $prevBondState => $newBondState")

            if (this@BluetoothManager.device?.address == device?.address) {
                if(newBondState==BluetoothDevice.BOND_BONDED)
                    bluetoothHelper.connectA2dp(device!!)
            }
        }
    }

    fun connectDevice(auto: Boolean) {
        val bluetoothDevice = bleHelper.getBluetoothDevice()
        device=bluetoothDevice

        if (bluetoothDevice != null)
            bluetoothHelper.bondDevice(bluetoothDevice)
    }

    fun registerReceivers() {
        registerBluetoothBondStateReceiver()
    }

    fun unregisterReceivers() {
        unregisterBluetoothBondStateReceiver()
    }

    private fun registerBluetoothBondStateReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(bluetoothBondStateReceiver, intentFilter)
    }

    private fun unregisterBluetoothBondStateReceiver() {
        context.unregisterReceiver(bluetoothBondStateReceiver)
    }
}