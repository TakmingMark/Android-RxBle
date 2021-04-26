package com.example.origamilabs_rxble_android.test.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.origamilabs_rxble_android.bluetooth.service.NotificationHelper
import timber.log.Timber

class TestService : Service() {
    private val notificationHelper = NotificationHelper(this)
    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate()")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand()")
        startForeground(
            notificationHelper.getNotificationId(),
            notificationHelper.getNotification("HELLO")
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
        fun getService(): TestService {
            return this@TestService
        }
    }
}