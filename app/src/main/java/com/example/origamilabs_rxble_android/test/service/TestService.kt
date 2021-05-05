package com.example.origamilabs_rxble_android.test.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.origamilabs_rxble_android.bluetooth.service.NotificationHelper
import timber.log.Timber

class TestService : Service() {
    private val notificationHelper = NotificationHelper(this)
    var i = 0
    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate()")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand()")
        startForeground()
        return START_STICKY
    }

    private fun startForeground() {
        startForeground(
            notificationHelper.getNotificationId(),
            notificationHelper.getNotification("HELLO")
        )
    }

    fun updateNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            notificationHelper.getNotificationId(),
            notificationHelper.getNotification((i++).toString())
        )
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


    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("onUnbind()")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            stopForeground(notificationHelper.getNotificationId())
        } else {
            stopForeground(true)
        }
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService(): TestService {
            return this@TestService
        }
    }
}