package com.example.origamilabs_rxble_android.bluetooth.service

import android.app.Service
import android.content.Intent
import android.os.*
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManager
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManagerListener
import com.example.origamilabs_rxble_android.BuildConfig
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.BUNDLE_SERVICE_MESSAGE_KEY
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_CONNECT_DEVICE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_HANDLER_DISCOVER_DEVICE_MAC_ADDRESS
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_RECEIVED_BLE_VALUE
import com.example.origamilabs_rxble_android.bluetooth.service.BleServiceHandler.Companion.INTENT_SERVICE_SCAN_DEVICE
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
        override fun onScan(macAddress: String, deviceName: String, rssi: Int) {
            clients.forEach { client ->
                val message = Message.obtain(null, MSG_SEND_VALUE)
                val bundle = Bundle()
                bundle.putString(INTENT_SERVICE_HANDLER_DISCOVER_DEVICE_MAC_ADDRESS, macAddress)
                message.data = bundle
                client.send(message)
            }
        }

        override fun onListenNotification(characteristicUuid: UUID, value: Int) {
            clients.forEach { client ->
                val message = Message.obtain(null, MSG_SEND_VALUE)
                val bundle = Bundle()
                bundle.putInt(INTENT_SERVICE_RECEIVED_BLE_VALUE, value)
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
                        when (messageKey) {
                            INTENT_SERVICE_SCAN_DEVICE -> {
                                scanDevice()
                            }
                            INTENT_SERVICE_CONNECT_DEVICE -> {
                                val macAddress = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                )
                                connectDevice(macAddress)
                            }
                        }
                        Timber.d("messageKey:$messageKey")
                    }
                }
                else -> {
                    super.handleMessage(msg)
                }
            }
        }
    }

//    fun setListener(bluetoothManagerListener: BluetoothManagerListener) {
//        bluetoothManager.bluetoothManagerListener = bluetoothManagerListener
//    }

    fun connectDevice(macAddress: String?) {
        if (macAddress.isNullOrEmpty()) return
        bluetoothManager.connectDevice(macAddress)
    }

    fun scanDevice() {
        bluetoothManager.startScanDevice()
    }

    fun stopScanDevice() {
        bluetoothManager.stopScanDevice()
    }
}