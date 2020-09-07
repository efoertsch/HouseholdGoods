package org.householdgoods.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.householdgoods.BuildConfig
import timber.log.Timber
import timber.log.Timber.DebugTree


@HiltAndroidApp
class HouseholdGoodsApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        // This will initialise Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

}