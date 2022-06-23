package com.example.storage_data.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs {

    companion object {
        fun SharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                FRAGMENT_SELECTION_PREFS,
                Context.MODE_PRIVATE
            )
        }

        fun setImagesPrefs(preferences: SharedPreferences, check: Boolean) {
            val editor: SharedPreferences.Editor? = preferences.edit()
            editor?.putBoolean(LONG_PRESS_IMAGES, check)
            editor?.putBoolean(LONG_PRESS_VIDEOS, false)
            editor?.putBoolean(LONG_PRESS_DOCS, false)

            editor?.apply()
        }

        fun getImagesPrefs(preferences: SharedPreferences): Boolean {
            return preferences.getBoolean(LONG_PRESS_IMAGES, false)
        }

        fun setVideosPrefs(preferences: SharedPreferences, check: Boolean) {
            val editor: SharedPreferences.Editor? = preferences.edit()
            editor?.putBoolean(LONG_PRESS_IMAGES, false)
            editor?.putBoolean(LONG_PRESS_VIDEOS, check)
            editor?.putBoolean(LONG_PRESS_DOCS, false)

            editor?.apply()
        }

        fun getVideosPrefs(preferences: SharedPreferences): Boolean {
            return preferences.getBoolean(LONG_PRESS_VIDEOS, false)
        }

        fun setDocsPrefs(preferences: SharedPreferences, check: Boolean) {
            val editor: SharedPreferences.Editor? = preferences.edit()
            editor?.putBoolean(LONG_PRESS_IMAGES, false)
            editor?.putBoolean(LONG_PRESS_VIDEOS, false)
            editor?.putBoolean(LONG_PRESS_DOCS, check)

            editor?.apply()
        }

        fun getDocsPrefs(preferences: SharedPreferences): Boolean {
            return preferences.getBoolean(LONG_PRESS_DOCS, false)
        }
    }
}