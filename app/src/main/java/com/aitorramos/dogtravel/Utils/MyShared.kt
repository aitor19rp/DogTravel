package com.aitorramos.dogtravel.utils

import android.content.Context

class MyShared(context: Context){

    private val fileName = "user_range"
    private val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    var range: Int
        get() = prefs.getInt("range", -1)
        set(value) = prefs.edit().putInt("range", value).apply()
}