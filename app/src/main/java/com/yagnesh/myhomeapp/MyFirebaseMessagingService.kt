package com.yagnesh.myhomeapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "Message received from: ${message.from}")

        // Check if message contains notification payload
        message.notification?.let { notification ->
            val title = notification.title ?: "Alert"
            val body = notification.body ?: ""
            val type = message.data["type"] ?: "general"

            when (type) {
                "red_alert" -> {
                    showRedAlertNotification(title, body)
                }
                else -> {
                    showNotification(title, body)
                }
            }
        }

        // Check if message contains data payload
        if (message.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${message.data}")

            val type = message.data["type"]
            val title = message.data["title"] ?: "Alert"
            val body = message.data["body"] ?: ""

            when (type) {
                "red_alert" -> {
                    showRedAlertNotification(title, body)
                }
                "bill_reminder" -> {
                    showNotification(title, body)
                }
                else -> {
                    showNotification(title, body)
                }
            }
        }
    }

    private fun showRedAlertNotification(title: String, body: String) {
        val channelId = "red_alert_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Red Alert Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency alerts from family members"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setLights(android.graphics.Color.RED, 1000, 1000)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "general_notifications"
        val notificationId = System.currentTimeMillis().toInt()

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // Save token to Firestore if user is logged in
        if (UserSession.userId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(UserSession.userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token saved to Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error saving token", e)
                }
        }
    }
}