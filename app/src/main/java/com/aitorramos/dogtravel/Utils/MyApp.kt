package com.aitorramos.dogtravel.Utils

import android.app.Application
import com.aitorramos.dogtravel.utils.MyShared

val preferences: MyShared? by lazy { MyApp.prefs }

class MyApp : Application(){

    companion object{
        var prefs: MyShared? = null
    }

    override fun onCreate() {
        super.onCreate()
        prefs = MyShared(applicationContext)
    }
}