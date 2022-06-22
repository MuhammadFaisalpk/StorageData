package com.example.storage_data.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs {
    companion object {
        private var SharedPrefName = "storage_data_preference"

        fun SharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                SharedPrefName,
                Context.MODE_PRIVATE
            )
        }

        fun setImagesPrefs(preferences: SharedPreferences, check: Boolean) {
            val editor: SharedPreferences.Editor? = preferences.edit()
            editor?.putBoolean("long_press_images", check)
            editor?.putBoolean("long_press_videos", false)
            editor?.putBoolean("long_press_docs", false)

            editor?.apply()
        }

        fun getImagesPrefs(preferences: SharedPreferences): Boolean {
            return preferences.getBoolean("long_press_images", false)
        }

        fun setVideosPrefs(preferences: SharedPreferences, check: Boolean) {
            val editor: SharedPreferences.Editor? = preferences.edit()
            editor?.putBoolean("long_press_images", false)
            editor?.putBoolean("long_press_videos", check)
            editor?.putBoolean("long_press_docs", false)

            editor?.apply()
        }

        fun getVideosPrefs(preferences: SharedPreferences): Boolean {
            return preferences.getBoolean("long_press_videos", false)
        }

        fun setDocsPrefs(preferences: SharedPreferences, check: Boolean) {
            val editor: SharedPreferences.Editor? = preferences.edit()
            editor?.putBoolean("long_press_images", false)
            editor?.putBoolean("long_press_videos", false)
            editor?.putBoolean("long_press_docs", check)

            editor?.apply()
        }

        fun getDocsPrefs(preferences: SharedPreferences): Boolean {
            return preferences.getBoolean("long_press_docs", false)
        }
    }
}