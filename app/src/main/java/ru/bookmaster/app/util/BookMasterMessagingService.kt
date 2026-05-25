package ru.bookmaster.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BookMasterMessagingService : FirebaseMessagingService() {

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
        // Не вызываем super, чтобы самим управлять уведомлением
        message.notification?.let {
            showHeadsUpNotification(it.title, it.body)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "bookmaster_new_appointment",
            "Новые записи",
            NotificationManager.IMPORTANCE_HIGH  // <-- ВЫСОКИЙ приоритет = всплывает!
        ).apply {
            description = "Уведомления о новых записях клиентов"
            enableVibration(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showHeadsUpNotification(title: String?, body: String?) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "home")
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = android.app.Notification.Builder(this, "bookmaster_new_appointment")
            .setContentTitle(title ?: "BookMaster")
            .setContentText(body ?: "")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}