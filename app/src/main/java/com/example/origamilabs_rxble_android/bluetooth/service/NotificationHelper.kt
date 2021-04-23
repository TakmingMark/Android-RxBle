package com.example.origamilabs_rxble_android.bluetooth.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.origamilabs_rxble_android.R

class NotificationHelper(private val context: Context) {
    private val NOTIFICATION_ID = 1002
    private val CHANNEL_ID ="ble_service"
    private val CHANNEL_NAME = "ble foreground service"

    fun getNotification(contentText: String): Notification {
        val builder = NotificationCompat
            .Builder(
                context,
                CHANNEL_ID
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Please good to work")
            .setContentText(contentText)
            .setOngoing(true)
            .setShowWhen(false)

        //NotificationManager.IMPORTANCE_LOW, open and show notification on bar, can't popup toast, not alert sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.createNotificationChannel(mChannel)
        }
        return builder.build()
    }

    fun getNotificationId(): Int {
        return NOTIFICATION_ID
    }
}