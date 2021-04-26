package com.example.origamilabs_rxble_android.bluetooth.service

import android.app.Service
import android.content.Intent
import android.os.*
import com.example.origamilabs_rxble_android.BuildConfig
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManager
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManagerListener
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.BUNDLE_SERVICE_MESSAGE_KEY
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_CHECK_AUTO_CONNECT_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_CHECK_CONNECT_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_CHECK_LISTEN_NOTIFICATION_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_CHECK_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_CHECK_SCAN_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_CONNECT_FAILURE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_CONNECT_SUCCESS
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_FAILURE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_SUCCESS
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_FAILURE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_SUCCESS
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_RESPONSE_AUTO_CONNECT_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_RESPONSE_CONNECT_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_RESPONSE_LISTEN_NOTIFICATION_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_RESPONSE_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_RESPONSE_SCAN_RUNNING_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_SCAN_FAILURE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_SCAN_SUCCESS
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_START_AUTO_CONNECT
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_START_CONNECT
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_START_LISTEN_NOTIFICATION
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_START_OBSERVE_BLUETOOTH_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_START_SCAN
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_STOP_AUTO_CONNECT
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_STOP_CONNECT
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_STOP_LISTEN_NOTIFICATION
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_STOP_OBSERVE_BLUETOOTH_STATE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_STOP_SCAN
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.MSG_REGISTER_CLIENT
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.MSG_SEND_VALUE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.MSG_UNREGISTER_CLIENT
import io.reactivex.Observable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class BleService : Service() {
    private val messenger = Messenger(BleServiceMessageHandler(Looper.myLooper()!!))
    private val clients = ArrayList<Messenger>()

    private val bluetoothManager = BluetoothManager(this)
    private val notificationHelper = NotificationHelper(this)

    private val bluetoothManagerListener = object : BluetoothManagerListener() {
        override fun onObserveBleState(state: String) {
            sendMessageToServiceHandler(
                MSG_SEND_VALUE,
                INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_SUCCESS,
                state
            )
        }

        override fun onObserveBleStateError(error: String) {
            sendMessageToServiceHandler(
                MSG_SEND_VALUE,
                INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_FAILURE,
                error
            )
        }

        override fun onScan(macAddress: String, deviceName: String, rssi: Int) {
            sendMessageToServiceHandler(
                MSG_SEND_VALUE,
                INTENT_SERVICE_HANDLER_SCAN_SUCCESS,
                macAddress
            )
        }

        override fun onScanError(error: String) {
            sendMessageToServiceHandler(
                MSG_SEND_VALUE,
                INTENT_SERVICE_HANDLER_SCAN_FAILURE, error
            )
        }

        override fun onBleConnected(macAddress: String) {
            sendMessageToServiceHandler(
                MSG_SEND_VALUE,
                INTENT_SERVICE_HANDLER_CONNECT_SUCCESS,
                macAddress
            )
        }

        override fun onConnectBleError(error: String) {
            sendMessageToServiceHandler(
                MSG_SEND_VALUE,
                INTENT_SERVICE_HANDLER_CONNECT_FAILURE,
                error
            )
        }

        override fun onListenNotification(characteristicUuid: UUID, value: Int) {
            sendMessageToServiceHandler(
                MSG_SEND_VALUE,
                INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_SUCCESS, value.toString()
            )
        }

        override fun onListenNotificationError(error: String) {
            sendMessageToServiceHandler(
                MSG_SEND_VALUE,
                INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_FAILURE,
                error
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("onCreate()")
        startForeground(
            notificationHelper.getNotificationId(),
            notificationHelper.getNotification("Connected")
        )
        bluetoothManager.registerReceivers()
        bluetoothManager.bluetoothManagerListener = bluetoothManagerListener

        Observable.interval(1, TimeUnit.SECONDS).subscribe {
            Timber.d("it:$it")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand()")
        startForeground(
            notificationHelper.getNotificationId(),
            notificationHelper.getNotification("Connected")
        )
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy()")
        bluetoothManager.unregisterReceivers()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Timber.d("onBind()")
        return messenger.binder
    }

    private fun checkObserveBluetoothStateRunning() {
        val isRunning = bluetoothManager.isObserveBleStateRunning()
        sendMessageToServiceHandler(
            MSG_SEND_VALUE,
            INTENT_SERVICE_HANDLER_RESPONSE_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE,
            isRunning
        )
    }

    private fun startObserveBluetoothState() {
        bluetoothManager.startObserveBleState()
        checkObserveBluetoothStateRunning()
    }

    private fun stopObserveBluetoothState() {
        bluetoothManager.stopObserveBleState()
        checkObserveBluetoothStateRunning()
    }

    private fun checkScanRunning() {
        val isRunning = bluetoothManager.isScanDeviceRunning()
        sendMessageToServiceHandler(
            MSG_SEND_VALUE,
            INTENT_SERVICE_HANDLER_RESPONSE_SCAN_RUNNING_STATE,
            isRunning
        )
    }

    private fun startScanDevice() {
        bluetoothManager.startScanDevice()
        checkScanRunning()
    }

    private fun stopScanDevice() {
        bluetoothManager.stopScanDevice()
        checkScanRunning()
    }

    private fun checkConnectRunning() {
        val isRunning = bluetoothManager.isConnectDeviceRunning()
        Timber.d("checkConnectRunning:$isRunning")
        sendMessageToServiceHandler(
            MSG_SEND_VALUE,
            INTENT_SERVICE_HANDLER_RESPONSE_CONNECT_RUNNING_STATE,
            isRunning
        )
    }

    private fun startConnectDevice(macAddress: String) {
        bluetoothManager.startConnectDevice(macAddress)
        checkConnectRunning()
    }

    private fun stopConnectDevice() {
        bluetoothManager.stopConnectDevice()
        checkConnectRunning()
    }

    private fun checkListenNotificationRunning() {
        val isRunning = bluetoothManager.isListenNotificationRunning()
        Timber.d("checkListenNotificationRunning:$isRunning")
        sendMessageToServiceHandler(
            MSG_SEND_VALUE,
            INTENT_SERVICE_HANDLER_RESPONSE_LISTEN_NOTIFICATION_RUNNING_STATE,
            isRunning
        )
    }

    private fun startListenNotification() {
        bluetoothManager.startListenNotification()
        checkListenNotificationRunning()
    }

    private fun stopListenNotification() {
        bluetoothManager.stopListenNotification()
        checkListenNotificationRunning()
    }

    private fun checkAutoConnectRunning() {
        val isRunning = bluetoothManager.isAutoConnectRunning()
        Timber.d("checkAutoConnectRunning:$isRunning")
        sendMessageToServiceHandler(
            MSG_SEND_VALUE,
            INTENT_SERVICE_HANDLER_RESPONSE_AUTO_CONNECT_RUNNING_STATE,
            isRunning
        )
    }

    private fun startAutoConnect(macAddress: String) {
        bluetoothManager.startAutoConnect(macAddress)
        checkAutoConnectRunning()
    }

    private fun stopAutoConnect() {
        bluetoothManager.stopAutoConnect()
        checkAutoConnectRunning()
    }

    private fun connectDevice(macAddress: String?) {
        if (macAddress.isNullOrEmpty()) return
        bluetoothManager.connectDevice(macAddress)
    }

    private fun sendMessageToServiceHandler(msgNumber: Int, msgKey: String) {
        clients.forEach { client ->
            try {
                val msg = Message.obtain(null, msgNumber)
                val bundle = Bundle()
                bundle.putString(BUNDLE_SERVICE_MESSAGE_KEY, msgKey)
                msg.data = bundle
                msg.replyTo = messenger
                client.send(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendMessageToServiceHandler(msgNumber: Int, msgKey: String, externalValue: String) {
        clients.forEach { client ->
            try {
                val msg = Message.obtain(null, msgNumber)
                val bundle = Bundle()
                bundle.putString(BUNDLE_SERVICE_MESSAGE_KEY, msgKey)
                bundle.putString(BUNDLE_SERVICE_EXTERNAL_VALUE_KEY, externalValue)
                msg.data = bundle
                msg.replyTo = messenger
                client.send(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendMessageToServiceHandler(
        msgNumber: Int,
        msgKey: String,
        externalValue: Boolean
    ) {
        clients.forEach { client ->
            try {
                val msg = Message.obtain(null, msgNumber)
                val bundle = Bundle()
                bundle.putString(BUNDLE_SERVICE_MESSAGE_KEY, msgKey)
                bundle.putBoolean(BUNDLE_SERVICE_EXTERNAL_VALUE_KEY, externalValue)
                msg.data = bundle
                msg.replyTo = messenger
                client.send(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class BleServiceMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Timber.d("msg.what:${msg.what}")
            when (msg.what) {
                MSG_REGISTER_CLIENT -> {
                    clients.add(msg.replyTo)
                }
                MSG_UNREGISTER_CLIENT -> {
                    clients.remove(msg.replyTo)
                }
                MSG_SEND_VALUE -> {
                    val bundle = msg.data
                    if (bundle != null) {
                        val messageKey = bundle.getString(BUNDLE_SERVICE_MESSAGE_KEY)
                        Timber.d("messageKey:$messageKey")
                        when (messageKey) {
                            INTENT_SERVICE_CHECK_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE -> {
                                checkObserveBluetoothStateRunning()
                            }
                            INTENT_SERVICE_START_OBSERVE_BLUETOOTH_STATE -> {
                                startObserveBluetoothState()
                            }
                            INTENT_SERVICE_STOP_OBSERVE_BLUETOOTH_STATE -> {
                                stopObserveBluetoothState()
                            }
                            INTENT_SERVICE_CHECK_SCAN_RUNNING_STATE -> {
                                checkScanRunning()
                            }
                            INTENT_SERVICE_START_SCAN -> {
                                startScanDevice()
                            }
                            INTENT_SERVICE_STOP_SCAN -> {
                                stopScanDevice()
                            }
                            INTENT_SERVICE_CHECK_CONNECT_RUNNING_STATE -> {
                                checkConnectRunning()
                            }
                            INTENT_SERVICE_START_CONNECT -> {
                                val macAddress = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                startConnectDevice(macAddress)
                            }
                            INTENT_SERVICE_STOP_CONNECT -> {
                                stopConnectDevice()
                            }
                            INTENT_SERVICE_CHECK_LISTEN_NOTIFICATION_RUNNING_STATE -> {
                                checkListenNotificationRunning()
                            }
                            INTENT_SERVICE_START_LISTEN_NOTIFICATION -> {
                                startListenNotification()
                            }
                            INTENT_SERVICE_STOP_LISTEN_NOTIFICATION -> {
                                stopListenNotification()
                            }
                            INTENT_SERVICE_CHECK_AUTO_CONNECT_RUNNING_STATE -> {
                                checkAutoConnectRunning()
                            }
                            INTENT_SERVICE_START_AUTO_CONNECT -> {
                                val macAddress = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                startAutoConnect(macAddress)
                            }
                            INTENT_SERVICE_STOP_AUTO_CONNECT -> {
                                stopAutoConnect()
                            }
                        }
                    }
                }
                else -> {
                    super.handleMessage(msg)
                }
            }
        }
    }
}