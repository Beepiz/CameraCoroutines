package com.beepiz.cameracoroutines.sample

import android.app.Application
import timber.log.Timber

@Suppress("unused") // IDE bug: marked as unused even if referenced from AndroidManifest.xml
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
