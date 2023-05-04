package com.m800.sdk.core.demo

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean

class TestService : Service() {

    inner class TestBinder : Binder() {
        fun getService(): TestService {
            return this@TestService
        }
    }

    private val TAG = TestService::class.java.simpleName

    private val binder = TestBinder()

    private val isStopped = AtomicBoolean(true)

    private val thread = Thread(Runnable {
        while(!isStopped.get()) {
            Log.d(TAG, "M800TestService is running")
            Thread.sleep(10000)
        }
    })

    private val channelId = "10101"

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        if (isStopped.get()) {
            isStopped.set(false)
            val notification: Notification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel(channelId, TAG, importance)
                mChannel.description = TAG
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(mChannel)

                notification = Notification.Builder(application, channelId)
                        .setSmallIcon(R.drawable.ic_call_test)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(TAG).setAutoCancel(true).build()
            } else {
                notification = NotificationCompat.Builder(application, channelId)
                        .setSmallIcon(R.drawable.ic_call_test)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(TAG).setAutoCancel(true).build()
            }

            startForeground(1, notification)

            thread.start()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        isStopped.set(true)
        super.onDestroy()
    }

}