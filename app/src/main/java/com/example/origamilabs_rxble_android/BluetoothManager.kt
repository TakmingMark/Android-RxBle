package com.example.origamilabs_rxble_android

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import timber.log.Timber
import java.util.*


class BluetoothManager(private val context: Context) {
    private val bluetoothHelper by lazy {
        BluetoothHelper(context)
    }

    private val bleHelper: BleHelper by lazy {
        BleHelper(context)
    }

    private var macAddress = ""

    private var device: BluetoothDevice? = null

    var bluetoothManagerListener: IBluetoothManagerListener? = null

    private val bluetoothBondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

            val newBondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
            val prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

            Timber.d("Bond state changed: $prevBondState => $newBondState")

//            if (this@BluetoothManager.device?.address == device?.address) {
//                if (newBondState == BluetoothDevice.BOND_BONDED)
//                    bluetoothHelper.connectA2dp(device!!)
//            }
        }
    }

    private val bluetoothAclStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                ?: return

            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Timber.d("Bluetooth device ACL connected")
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Timber.d("Bluetooth device ACL disconnected")
                }
            }
        }
    }

    private val bluetoothListener = object : BluetoothListener() {
        override fun onA2dpConnected() {
            if (!bleHelper.isConnectBleEnabled) {
                bleHelper.enabledObserveBleState(true)
                bleHelper.enabledConnectBle(true, device)
            }

        }
    }

    private val bleListener = object : BleListener() {
        override fun onObserveBleState(state: String) {
            bluetoothManagerListener?.onObserveBleState(state)
        }

        override fun onObserveBleStateError(error: String) {
            bluetoothManagerListener?.onObserveBleStateError(error)
        }

        override fun onScan(macAddress: String, deviceName: String, rssi: Int) {
            bluetoothManagerListener?.onScan(macAddress, deviceName, rssi)
        }

        override fun onScanError(error: String) {
            bluetoothManagerListener?.onScanError(error)
        }

        override fun onBleConnected(macAddress: String) {
            bluetoothManagerListener?.onBleConnected(macAddress)
            bleHelper.enabledDiscoverService(true)
        }

        override fun onConnectBleError(error: String) {
            bluetoothManagerListener?.onConnectBleError(error)
            error.apply {
                when {
                    this.contains("status 133") -> {
                        bleHelper.startConnectBleTimer()
                    }
                    this.contains("status 8") -> {
                        bleHelper.startConnectBleTimer()
                    }
                    this.contains("status 40") -> {
                        bleHelper.startConnectBleTimer()
                    }
                    this.contains("status 22") -> {
                        bleHelper.startConnectBleTimer()
                    }
                    this.contains("status 19") -> {
                        bleHelper.startConnectBleTimer()
                    }
                }
            }
        }

        override fun onDiscoverBleService(serviceUuid: UUID) {
            bluetoothManagerListener?.onDiscoverBleService(serviceUuid)
            bleHelper.enabledListenNotification(!bleHelper.isListenNotificationEnabled)
        }

        override fun onDiscoverBleServiceError(error: String) {
            bluetoothManagerListener?.onDiscoverBleServiceError(error)
        }

        override fun onReadBleCharacteristicValue(value: String) {
            bluetoothManagerListener?.onReadBleCharacteristicValue(value)
        }

        override fun onReadBleCharacteristicValueError(error: String) {
            bluetoothManagerListener?.onReadBleCharacteristicValueError(error)
        }

        override fun onListenNotification(characteristicUuid: UUID, value: Int) {
            bluetoothManagerListener?.onListenNotification(characteristicUuid, value)
        }

        override fun onListenNotificationError(error: String) {
            bluetoothManagerListener?.onListenNotificationError(error)
        }
    }

    init {
        bleHelper.bleListener = bleListener
        bluetoothHelper.bluetoothListener = bluetoothListener
    }

    fun scanDevice() {
        bleHelper.enabledScan(true)
    }

    fun stopScanDevice(){
        bleHelper.enabledScan(false)
    }

    fun connectDevice(macAddress: String, auto: Boolean) {
        val bluetoothDevice = bleHelper.getBluetoothDevice(macAddress)
        device = bluetoothDevice

        if (device == null)
            return

        bluetoothHelper.checkA2dpConnectedTimer()
    }

    fun printDeviceBondState(macAddress: String){
        val bluetoothDevice = bleHelper.getBluetoothDevice(macAddress)
        bluetoothHelper.printDeviceBondState(bluetoothDevice)
    }

    fun registerReceivers() {
        registerBluetoothBondStateReceiver()
        registerBluetoothAclStateReceiver()
    }

    fun unregisterReceivers() {
        unregisterBluetoothBondStateReceiver()
        unregisterBluetoothAclStateReceiver()
    }

    private fun registerBluetoothBondStateReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(bluetoothBondStateReceiver, intentFilter)
    }

    private fun unregisterBluetoothBondStateReceiver() {
        context.unregisterReceiver(bluetoothBondStateReceiver)
    }

    private fun registerBluetoothAclStateReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        context.registerReceiver(bluetoothAclStateReceiver, intentFilter)
    }

    private fun unregisterBluetoothAclStateReceiver() {
        context.unregisterReceiver(bluetoothAclStateReceiver)
    }

    interface IBluetoothManagerListener {
        fun onBondState(state: Int)
        fun onA2dpState(state: Int)

        fun onObserveBleState(state: String)
        fun onObserveBleStateError(error: String)

        fun onScan(macAddress: String, deviceName: String, rssi: Int)
        fun onScanError(error: String)

        fun onBleConnected(macAddress: String)
        fun onConnectBleError(error: String)

        fun onDiscoverBleService(serviceUuid: UUID)
        fun onDiscoverBleServiceError(error: String)

        fun onReadBleCharacteristicValue(value: String)
        fun onReadBleCharacteristicValueError(error: String)

        fun onListenNotification(characteristicUuid: UUID, value: Int)
        fun onListenNotificationError(error: String)
    }
}