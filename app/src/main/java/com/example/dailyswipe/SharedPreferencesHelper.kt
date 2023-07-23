package com.example.dailyswipe

import android.content.Context

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("my_shared_preferences", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean("first_launch", true)
    }

    fun setFirstLaunch(firstLaunch: Boolean) {
        sharedPreferences.edit().putBoolean("first_launch", firstLaunch).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("user_name", null)
    }

    fun setUserName(userName: String) {
        sharedPreferences.edit().putString("user_name", userName).apply()
    }

    fun getUserCountry(): String? {
        return sharedPreferences.getString("user_country", null)
    }

    fun setUserCountry(userCountry: String) {
        sharedPreferences.edit().putString("user_country", userCountry).apply()
    }

    fun clearCountry(){
        sharedPreferences.edit().clear().apply()
    }
}