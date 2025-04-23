package com.example.chronosyne.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    fun saveRememberMeState(isChecked: Boolean) {
        sharedPreferences.edit().putBoolean("REMEMBER_ME", isChecked).apply()
    }

    fun isRemembered(): Boolean {
        return sharedPreferences.getBoolean("REMEMBER_ME", false)
    }
}