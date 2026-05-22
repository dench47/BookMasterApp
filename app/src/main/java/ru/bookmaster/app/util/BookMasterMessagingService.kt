package ru.bookmaster.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
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
            .edit().putString("fcm_token", token).apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Не вызываем super, чтобы самим управлять уведомлением
        message.notification?.let {
            showHeadsUpNotification(it.title, it.body)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    }

    private fun showHeadsUpNotification(title: String?, body: String?) {
        val notification = android.app.Notification.Builder(this, "bookmaster_new_appointment")
            .setContentTitle(title ?: "BookMaster")
            .setContentText(body ?: "")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}