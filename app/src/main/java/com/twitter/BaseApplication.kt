package com.twitter

import android.app.Application


import com.twitter.di.ApplicationComponent
import com.twitter.di.DaggerApplicationComponent
import com.twitter.di.module.NetworkModule
import com.twitter.di.module.TwitterModule
import timber.log.Timber

class BaseApplication : Application() {
    var component: ApplicationComponent? = null
        private set

    override fun onCreate() {
        super.onCreate()
        buildApplicationComponent()
        plantLoggingTree()
    }

    private fun buildApplicationComponent() {
        component = DaggerApplicationComponent.builder()
                .twitterModule(TwitterModule())
                .networkModule(NetworkModule())
                .build()
    }

    private fun plantLoggingTree() {
        Timber.plant(Timber.DebugTree())

    }

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String, message: String, t: Throwable) {
            // implement crash reporting with Crashlytics, Bugsnag or whatever if necessary
        }
    }
}
