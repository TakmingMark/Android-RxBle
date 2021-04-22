package com.example.origamilabs_rxble_android

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import timber.log.Timber
import java.util.*

class BleService : Service() {
    private val bluetoothManager=BluetoothManager(this)
    private val notificationHelper=NotificationHelper(this)

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate()")
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

    private val binder = LocalBinder()
    override fun onBind(p0: Intent?): IBinder? {
        Timber.d("onBind()")
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleService {
            return this@BleService
        }
    }

    fun setListener(bluetoothManagerListener: BluetoothManagerListener){
        bluetoothManager.bluetoothManagerListener=bluetoothManagerListener
    }

    fun connectDevice(macAddress: String){
        bluetoothManager.connectDevice(macAddress,false)
    }

    fun scanDevice(){
        bluetoothManager.scanDevice()
    }

    fun stopScanDevice(){
        bluetoothManager.stopScanDevice()
    }
}