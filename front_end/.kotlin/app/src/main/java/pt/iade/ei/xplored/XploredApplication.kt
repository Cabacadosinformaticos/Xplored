package pt.iade.ei.xplored

import android.app.Application
import android.content.Context

class XploredApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // FIX: Initialize this immediately before doing anything else
        instance = this
    }

    companion object {
        lateinit var instance: XploredApplication
            private set

        // Helper to prevent crashes if accessed too early
        fun getContext(): Context {
            return instance.applicationContext
        }
    }
}