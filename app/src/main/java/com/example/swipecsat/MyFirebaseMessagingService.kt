package com.example.swipecsat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import kotlin.random.Random

const val CHANNEL_ID = "channel"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "description"
        }

        val nManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nManager.createNotificationChannel(channel)

        val pollIntent = Intent(this, MainActivity::class.java)
//            .apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            }

        val productName = message.data["product_name"] ?: ""

        pollIntent.putExtra("productName", productName)
//        pollIntent.putExtra("test", message.data.toString())

        val pendingIntent = PendingIntent.getActivity(this, 0, pollIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val nBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(productName)
            .setContentText("Пройдите небольшой опрос из 3 вопросов и получите приз!")
            .setSmallIcon(R.drawable.notification_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nManager.notify(Random.nextInt(), nBuilder)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoroutineScope(Dispatchers.IO).launch {
            val sp = getSharedPreferences("regData", MODE_PRIVATE)

            val client = okhttp3.OkHttpClient()

            val body = okhttp3.RequestBody
                .create("application/json".toMediaType(),
                    "{\"id\": \"$token\", \"gender\": \"${sp.getString("gender", "")}\", \"age\": ${sp.getInt("age", 0)}}")

            val request = okhttp3.Request.Builder()
                .url("http://158.160.98.205:8000/api/user/register")
                .post(body)
                .build()

            client.newCall(request).execute()
        }
    }
}