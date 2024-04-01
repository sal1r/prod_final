package com.example.swipecsat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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

        pollIntent.putExtra("items", "[\"fsfsd\", \"232435\", \"lk.jkuk\"]")

        val pendingIntent = PendingIntent.getActivity(this, 0, pollIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val nBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Тест")
            .setContentText("Бред всякий текст 1 2 выфвфвфваыаываикувыа")
            .setSmallIcon(R.drawable.notification_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .build()

        nManager.notify(Random.nextInt(), nBuilder)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}