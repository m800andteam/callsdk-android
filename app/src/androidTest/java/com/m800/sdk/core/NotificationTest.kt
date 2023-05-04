package com.m800.sdk.core

import android.content.Context
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.m800.sdk.core.error.M800ApiErrorException
import com.m800.sdk.core.noti.M800Notification
import com.m800.sdk.core.noti.M800NotificationListener
import com.m800.sdk.core.noti.M800NotificationTokenType
import com.m800.sdk.core.presence.M800PresenceListener
import com.m800.sdk.core.util.deferize
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.create
import java.util.*

private const val SERVICE_ID = "lc-int-1.m800.com"
private const val USER_NAME = "jerryctt@gmail.com"
private const val USER_PASSWORD = "VOnslkw0821"
private const val TYPE = "my-type"

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NotificationTest {
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getTargetContext()
        val pref = context.getSharedPreferences("M800Pref-$SERVICE_ID", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.clear()
        editor.apply()
    }

    @Test(timeout = 600000)
    @LargeTest
    fun testOnlineNotification() = runBlocking {
        val provision = M800Provision(SERVICE_ID)
        val context = InstrumentationRegistry.getTargetContext()
        val sdk = M800Core.initialize(context, provision)
        var testResult: Boolean = false

        deferize<Unit> {
            sdk.getAuthenticationManager().login(USER_NAME, USER_PASSWORD) { result, throwable ->
                complete(Unit)
            }
        }.await()

        val fcmToken = getFcmToken()
        val eid = sdk.getAuthenticationManager().getCurrentUser()?.eId ?: throw Exception("No user eid")

        sdk.getNotificationManager().updatePushToken(M800NotificationTokenType.FIREBASE_CLOUD_MESSAGING, fcmToken) { _, _ -> }
        sdk.getNotificationManager().registerListener(TYPE, listener = object : M800NotificationListener {
            override fun onNewNotification(type: String, data: M800Notification) {
                Log.d("test", "Received notification $type: $data")
                testResult = type == TYPE
            }
        })

        deferize<Unit> {
            sdk.getPresenceManager().connect{ result, throwable ->
                if (throwable != null) {
                    throwable.printStackTrace()
                }
                complete(Unit)
            }
        }.await()

        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder().addNetworkInterceptor(logger).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://kube-worker.cloud.m800.com:30311")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()

        val internalService: InternalService = retrofit.create()

        val bodyStr = with(JSONObject()) {
            put("mode", 0)
            put("content", "This is a content")
            put("package", "com.m800.sdk.core.demo")
            put("badge", 1)
            put("type", TYPE)
            put("data", with(JSONObject()) {
                put("timestamp", Date().time.toString())
            })
        }

        val body = RequestBody.create(MediaType.parse("application/json"), bodyStr.toString())
        val result = internalService.sendNotification(eid, body).await()


        delay(5000)

        assertTrue(testResult)
    }

    private suspend fun getFcmToken(): String {
        return deferize<String> {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("test", "getInstanceId failed", task.exception)
                        completeExceptionally(task.exception!!)
                        return@OnCompleteListener
                    }
                    complete(task.result?.token ?: "")
                })
        }.await()
    }
}