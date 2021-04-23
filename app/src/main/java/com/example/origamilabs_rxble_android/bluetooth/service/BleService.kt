package com.example.origamilabs_rxble_android.bluetooth.service

import android.app.Service
import android.content.Intent
import android.os.*
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManager
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManagerListener
import com.example.origamilabs_rxble_android.BuildConfig
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.BUNDLE_SERVICE_MESSAGE_KEY
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_CONNECT_FAILURE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_CONNECT_SUCCESS
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_START_CONNECT
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_SCAN_SUCCESS
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_SUCCESS
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_SCAN_FAILURE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_START_SCAN
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_STOP_SCAN
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.MSG_REGISTER_CLIENT
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.MSG_SEND_VALUE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.MSG_UNREGISTER_CLIENT
import io.reactivex.Observable
import timber.log.Timber
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class BleService : Service() {
    private val messenger = Messenger(BleServiceMessageHandler(Looper.myLooper()!!))
    private val clients = ArrayList<Messenger>()

    private val bluetoothManager = BluetoothManager(this)
    private val notificationHelper = NotificationHelper(this)

    private val bluetoothManagerListener = object : BluetoothManagerListener() {
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
            clients.forEach { client ->
                val message = Message.obtain(null, MSG_SEND_VALUE)
                val bundle = Bundle()
                bundle.putInt(INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_SUCCESS, value)
                message.data = bundle
                client.send(message)
            }
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
    }

    override fun onBind(p0: Intent?): IBinder? {
        Timber.d("onBind()")
        return messenger.binder
    }

    private fun startScanDevice() {
        bluetoothManager.startScanDevice()
    }

    private fun stopScanDevice() {
        bluetoothManager.stopScanDevice()
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
                            INTENT_SERVICE_START_SCAN -> {
                                startScanDevice()
                            }
                            INTENT_SERVICE_STOP_SCAN -> {
                                stopScanDevice()
                            }
                            INTENT_SERVICE_START_CONNECT -> {
                                val macAddress = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                )
                                connectDevice(macAddress)
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