package com.example.origamilabs_rxble_android.bluetooth.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
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
        const val BUNDLE_SERVICE_HANDLER_EXTERNAL_VALUE_KEY =
            "bundle_send_handler_external_value_key"

        const val INTENT_SERVICE_CHECK_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE =
            "intent_service_check_observe_bluetooth_state_running_state"
        const val INTENT_SERVICE_START_OBSERVE_BLUETOOTH_STATE =
            "intent_service_start_observe_bluetooth_state"
        const val INTENT_SERVICE_STOP_OBSERVE_BLUETOOTH_STATE =
            "intent_service_stop_observe_bluetooth_state"
        const val INTENT_SERVICE_CHECK_SCAN_RUNNING_STATE="intent_service_check_scan_running_state"
        const val INTENT_SERVICE_START_SCAN = "intent_service_start_scan"
        const val INTENT_SERVICE_STOP_SCAN = "intent_service_stop_scan"
        const val INTENT_SERVICE_START_CONNECT = "intent_service_start_connect"
        const val INTENT_SERVICE_STOP_CONNECT = "intent_service_stop_connect"

        const val INTENT_SERVICE_HANDLER_RESPONSE_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE =
            "intent_service_handler_response_observe_bluetooth_state_running_state"
        const val INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_SUCCESS =
            "intent_service_handler_observe_bluetooth_state_success"
        const val INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_FAILURE =
            "intent_service_handler_observe_bluetooth_state_failure"
        const val INTENT_SERVICE_HANDLER_RESPONSE_SCAN_RUNNING_STATE="intent_service_handler_response_scan_running_state"
        const val INTENT_SERVICE_HANDLER_SCAN_SUCCESS = "intent_service_handler_scan_success"
        const val INTENT_SERVICE_HANDLER_SCAN_FAILURE = "intent_service_handler_scan_failure"

        const val INTENT_SERVICE_HANDLER_CONNECT_SUCCESS = "intent_service_handler_connect_success"
        const val INTENT_SERVICE_HANDLER_CONNECT_FAILURE = "intent_service_handler_connect_failure"

        const val INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_SUCCESS =
            "intent_service_handler_listen_notification"

    }

    var isBound = false
        private set

    var isObserveBluetoothRunning = false
        private set
    var isScanRunning = false
        private set

    private val messenger = Messenger(BleServiceHandlerMessageHandler(looper))

    private var service: Messenger? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            service = Messenger(iBinder)
            var intent = Intent(
                context,
                BleService::class.java
            )
            context.startService(intent)
            sendMessageToService(MSG_REGISTER_CLIENT)
            checkObserveBluetoothRunningState()
            checkScanRunningState()

            isBound = true
            bleServiceConnectionListener.onConnected()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            service = null
            isBound = false
            bleServiceConnectionListener.onDisconnected()
        }
    }

    fun getServiceConnection(): ServiceConnection {
        return serviceConnection
    }

    fun checkObserveBluetoothRunningState() {
        sendMessageToService(
            MSG_SEND_VALUE,
            INTENT_SERVICE_CHECK_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE
        )
    }

    fun startObserveBluetoothState() {
        sendMessageToService(
            MSG_SEND_VALUE,
            INTENT_SERVICE_START_OBSERVE_BLUETOOTH_STATE
        )
    }

    fun stopObserveBluetoothState() {
        sendMessageToService(
            MSG_SEND_VALUE,
            INTENT_SERVICE_STOP_OBSERVE_BLUETOOTH_STATE
        )
    }

    fun checkScanRunningState(){
        sendMessageToService(
            MSG_SEND_VALUE,
            INTENT_SERVICE_CHECK_SCAN_RUNNING_STATE
        )
    }

    fun startScanDevice() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_START_SCAN)
    }

    fun stopScanDevice() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_STOP_SCAN)
    }

    fun startConnectDevice(macAddress: String) {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_START_CONNECT, macAddress)
    }

    fun stopConnectDevice() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_STOP_CONNECT)
    }

    private fun sendMessageToService(msgNumber: Int) {
        try {
            val msg = Message.obtain(null, msgNumber)
            msg.replyTo = messenger
            service?.send(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMessageToService(msgNumber: Int, msgKey: String?) {
        try {
            val msg = Message.obtain(null, msgNumber)
            val bundle = Bundle()
            bundle.putString(BUNDLE_SERVICE_MESSAGE_KEY, msgKey)
            msg.data = bundle
            msg.replyTo = messenger
            service?.send(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMessageToService(msgNumber: Int, msgKey: String, externalValue: String) {
        try {
            val msg = Message.obtain(null, msgNumber)
            val bundle = Bundle()
            bundle.putString(BUNDLE_SERVICE_MESSAGE_KEY, msgKey)
            bundle.putString(BUNDLE_SERVICE_EXTERNAL_VALUE_KEY, externalValue)
            msg.data = bundle
            msg.replyTo = messenger
            service?.send(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class BleServiceHandlerMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Timber.d("msg.what:${msg.what}")
            when (msg.what) {
                MSG_SEND_VALUE -> {
                    val bundle = msg.data
                    if (bundle != null) {
                        val messageKey = bundle.getString(BUNDLE_SERVICE_MESSAGE_KEY)
                        Timber.d("messageKey:$messageKey")
                        when (messageKey) {
                            INTENT_SERVICE_HANDLER_RESPONSE_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE -> {
                                isObserveBluetoothRunning = bundle.getBoolean(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                )
                                bleServiceConnectionListener.onCheckObserveBluetoothStateRunning(
                                    isObserveBluetoothRunning
                                )
                            }
                            INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_SUCCESS -> {
                                val state =
                                    bundle.getString(BUNDLE_SERVICE_EXTERNAL_VALUE_KEY) ?: ""
                                bleServiceConnectionListener.onObserveBluetoothStateSuccess(state)
                            }
                            INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_FAILURE -> {
                                val error =
                                    bundle.getString(BUNDLE_SERVICE_EXTERNAL_VALUE_KEY) ?: ""
                                bleServiceConnectionListener.onObserveBluetoothStateFailure(error)
                            }
                            INTENT_SERVICE_HANDLER_RESPONSE_SCAN_RUNNING_STATE->{
                                isScanRunning = bundle.getBoolean(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                )
                                bleServiceConnectionListener.onCheckScanRunning(
                                    isScanRunning
                                )
                            }
                            INTENT_SERVICE_HANDLER_SCAN_SUCCESS -> {
                                val macAddress = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                bleServiceConnectionListener.onScanSuccess(macAddress)
                            }
                            INTENT_SERVICE_HANDLER_SCAN_FAILURE -> {
                                val error = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                bleServiceConnectionListener.onScanSuccess(error)
                            }
                            INTENT_SERVICE_HANDLER_CONNECT_SUCCESS -> {
                                val macAddress = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                bleServiceConnectionListener.onConnectSuccess(macAddress)
                            }
                            INTENT_SERVICE_HANDLER_CONNECT_FAILURE -> {
                                val error = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                bleServiceConnectionListener.onConnectFailure(error)
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

    interface BleServiceConnectionListener {
        fun onConnected()
        fun onDisconnected()

        fun onCheckObserveBluetoothStateRunning(isRunning: Boolean)
        fun onObserveBluetoothStateSuccess(state: String)
        fun onObserveBluetoothStateFailure(error: String)

        fun onCheckScanRunning(isRunning: Boolean)
        fun onScanSuccess(macAddress: String)
        fun onScanFailure(error: String)

        fun onConnectSuccess(macAddress: String)
        fun onConnectFailure(error: String)
    }
}