package com.example.natureobserverapp

import android.app.Activity
import android.content.Context

// Singleton object for different Shared Preference functions
object SharedPreferencesFunctions {
    private const val sharedPrefFile = "sharedpreference"

    fun getSharedPreferenceIndexValue(
        activity: Activity,
        keyName: String,
        defaultValue: Int
    ): Int? {
        val sharedPreference =
            activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        return sharedPreference?.getInt(keyName, defaultValue)
    }

    fun putSharedPreferenceIndexValue(activity: Activity, keyName: String, value: Int) {
        val sharedPreference =
            activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putInt(keyName, value)
        editor.apply()
    }

    fun getSharedPreferenceStringSet(
        activity: Activity,
        keyName: String,
        defaultValue: HashSet<String>
    ): Set<String>? {
        val sharedPreference =
            activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        return sharedPreference?.getStringSet(keyName, defaultValue)
    }

    fun putSharedPreferenceStringSet(activity: Activity, keyName: String, value: HashSet<String>) {
        val sharedPreference =
            activity.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putStringSet(keyName, value)
        editor.apply()
    }
}