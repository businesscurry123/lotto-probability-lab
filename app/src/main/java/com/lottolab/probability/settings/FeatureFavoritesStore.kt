package com.lottolab.probability.settings

import android.content.Context

object FeatureFavoritesStore {
    private const val PREF_NAME = "feature_favorites"
    private const val KEY_IDS = "favorite_feature_ids"

    fun readFavorites(context: Context): Set<String> =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_IDS, emptySet())
            .orEmpty()

    fun saveFavorites(context: Context, ids: Set<String>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_IDS, ids)
            .apply()
    }
}
