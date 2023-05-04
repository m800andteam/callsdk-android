package com.m800.sdk.core.demo

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.m800.sdk.core.M800BaseManager

private const val TYPE = "type"
private const val C_IN = "c_in"

class FCMService: FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("TEST", "123 FCM message received: ${message?.data}")
        Log.d("TEST", "Noti body: ${message?.notification?.body}")
        Log.d("TEST", "Noti body: ${message?.notification?.clickAction}")

        val sdk = M800BaseManager.getInstance(getString(R.string.service_id)) ?: return
        val map = message?.data ?: return

        sdk.getNotificationManager().handlePushNotification(map)

        var type: String? = null
        if (message.data?.containsKey(TYPE) == true) {
            type = message.data?.get(TYPE)
        }

        if (!(application as MainApplication).isActivityInForeground &&
                type?.equals(C_IN) == true) {
            Log.d("TestMainActivity", "startActivity(intent)")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}