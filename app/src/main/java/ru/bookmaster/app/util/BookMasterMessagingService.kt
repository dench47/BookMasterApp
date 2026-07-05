package ru.bookmaster.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BookMasterMessagingService : FirebaseMessagingService() {

    companion object {
        const val PREFS_NAME = "app_prefs"
        const val KEY_NEW_EVENTS_COUNT = "new_events_count"
        const val KEY_CANCELLED_COUNT = "cancelled_by_client_count"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit { putString("fcm_token", token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val type = message.data["type"] ?: ""

        when (type) {
            "CLIENT_CANCELLED" -> {
                val currentCount = prefs.getInt(KEY_CANCELLED_COUNT, 0)
                prefs.edit { putInt(KEY_CANCELLED_COUNT, currentCount + 1) }
            }
            else -> {
                val currentCount = prefs.getInt(KEY_NEW_EVENTS_COUNT, 0)
                prefs.edit { putInt(KEY_NEW_EVENTS_COUNT, currentCount + 1) }
            }
        }

        val title = message.data["title"] ?: "BookMaster"
        val body = message.data["body"] ?: "Новое событие"
        showNotification(title, body)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "bookmaster_new_appointment",
            "Новые записи",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Уведомления о новых записях клиентов"
            enableVibration(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, body: String) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit { putBoolean("show_pending_sheet", true) }

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = android.app.Notification.Builder(this, "bookmaster_new_appointment")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}