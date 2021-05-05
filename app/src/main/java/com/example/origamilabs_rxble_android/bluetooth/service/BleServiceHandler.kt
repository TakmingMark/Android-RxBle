package com.example.origamilabs_rxble_android.bluetooth.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import timber.log.Timber

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

        const val INTENT_SERVICE_CHECK_SCAN_RUNNING_STATE =
            "intent_service_check_scan_running_state"
        const val INTENT_SERVICE_START_SCAN = "intent_service_start_scan"
        const val INTENT_SERVICE_STOP_SCAN = "intent_service_stop_scan"

        const val INTENT_SERVICE_CHECK_CONNECT_RUNNING_STATE =
            "intent_service_check_connect_running_state"
        const val INTENT_SERVICE_START_CONNECT = "intent_service_start_connect"
        const val INTENT_SERVICE_STOP_CONNECT = "intent_service_stop_connect"

        const val INTENT_SERVICE_CHECK_LISTEN_NOTIFICATION_RUNNING_STATE =
            "intent_service_check_listen_notification_running_state"
        const val INTENT_SERVICE_START_LISTEN_NOTIFICATION =
            "intent_service_start_listen_notification"
        const val INTENT_SERVICE_STOP_LISTEN_NOTIFICATION =
            "intent_service_stop_listen_notification"

        const val INTENT_SERVICE_CHECK_AUTO_CONNECT_RUNNING_STATE =
            "intent_service_check_auto_connect_running_state"
        const val INTENT_SERVICE_START_AUTO_CONNECT = "intent_service_start_auto_connect"
        const val INTENT_SERVICE_STOP_AUTO_CONNECT = "intent_service_stop_auto_connect"

        const val INTENT_SERVICE_HANDLER_RESPONSE_OBSERVE_BLUETOOTH_STATE_RUNNING_STATE =
            "intent_service_handler_response_observe_bluetooth_state_running_state"
        const val INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_SUCCESS =
            "intent_service_handler_observe_bluetooth_state_success"
        const val INTENT_SERVICE_HANDLER_OBSERVE_BLUETOOTH_STATE_FAILURE =
            "intent_service_handler_observe_bluetooth_state_failure"

        const val INTENT_SERVICE_HANDLER_RESPONSE_SCAN_RUNNING_STATE =
            "intent_service_handler_response_scan_running_state"
        const val INTENT_SERVICE_HANDLER_SCAN_SUCCESS = "intent_service_handler_scan_success"
        const val INTENT_SERVICE_HANDLER_SCAN_FAILURE = "intent_service_handler_scan_failure"

        const val INTENT_SERVICE_HANDLER_RESPONSE_CONNECT_RUNNING_STATE =
            "intent_service_handler_response_connect_running_state"
        const val INTENT_SERVICE_HANDLER_CONNECT_SUCCESS = "intent_service_handler_connect_success"
        const val INTENT_SERVICE_HANDLER_CONNECT_FAILURE = "intent_service_handler_connect_failure"

        const val INTENT_SERVICE_HANDLER_RESPONSE_LISTEN_NOTIFICATION_RUNNING_STATE =
            "intent_service_handler_response_listen_notification_running_state"
        const val INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_SUCCESS =
            "intent_service_handler_listen_notification"
        const val INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_FAILURE =
            "intent_service_handler_listen_notification_failure"

        const val INTENT_SERVICE_HANDLER_RESPONSE_AUTO_CONNECT_RUNNING_STATE =
            "intent_service_handler_response_auto_connect_running_state"
        const val INTENT_SERVICE_HANDLER_AUTO_CONNECT_SUCCESS =
            "intent_service_handler_auto_connect_success"
        const val INTENT_SERVICE_HANDLER_AUTO_CONNECT_FAILURE =
            "intent_service_handler_auto_connect_failure"

    }

    var isBound = false
        private set

    var isObserveBluetoothRunning = false
        private set
    var isScanRunning = false
        private set
    var isConnectRunning = false
        private set
    var isListenNotificationRunning = false
        private set

    var isAutoConnectRunning = false
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
            checkConnectState()
            checkListenNotificationState()
            checkAutoConnectState()

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

    fun clearService() {
        sendMessageToService(MSG_UNREGISTER_CLIENT)
        service = null
        isBound = false
        bleServiceConnectionListener.onDisconnected()
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

    fun checkScanRunningState() {
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

    fun checkConnectState() {
        sendMessageToService(
            MSG_SEND_VALUE,
            INTENT_SERVICE_CHECK_CONNECT_RUNNING_STATE
        )
    }

    fun startConnectDevice(macAddress: String) {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_START_CONNECT, macAddress)
    }

    fun stopConnectDevice() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_STOP_CONNECT)
    }

    fun checkListenNotificationState() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_CHECK_LISTEN_NOTIFICATION_RUNNING_STATE)
    }

    fun startListenNotification() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_START_LISTEN_NOTIFICATION)
    }

    fun stopListenNotification() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_STOP_LISTEN_NOTIFICATION)
    }

    fun checkAutoConnectState() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_CHECK_AUTO_CONNECT_RUNNING_STATE)
    }

    fun startAutoConnect(macAddress: String) {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_START_AUTO_CONNECT, macAddress)
    }

    fun stopAutoConnect() {
        sendMessageToService(MSG_SEND_VALUE, INTENT_SERVICE_STOP_AUTO_CONNECT)
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
                            INTENT_SERVICE_HANDLER_RESPONSE_SCAN_RUNNING_STATE -> {
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
                            INTENT_SERVICE_HANDLER_RESPONSE_CONNECT_RUNNING_STATE -> {
                                isConnectRunning = bundle.getBoolean(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                )
                                bleServiceConnectionListener.onCheckConnectRunning(
                                    isConnectRunning
                                )
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
                            INTENT_SERVICE_HANDLER_RESPONSE_LISTEN_NOTIFICATION_RUNNING_STATE -> {
                                isListenNotificationRunning = bundle.getBoolean(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                )
                                bleServiceConnectionListener.onCheckListenNotificationRunning(
                                    isListenNotificationRunning
                                )
                            }
                            INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_SUCCESS -> {
                                val command = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                bleServiceConnectionListener.onListenNotificationSuccess(command)
                            }
                            INTENT_SERVICE_HANDLER_LISTEN_NOTIFICATION_FAILURE -> {
                                val error = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                bleServiceConnectionListener.onListenNotificationFailure(error)
                            }
                            INTENT_SERVICE_HANDLER_RESPONSE_AUTO_CONNECT_RUNNING_STATE -> {
                                isAutoConnectRunning = bundle.getBoolean(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                )
                                bleServiceConnectionListener.onAutoConnectRunning(
                                    isAutoConnectRunning
                                )
                            }
                            INTENT_SERVICE_HANDLER_AUTO_CONNECT_SUCCESS -> {
                                bleServiceConnectionListener.onAutoConnectSuccess()
                            }
                            INTENT_SERVICE_HANDLER_AUTO_CONNECT_FAILURE -> {
                                val error = bundle.getString(
                                    BUNDLE_SERVICE_EXTERNAL_VALUE_KEY
                                ) ?: return
                                bleServiceConnectionListener.onAutoConnectFailure(error)
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

        fun onCheckConnectRunning(isRunning: Boolean)
        fun onConnectSuccess(macAddress: String)
        fun onConnectFailure(error: String)

        fun onCheckListenNotificationRunning(isRunning: Boolean)
        fun onListenNotificationSuccess(command: String)
        fun onListenNotificationFailure(error: String)

        fun onAutoConnectRunning(isRunning: Boolean)
        fun onAutoConnectSuccess()
        fun onAutoConnectFailure(error: String)
    }
}