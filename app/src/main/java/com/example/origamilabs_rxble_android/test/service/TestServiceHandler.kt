package com.example.origamilabs_rxble_android.test.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import timber.log.Timber

class TestServiceHandler(private val context: Context) {
    private var service: TestService? = null


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
            Timber.d("onServiceConnected")
            service = (iBinder as TestService.LocalBinder).getService()
            var intent = Intent(
                context,
                TestService::class.java
            )
            context.startService(intent)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Timber.d("onServiceDisconnected")
            service = null
        }
    }

    fun clearService() {
        service = null
    }

    fun getServiceConnection(): ServiceConnection {
        return serviceConnection
    }
}