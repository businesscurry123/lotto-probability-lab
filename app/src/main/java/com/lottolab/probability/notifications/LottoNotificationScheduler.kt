package com.lottolab.probability.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class ReminderType(
    val id: String,
    val label: String,
    val title: String,
    val message: String,
    val notificationId: Int,
    val requestCode: Int,
    val dayOfWeek: DayOfWeek,
    val hour: Int,
    val minute: Int,
) {
    PURCHASE_CHECK(
        id = "purchase_check",
        label = "토요일 19:00 번호 확인",
        title = "토요일 번호 확인",
        message = "구매 예정이라면 오늘 사용할 번호를 확인해보세요.",
        notificationId = 4100,
        requestCode = 5100,
        dayOfWeek = DayOfWeek.SATURDAY,
        hour = 19,
        minute = 0,
    ),
    PRE_DRAW(
        id = "pre_draw",
        label = "추첨 전날",
        title = "이번 회차 번호 기록 준비",
        message = "이번 회차에 사용할 번호를 저장해보세요.",
        notificationId = 4101,
        requestCode = 5101,
        dayOfWeek = DayOfWeek.FRIDAY,
        hour = 18,
        minute = 0,
    ),
    POST_DRAW(
        id = "post_draw",
        label = "추첨 직후",
        title = "저장 번호 적중 결과",
        message = "저장한 번호의 적중 결과가 나왔습니다.",
        notificationId = 4102,
        requestCode = 5102,
        dayOfWeek = DayOfWeek.SATURDAY,
        hour = 21,
        minute = 0,
    ),
    REPORT_READY(
        id = "report_ready",
        label = "추첨 직후 리포트",
        title = "이번 회차 분석 리포트",
        message = "이번 회차 분석 리포트가 업데이트되었습니다.",
        notificationId = 4103,
        requestCode = 5103,
        dayOfWeek = DayOfWeek.SATURDAY,
        hour = 21,
        minute = 10,
    ),
}

object LottoNotificationScheduler {
    private const val PREF_NAME = "lotto_notification_settings"
    private val zoneId = ZoneId.of("Asia/Seoul")

    fun isEnabled(context: Context, type: ReminderType): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(type.id, false)

    fun setEnabled(context: Context, type: ReminderType, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(type.id, enabled)
            .apply()
        if (enabled) {
            scheduleNext(context, type)
        } else {
            cancel(context, type)
        }
    }

    fun scheduleNextIfEnabled(context: Context, type: ReminderType) {
        if (isEnabled(context, type)) scheduleNext(context, type)
    }

    fun scheduleNext(context: Context, type: ReminderType) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val nextTimeMillis = nextOccurrence(type).atZone(zoneId).toInstant().toEpochMilli()
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            nextTimeMillis,
            pendingIntent(context, type),
        )
    }

    fun cancel(context: Context, type: ReminderType) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, type))
    }

    fun cancelLegacyNextDay(context: Context) {
        val intent = Intent(context, LottoNotificationReceiver::class.java).apply {
            putExtra(LottoNotificationReceiver.EXTRA_TYPE, "next_day")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            5103,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("next_day")
            .apply()
    }

    private fun nextOccurrence(type: ReminderType): LocalDateTime {
        val now = LocalDateTime.now(zoneId)
        var next = now
            .with(TemporalAdjusters.nextOrSame(type.dayOfWeek))
            .withHour(type.hour)
            .withMinute(type.minute)
            .withSecond(0)
            .withNano(0)
        if (!next.isAfter(now)) {
            next = next.plusWeeks(1)
        }
        return next
    }

    private fun pendingIntent(context: Context, type: ReminderType): PendingIntent {
        val intent = Intent(context, LottoNotificationReceiver::class.java).apply {
            putExtra(LottoNotificationReceiver.EXTRA_TYPE, type.id)
        }
        return PendingIntent.getBroadcast(
            context,
            type.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
