package com.twitter.di

import com.twitter.di.module.NetworkModule
import com.twitter.di.module.TwitterModule
import com.twitter.ui.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(TwitterModule::class, NetworkModule::class))
interface ApplicationComponent {
    fun inject(mainActivity: MainActivity)
}
