package com.lottolab.probability.settings

import android.content.Context
import com.lottolab.probability.domain.SlipQrDisplayMode

object SlipQrSettingsStore {
    private const val PREF_NAME = "slip_qr_settings"
    private const val KEY_DISPLAY_MODE = "slip_qr_display_mode"

    fun readDisplayMode(context: Context): SlipQrDisplayMode {
        val storedValue = context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DISPLAY_MODE, null)

        return SlipQrDisplayMode.entries.firstOrNull { it.name == storedValue }
            ?: SlipQrDisplayMode.GROUP_BY_FIVE
    }

    fun saveDisplayMode(context: Context, mode: SlipQrDisplayMode) {
        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DISPLAY_MODE, mode.name)
            .apply()
    }
}
