package com.example.origamilabs_rxble_android

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View.FOCUS_DOWN
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManager
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManagerListener
import com.example.origamilabs_rxble_android.bluetooth.service.BleService
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler
import com.example.origamilabs_rxble_android.test.service.TestService
import com.example.origamilabs_rxble_android.test.service.TestServiceHandler
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.part_of_main_ble.*
import kotlinx.android.synthetic.main.part_of_main_bluetooth.*
import kotlinx.android.synthetic.main.part_of_main_others.*
import kotlinx.android.synthetic.main.part_of_main_service.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var message = ""

    private val rxPermissions: RxPermissions by lazy {
        RxPermissions(this)
    }

    private val bluetoothManager: BluetoothManager by lazy {
        BluetoothManager(this)
    }

    private val bleServiceHandler: BleServiceHandler by lazy {
        BleServiceHandler(this, bleServiceConnectionListener, mainLooper)
    }

    private val testServiceHandler: TestServiceHandler by lazy {
        TestServiceHandler(this)
    }

    private var grantPermissionsDisposable: Disposable? = null

    private var bluetoothManagerListener: BluetoothManagerListener? = null

    private val bleServiceConnectionListener = object :
        BleServiceHandler.BleServiceConnectionListener {
        override fun onStarted() {
            appendMessageView("Ble Service started")

            changeEnabledButtonText(
                bleServiceHandler.isStart,
                start_service_button
            )
        }

        override fun onStopped() {
            appendMessageView("Ble Service stopped")

            changeEnabledButtonText(
                bleServiceHandler.isStart,
                start_service_button
            )
        }

        override fun onConnected() {
            appendMessageView("Ble Service connected")

            changeEnabledButtonText(
                bleServiceHandler.isBound,
                bind_service_button
            )
        }

        override fun onDisconnected() {
            appendMessageView("Ble Service disconnected")

            changeEnabledButtonText(
                bleServiceHandler.isBound,
                bind_service_button
            )
        }

        override fun onCheckObserveBluetoothStateRunning(isRunning: Boolean) {
            appendMessageView("onCheckObserveBluetoothStateRunning:$isRunning")

            changeEnabledButtonText(
                isRunning,
                service_observe_bluetooth_state_button
            )
        }

        override fun onObserveBluetoothStateSuccess(state: String) {
            appendMessageView("onObserveBluetoothStateSuccess:$state")
        }

        override fun onObserveBluetoothStateFailure(error: String) {
            appendMessageView("onObserveBluetoothStateFailure:$error")
        }

        override fun onCheckScanRunning(isRunning: Boolean) {
            appendMessageView("onCheckScanRunning:$isRunning")

            changeEnabledButtonText(
                isRunning,
                service_scan_button
            )
        }

        override fun onScanSuccess(macAddress: String) {
            appendMessageView("onScanSuccess: $macAddress")

//            if (mac_address_edit_text.text.toString() == macAddress) {
//                bleServiceHandler.stopScanDevice()
//                bleServiceHandler.startConnectDevice(macAddress)
//            }
        }

        override fun onScanFailure(error: String) {
            appendMessageView("onScanFailure: $error")
        }

        override fun onCheckConnectRunning(isRunning: Boolean) {
            appendMessageView("onCheckConnectRunning:$isRunning")

            changeEnabledButtonText(
                isRunning,
                service_connect_button
            )
        }

        override fun onConnectSuccess(macAddress: String) {
            appendMessageView("onConnectSuccess: $macAddress")
        }

        override fun onConnectFailure(error: String) {
            appendMessageView("onConnectFailure: $error")
        }

        override fun onCheckListenNotificationRunning(isRunning: Boolean) {
            appendMessageView("onCheckListenNotificationRunning:$isRunning")

            changeEnabledButtonText(
                isRunning,
                service_listen_notification_button
            )
        }

        override fun onListenNotificationSuccess(command: String) {
            appendMessageView("onListenNotificationSuccess: $command")
        }

        override fun onListenNotificationFailure(error: String) {
            appendMessageView("onListenNotificationFailure: $error")
        }

        override fun onAutoConnectRunning(isRunning: Boolean) {
            appendMessageView("onAutoConnectRunning: $isRunning")
            changeEnabledButtonText(
                isRunning,
                service_auto_connect_button
            )
        }

        override fun onAutoConnectSuccess() {
            appendMessageView("onAutoConnectSuccess")
        }

        override fun onAutoConnectFailure(error: String) {
            appendMessageView("onAutoConnectFailure:$error")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        grantPermissions()
        initBluetoothManagerListener()
        initBleViewListener()
        initBluetoothViewListener()
        initServiceViewListener()
        initOthersViewListener()
        initShowViewListener()
    }

    private fun initBleViewListener() {
        observe_bluetooth_state_button.setOnClickListener {
            when (bluetoothManager.isObserveBleStateRunning()) {
                true -> bluetoothManager.stopObserveBleState()
                false -> bluetoothManager.startObserveBleState()
            }

            changeEnabledButtonText(
                bluetoothManager.isObserveBleStateRunning(),
                observe_bluetooth_state_button
            )
        }

        scan_button.setOnClickListener {
            when (bluetoothManager.isScanDeviceRunning()) {
                true -> bluetoothManager.stopScanDevice()
                false -> bluetoothManager.startScanDevice()
            }
            changeEnabledButtonText(bluetoothManager.isScanDeviceRunning(), scan_button)
        }

        connect_button.setOnClickListener {

            when (bluetoothManager.isConnectDeviceRunning()) {
                true -> bluetoothManager.stopConnectDevice()
                false -> {
                    val macAddress = mac_address_edit_text.text.toString()
                    bluetoothManager.startConnectDevice(macAddress)
                }
            }
            changeEnabledButtonText(bluetoothManager.isConnectDeviceRunning(), connect_button)
        }

        discover_service_button.setOnClickListener {
            when (bluetoothManager.isDiscoverServiceRunning()) {
                true -> bluetoothManager.stopDiscoverService()
                false -> bluetoothManager.startDiscoverService()
            }
            changeEnabledButtonText(
                bluetoothManager.isDiscoverServiceRunning(),
                discover_service_button
            )
        }

        read_characteristic_button.setOnClickListener {
            when (bluetoothManager.isReadCharacteristicValueRunning()) {
                true -> bluetoothManager.stopReadCharacteristicValue()
                false -> bluetoothManager.startReadCharacteristicValue()
            }

            changeEnabledButtonText(
                bluetoothManager.isReadCharacteristicValueRunning(),
                read_characteristic_button
            )
        }

        listen_notification_button.setOnClickListener {
            when (bluetoothManager.isListenNotificationRunning()) {
                true -> bluetoothManager.stopListenNotification()
                false -> bluetoothManager.startListenNotification()
            }

            changeEnabledButtonText(
                bluetoothManager.isListenNotificationRunning(),
                listen_notification_button
            )
        }

        auto_connect_button.setOnClickListener {
            when (bluetoothManager.isAutoConnectRunning()) {
                true -> bluetoothManager.stopAutoConnect()
                false -> {
                    val macAddress = mac_address_edit_text.text.toString()
                    bluetoothManager.startAutoConnect(macAddress)
                }
            }

            changeEnabledButtonText(
                bluetoothManager.isAutoConnectRunning(),
                auto_connect_button
            )
        }
    }

    private fun initBluetoothViewListener() {
        bonded_button.setOnClickListener {
            val macAddress = mac_address_edit_text.text.toString()
            val device = bluetoothManager.getBluetoothDevice(macAddress)
            bluetoothManager.bondDevice(device)
        }

        connect_a2dp_button.setOnClickListener {
            val macAddress = mac_address_edit_text.text.toString()
            val device = bluetoothManager.getBluetoothDevice(macAddress)
            bluetoothManager.connectA2dp(device)
        }

        check_bond_state_button.setOnClickListener {
            bluetoothManager.printDeviceBondState(mac_address_edit_text.text.toString())
        }
    }


    private fun initServiceViewListener() {
        start_service_button.setOnClickListener {
            when (bleServiceHandler.isStart) {
                true -> bleServiceHandler.stopService()
                false -> bleServiceHandler.startService()
            }
        }

        bind_service_button.setOnClickListener {
            when (bleServiceHandler.isBound) {
                true -> unbindBleService()
                false -> bindBleService()
            }
        }

        service_observe_bluetooth_state_button.setOnClickListener {
            when (bleServiceHandler.isObserveBluetoothRunning) {
                true -> bleServiceHandler.stopObserveBluetoothState()
                false -> bleServiceHandler.startObserveBluetoothState()
            }
        }

        service_scan_button.setOnClickListener {
            when (bleServiceHandler.isScanRunning) {
                true -> bleServiceHandler.stopScanDevice()
                false -> bleServiceHandler.startScanDevice()
            }
        }

        service_connect_button.setOnClickListener {
            when (bleServiceHandler.isConnectRunning) {
                true -> bleServiceHandler.stopConnectDevice()
                false -> {
                    val macAddress = mac_address_edit_text.text.toString()
                    bleServiceHandler.startConnectDevice(macAddress)
                }
            }
        }

        service_listen_notification_button.setOnClickListener {
            when (bleServiceHandler.isListenNotificationRunning) {
                true -> bleServiceHandler.stopListenNotification()
                false -> bleServiceHandler.startListenNotification()
            }
        }

        service_auto_connect_button.setOnClickListener {
            when (bleServiceHandler.isAutoConnectRunning) {
                true -> bleServiceHandler.stopAutoConnect()
                false -> {
                    val macAddress = mac_address_edit_text.text.toString()
                    bleServiceHandler.startAutoConnect(macAddress)
                }
            }
        }
    }


    private fun initOthersViewListener() {
        crash_app_button.setOnClickListener {
            throw Exception("")
        }

        create_service_button.setOnClickListener {
            bindTestService()
        }

        destroy_service_button.setOnClickListener {
            unbindTestService()
        }
        update_service_button.setOnClickListener {
            updateTestService()
        }
    }

    private fun initShowViewListener() {
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
//        bluetoothManager.registerReceivers()
    }

    override fun onPause() {
        super.onPause()
//        bluetoothManager.unregisterReceivers()
    }

    private fun initBluetoothManagerListener() {
        if (bluetoothManagerListener == null) {
            bluetoothManagerListener = object : BluetoothManagerListener() {
                override fun onObserveBleState(state: String) {
                    appendMessageView("observeBleState:$state")
                }

                override fun onObserveBleStateError(error: String) {
                    appendMessageView("onObserveBleStateError:${error}")
                }

                override fun onScan(macAddress: String, deviceName: String, rssi: Int) {
                    appendMessageView("onScan $macAddress,$deviceName,$rssi")
                }

                override fun onScanError(error: String) {
                    appendMessageView("onScanError:$error")
                }

                override fun onBleConnected(macAddress: String) {
                    appendMessageView("onBleConnected $macAddress")
                }

                override fun onConnectBleError(error: String) {
                    appendMessageView("onConnectBleError:$error")
                }

                override fun onDiscoverBleService(serviceUuid: UUID) {
                    appendMessageView("onDiscoverBleService,serviceUuid $serviceUuid")
                }

                override fun onDiscoverBleService(
                    gattServiceUuid: UUID,
                    characteristicUuids: List<UUID>
                ) {
                    appendMessageView("onDiscoverBleService,gattServiceUuid $gattServiceUuid,characteristicUuids:${characteristicUuids}")
                }

                override fun onDiscoverBleServiceError(error: String) {
                    appendMessageView("onDiscoverBleServiceError:$error")
                }

                override fun onListenNotification(characteristicUuid: UUID, value: Int) {
                    appendMessageView("onListenNotification:$value")
                }

                override fun onListenNotificationError(error: String) {
                    appendMessageView("onListenNotificationError:$error")
                }

                override fun onAutoConnectSuccess() {
                    appendMessageView("onAutoConnectSuccess")
                }

                override fun onAutoConnectError(error: String) {
                    appendMessageView("onAutoConnectError$error")
                }
            }
            bluetoothManager.bluetoothManagerListener = this.bluetoothManagerListener
        }
    }

    private fun bindBleService() {
        bindService(
            Intent(this, BleService::class.java),
            bleServiceHandler.getServiceConnection(),
            Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindBleService() {
        unbindService(bleServiceHandler.getServiceConnection())
        bleServiceHandler.clearService()
    }

    private fun bindTestService() {
        bindService(
            Intent(this, TestService::class.java),
            testServiceHandler.getServiceConnection(),
            Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindTestService() {
        unbindService(testServiceHandler.getServiceConnection())
        testServiceHandler.clearService()
    }

    private fun updateTestService() {
        testServiceHandler.updateNotification()
    }
}