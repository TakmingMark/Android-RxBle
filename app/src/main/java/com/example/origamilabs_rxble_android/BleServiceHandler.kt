package com.example.origamilabs_rxble_android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class BleServiceHandler(
    private val context: Context,
    bleServiceConnectionListener: BleServiceConnectionListener
) {
    private var service: BleService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            service = (iBinder as BleService.LocalBinder).getService()
            var intent = Intent(
                context,
                BleService::class.java
            )
            context.startService(intent)
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

    fun setListener(bluetoothManagerListener: BluetoothManagerListener){
        service?.setListener(bluetoothManagerListener)
    }

    fun connectDevice(macAddress: String){
        service?.connectDevice(macAddress)
    }

    interface BleServiceConnectionListener{
        fun onConnected()
        fun onDisconnected()
    }
}