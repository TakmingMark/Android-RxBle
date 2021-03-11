package com.example.origamilabs_rxble_android

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.*
import android.widget.Button
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var message = ""

    private val rxPermissions: RxPermissions by lazy {
        RxPermissions(this)
    }

    private val bluetoothHelper by lazy {
        BluetoothHelper(this)
    }

    private val bleHelper: BleHelper by lazy {
        BleHelper(this)
    }

    private val bluetoothManager: BluetoothManager by lazy {
        BluetoothManager(this)
    }

    private var grantPermissionsDisposable: Disposable? = null

    private val bluetoothManagerListener = object : BluetoothManagerListener() {
        override fun onObserveBleState(state: String) {
            appendMessageView("observeBleState:$state")
        }

        override fun onObserveBleStateError(error: String) {
            appendMessageView(error)
        }

        override fun onScan(macAddress: String, deviceName: String, rssi: Int) {
            appendMessageView("Found $macAddress,$deviceName,$rssi")
        }

        override fun onScanError(error: String) {
            appendMessageView(error)
        }

        override fun onBleConnected(macAddress: String) {
            appendMessageView("connected $macAddress")
        }

        override fun onConnectBleError(error: String) {
            appendMessageView(error)
        }

        override fun onListenNotification(characteristicUuid: UUID, value: Int) {
            appendMessageView("Listen:$value")
        }

        override fun onListenNotificationError(error: String) {
            appendMessageView(error)
        }
    }

    private val bleListener = object : BleListener() {
        override fun onObserveBleState(state: String) {
            appendMessageView("observeBleState:$state")
        }

        override fun onObserveBleStateError(error: String) {
            appendMessageView(error)
        }

        override fun onScan(macAddress: String, deviceName: String, rssi: Int) {
            appendMessageView("Found $macAddress,$deviceName,$rssi")
        }

        override fun onScanError(error: String) {
            appendMessageView(error)
        }

        override fun onBleConnected(macAddress: String) {
            appendMessageView("connected $macAddress")
        }

        override fun onConnectBleError(error: String) {
            appendMessageView(error)
        }

        override fun onListenNotification(characteristicUuid: UUID, value: Int) {
            appendMessageView("Listen:$value")
        }

        override fun onListenNotificationError(error: String) {
            appendMessageView(error)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        grantPermissions()
        initListener()
    }

    private fun initListener() {
        bleHelper.bleListener = this.bleListener
        bluetoothManager.bluetoothManagerListener = this.bluetoothManagerListener

        observe_bluetooth_state_button.setOnClickListener {
            bleHelper.enabledObserveBleState(!bleHelper.isObserveBleStateEnabled)
            changeEnabledButtonText(
                bleHelper.isObserveBleStateEnabled,
                observe_bluetooth_state_button
            )
        }

        scan_button.setOnClickListener {
            bleHelper.enabledScan(!bleHelper.isScanEnabled)
            changeEnabledButtonText(bleHelper.isScanEnabled, scan_button)
        }

        connect_button.setOnClickListener {
            bleHelper.enabledConnectBle(
                !bleHelper.isConnectBleEnabled,
                bleHelper.getBluetoothDevice(mac_address_edit_text.text.toString())
            )
            changeEnabledButtonText(bleHelper.isConnectBleEnabled, connect_button)
        }

        discover_service_button.setOnClickListener {
            bleHelper.enabledDiscoverService(!bleHelper.isDiscoverServiceEnabled)
            changeEnabledButtonText(bleHelper.isDiscoverServiceEnabled, discover_service_button)
        }

        read_characteristic_button.setOnClickListener {
            bleHelper.enabledReadBleCharacteristicValue(!bleHelper.isReadCharacteristicEnabled)
            changeEnabledButtonText(
                bleHelper.isReadCharacteristicEnabled,
                read_characteristic_button
            )
        }

        bonded_button.setOnClickListener {
            val bluetoothDevice =
                bleHelper.getBleDevice(mac_address_edit_text.text.toString())?.bluetoothDevice
            if (bluetoothDevice != null)
                bluetoothHelper.bondDevice(bluetoothDevice)
        }

        connect_a2dp_button.setOnClickListener {
            val bluetoothDevice =
                bleHelper.getBleDevice(mac_address_edit_text.text.toString())?.bluetoothDevice
            if (bluetoothDevice != null)
                bluetoothHelper.connectA2dp(bluetoothDevice)
        }

        listen_notification_button.setOnClickListener {
            bleHelper.enabledListenNotification(!bleHelper.isListenNotificationEnabled)
            changeEnabledButtonText(
                bleHelper.isListenNotificationEnabled,
                listen_notification_button
            )
        }

        auto_connect_button.setOnClickListener {
            bluetoothManager.connectDevice(mac_address_edit_text.text.toString(), true)
        }

        refresh_button.setOnClickListener {
            bleHelper.enabledConnectBle(
                !bleHelper.isConnectBleEnabled,
                bleHelper.getBluetoothDevice(mac_address_edit_text.text.toString())
            )
        }

        scroll_2_view.viewTreeObserver.addOnGlobalLayoutListener {
            scroll_2_view.post {
                scroll_2_view.fullScroll(FOCUS_DOWN)
            }
        }

        clear_button.setOnClickListener {
            this.message = ""
            message_text_view.text = ""
        }
    }

    private fun grantPermissions() {
        var requestPermissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        rxPermissions
            .request(
                *requestPermissions
            )
            .subscribe { granted ->
                if ((!granted)) {
                    grantPermissions()
                }
            }
            .apply {
                grantPermissionsDisposable = this
            }
    }

    private fun appendMessageView(message: String) {
        val df = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentDateAndTime: String = df.format(Date())

        this.message = "${this.message}\n$currentDateAndTime:${message}"
        message_text_view.text = this.message
    }

    private fun changeEnabledButtonText(isEnabled: Boolean, button: Button) {
        if (isEnabled) {
            button.text = "DISABLED"
        } else {
            button.text = "ENABLED"
        }
    }

    override fun onResume() {
        super.onResume()
        bluetoothManager.registerReceivers()
    }

    override fun onPause() {
        super.onPause()
//        bluetoothManager.unregisterReceivers()
    }
}