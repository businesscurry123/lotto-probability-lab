package com.lottolab.probability.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lottolab.probability.R

class LottoNotificationReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE) ?: ReminderType.POST_DRAW.id
        val reminderType = ReminderType.entries.firstOrNull { it.id == type } ?: ReminderType.POST_DRAW

        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(reminderType.title)
            .setContentText(reminderType.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminderType.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (canPostNotifications(context)) {
            runCatching {
                NotificationManagerCompat.from(context).notify(reminderType.notificationId, notification)
            }
        }

        LottoNotificationScheduler.scheduleNextIfEnabled(context, reminderType)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "로또 기록 알림",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "저장 번호 확인과 회차 분석 리포트 알림"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val CHANNEL_ID = "lotto_record_updates"
        const val EXTRA_TYPE = "extra_reminder_type"
    }
}
