package com.example.origamilabs_rxble_android.bluetooth.manager

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.origamilabs_rxble_android.bluetooth.ble.BleHelper
import com.example.origamilabs_rxble_android.bluetooth.ble.BleListener
import com.example.origamilabs_rxble_android.bluetooth.classic.BluetoothHelper
import com.example.origamilabs_rxble_android.bluetooth.classic.BluetoothListener
import com.polidea.rxandroidble2.RxBleDevice
import timber.log.Timber
import java.util.*


class BluetoothManager(private val context: Context) {
    private var gattServiceUuid: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private var characteristicUuid: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")


    private val bluetoothHelper by lazy {
        BluetoothHelper(context)
    }

    private val bleHelper: BleHelper by lazy {
        BleHelper(context)
    }

    private var isAutoConnectRunning = false

    private var macAddress = ""
    private var device: BluetoothDevice? = null

    var bluetoothManagerListener: IBluetoothManagerListener? = null

    private val bluetoothBondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

            val newBondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
            val prevBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

            Timber.d("Bond state changed: $prevBondState => $newBondState")
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

        override fun onScan(
            rxBleDevice: RxBleDevice,
            macAddress: String,
            deviceName: String,
            rssi: Int
        ) {
            bluetoothManagerListener?.onScan(macAddress, deviceName, rssi)

            if (isAutoConnectRunning) {
                device = rxBleDevice.bluetoothDevice
                startConnectDevice(device!!)
                stopScanDevice()
            }
        }

        override fun onScanError(error: String) {
            bluetoothManagerListener?.onScanError(error)
        }

        override fun onBleConnected(macAddress: String) {
            bluetoothManagerListener?.onBleConnected(macAddress)

            if (isAutoConnectRunning)
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

        override fun onDiscoverBleService(gattServiceUuid: UUID, characteristicUuids: List<UUID>) {
            bluetoothManagerListener?.onDiscoverBleService(gattServiceUuid, characteristicUuids)
            val gattServiceExist = this@BluetoothManager.gattServiceUuid == gattServiceUuid
            if (isAutoConnectRunning && gattServiceExist)
                bleHelper.enabledListenNotification(
                    !bleHelper.isListenNotificationEnabled,
                    characteristicUuid
                )
        }

        override fun onDiscoverBleService(serviceUuid: UUID) {
            bluetoothManagerListener?.onDiscoverBleService(serviceUuid)


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


    fun isObserveBleStateRunning(): Boolean {
        return bleHelper.isObserveBleStateEnabled
    }

    fun startObserveBleState(): Boolean {
        return bleHelper.enabledObserveBleState(true)
    }

    fun stopObserveBleState(): Boolean {
        return bleHelper.enabledObserveBleState(false)
    }

    fun isScanDeviceRunning(): Boolean {
        return bleHelper.isScanEnabled
    }

    fun startScanDevice(): Boolean {
        return bleHelper.enabledScan(true)
    }

    fun stopScanDevice(): Boolean {
        return bleHelper.enabledScan(false)
    }

    fun isConnectDeviceRunning(): Boolean {
        return bleHelper.isConnectBleEnabled
    }

    fun startConnectDevice(macAddress: String) {
        val bluetoothDevice = bleHelper.getBluetoothDevice(macAddress)
        bleHelper.enabledConnectBle(true, bluetoothDevice)
    }

    fun startConnectDevice(device: BluetoothDevice) {
        bleHelper.enabledConnectBle(true, device)
    }

    fun stopConnectDevice() {
        bleHelper.enabledConnectBle(false, null)
    }

    fun isDiscoverServiceRunning(): Boolean {
        return bleHelper.isDiscoverServiceEnabled
    }

    fun startDiscoverService() {
        bleHelper.enabledDiscoverService(true)
    }

    fun stopDiscoverService() {
        bleHelper.enabledDiscoverService(false)
    }

    fun isReadCharacteristicValueRunning(): Boolean {
        return bleHelper.isReadCharacteristicEnabled
    }

    fun startReadCharacteristicValue() {
        bleHelper.enabledReadBleCharacteristicValue(true, characteristicUuid)
    }

    fun stopReadCharacteristicValue() {
        bleHelper.enabledReadBleCharacteristicValue(false, characteristicUuid)
    }

    fun isListenNotificationRunning(): Boolean {
        return bleHelper.isListenNotificationEnabled
    }

    fun startListenNotification() {
        bleHelper.enabledListenNotification(true, characteristicUuid)
    }

    fun stopListenNotification() {
        bleHelper.enabledListenNotification(false, characteristicUuid)
    }

    fun isAutoConnectRunning(): Boolean {
        return isAutoConnectRunning
    }

    fun startAutoConnect(macAddress: String) {
        this.macAddress = macAddress
        startScanDevice()
        isAutoConnectRunning = true
    }

    fun startAutoConnect(device: BluetoothDevice) {
        when {
            device == null -> {
                bluetoothManagerListener?.onAutoConnectError(AutoConnectErrorCode.DEVICE_NULL_ERROR.name)
                return
            }
            device.bondState != BOND_BONDED -> {
                bluetoothManagerListener?.onAutoConnectError(AutoConnectErrorCode.BOND_ERROR.name)
                return
            }
            !bluetoothHelper.isA2dpConnected() -> {
                bluetoothManagerListener?.onAutoConnectError(AutoConnectErrorCode.A2DP_CONNECT_Error_ERROR.name)
                return
            }
        }
        startConnectDevice(device)
    }

    fun stopAutoConnect() {
        stopObserveBleState()
        stopScanDevice()
        stopListenNotification()
        stopReadCharacteristicValue()
        stopDiscoverService()
        stopConnectDevice()
        isAutoConnectRunning = false
    }

    fun getBluetoothDevice(macAddress: String): BluetoothDevice? {
        return bleHelper.getBluetoothDevice(macAddress)
    }

    fun bondDevice(device: BluetoothDevice?) {
        if (device == null)
            return

        bluetoothHelper.bondDevice(device)
    }

    fun connectA2dp(device: BluetoothDevice?) {
        if (device == null)
            return

        bluetoothHelper.connectA2dp(device)
    }


    // For Service use
    fun connectDevice(macAddress: String) {
        val bluetoothDevice = bleHelper.getBluetoothDevice(macAddress)
        device = bluetoothDevice


        if (device == null)
            return

        bluetoothHelper.checkA2dpConnectedTimer()
    }

    fun printDeviceBondState(macAddress: String) {
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
        fun onDiscoverBleService(gattServiceUuid: UUID, characteristicUuids: List<UUID>)
        fun onDiscoverBleServiceError(error: String)

        fun onReadBleCharacteristicValue(value: String)
        fun onReadBleCharacteristicValueError(error: String)

        fun onListenNotification(characteristicUuid: UUID, value: Int)
        fun onListenNotificationError(error: String)

        fun onAutoConnectSuccess()
        fun onAutoConnectError(error: String)
    }

    enum class AutoConnectErrorCode(val code: Int) {
        DEVICE_NULL_ERROR(1),
        BOND_ERROR(2),
        A2DP_CONNECT_Error_ERROR(3)
    }
}