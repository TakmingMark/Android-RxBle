package com.example.origamilabs_rxble_android.bluetooth.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import com.example.origamilabs_rxble_android.bluetooth.manager.BluetoothManagerListener
import timber.log.Timber
import java.lang.Exception

class BleServiceHandler(
    private val context: Context,
    private val bleServiceConnectionListener: BleServiceConnectionListener,
    looper: Looper
) {
    companion object {
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SEND_VALUE = 3

        const val BUNDLE_SERVICE_MESSAGE_KEY = "bundle_send_service_message_key"
        const val BUNDLE_SERVICE_EXTERNAL_VALUE_KEY = "bundle_send_service_external_value_key"

        const val BUNDLE_SERVICE_HANDLER_MESSAGE_KEY = "bundle_send_handler_message_key"
        const val BUNDLE_SERVICE_HANDLER_EXTERNAL_VALUE_KEY = "bundle_send_handler_external_value_key"

        const val INTENT_SERVICE_SCAN_DEVICE = "intent_scan_device"
        const val INTENT_SERVICE_CONNECT_DEVICE = "connect_device"
        const val INTENT_SERVICE_RECEIVED_BLE_VALUE = "intent_received_ble_value"

        const val INTENT_SERVICE_HANDLER_DISCOVER_DEVICE_MAC_ADDRESS = "intent_discovery_device_MAC_ADDRESS"
    }

    private val messenger = Messenger(BleServiceHandlerMessageHandler(looper))

    //    private var service: BleService? = null
    private var service: Messenger? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            service = Messenger(iBinder)
            var intent = Intent(
                context,
                BleService::class.java
            )
            context.startService(intent)

            try {
                val msg = Message.obtain(null, MSG_REGISTER_CLIENT)
                msg.replyTo = messenger
                service?.send(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            bleServiceConnectionListener.onConnected()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
            bleServiceConnectionListener.onDisconnected()
        }
    }

    fun getServiceConnection(): ServiceConnection {
        return serviceConnection
    }

    fun setListener(bluetoothManagerListener: BluetoothManagerListener) {

//        service?.setListener(bluetoothManagerListener)
    }

    fun scanDevice() {
        sendMessageToService(INTENT_SERVICE_SCAN_DEVICE)
//        service?.scanDevice()
    }

    fun stopScanDevice() {
//        service?.stopScanDevice()
    }

    fun connectDevice(macAddress: String) {
        sendMessageToService(INTENT_SERVICE_CONNECT_DEVICE, macAddress)
//        service?.connectDevice(macAddress)
    }

    private fun sendMessageToService(message: String) {
        val bundle = Bundle()
        bundle.putString(BUNDLE_SERVICE_MESSAGE_KEY, message)
        val msg = Message.obtain()
        msg.data = bundle

        try {
            service?.send(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMessageToService(message: String, externalValue: String) {
        val bundle = Bundle()
        bundle.putString(BUNDLE_SERVICE_MESSAGE_KEY, message)
        bundle.putString(BUNDLE_SERVICE_EXTERNAL_VALUE_KEY, externalValue)
        val msg = Message.obtain()
        msg.data = bundle

        try {
            service?.send(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class BleServiceHandlerMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val bundle = msg.data
            if (bundle != null) {
                val messageKey = bundle.getString(BUNDLE_SERVICE_HANDLER_MESSAGE_KEY)
                when (messageKey) {
                    INTENT_SERVICE_HANDLER_DISCOVER_DEVICE_MAC_ADDRESS -> {
                        val discoverDeviceMacAddress = bundle.getString(
                            BUNDLE_SERVICE_HANDLER_EXTERNAL_VALUE_KEY
                        )
                        if (discoverDeviceMacAddress != null)
                            bleServiceConnectionListener.onDiscoverDeviceMacAddress(
                                discoverDeviceMacAddress
                            )
                    }
                }
                Timber.d("messageKey:$messageKey")
            }
        }
    }

    interface BleServiceConnectionListener {
        fun onConnected()
        fun onDisconnected()

        fun onDiscoverDeviceMacAddress(macAddress: String)
    }
}