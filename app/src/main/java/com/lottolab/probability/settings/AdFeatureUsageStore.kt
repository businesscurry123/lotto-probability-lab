package com.lottolab.probability.settings

import android.content.Context
import java.time.LocalDate
import java.time.ZoneId

data class TodayDrawUsage(
    val date: String,
    val usedCount: Int,
    val shownKeys: Set<String>,
) {
    val remainingUses: Int
        get() = (4 - usedCount).coerceAtLeast(0)
}

object AdFeatureUsageStore {
    private const val PREF_NAME = "ad_feature_usage"
    private const val TODAY_DRAW_PREFIX = "today_draw"
    private val zoneId = ZoneId.of("Asia/Seoul")

    fun todayKey(): String = LocalDate.now(zoneId).toString()

    fun readTodayDrawUsage(context: Context, date: String = todayKey()): TodayDrawUsage {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return TodayDrawUsage(
            date = date,
            usedCount = prefs.getInt("${TODAY_DRAW_PREFIX}_used_$date", 0),
            shownKeys = prefs.getStringSet("${TODAY_DRAW_PREFIX}_shown_$date", emptySet()).orEmpty(),
        )
    }

    fun claimTodayDrawCombination(
        context: Context,
        candidates: List<List<Int>>,
        date: String = todayKey(),
    ): List<Int>? {
        val usage = readTodayDrawUsage(context, date)
        if (usage.remainingUses <= 0) return null

        val next = candidates.firstOrNull { combinationKey(it) !in usage.shownKeys }
            ?: return null
        val nextKeys = usage.shownKeys + combinationKey(next)

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt("${TODAY_DRAW_PREFIX}_used_$date", usage.usedCount + 1)
            .putStringSet("${TODAY_DRAW_PREFIX}_shown_$date", nextKeys)
            .apply()

        return next
    }

    fun combinationKey(numbers: List<Int>): String =
        numbers.sorted().joinToString("-")
}
