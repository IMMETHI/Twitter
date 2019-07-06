package com.twitter.di.module

import com.twitter.twitter.TwitterApi
import com.twitter.twitter.TwitterApiProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TwitterModule {
    @Provides
    @Singleton
    fun provideTwitterApi(): TwitterApi {
        return TwitterApiProvider()
    }
}
