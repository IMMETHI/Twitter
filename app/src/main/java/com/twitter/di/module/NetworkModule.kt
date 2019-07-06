package com.twitter.di.module

import com.twitter.network.NetworkApi
import com.twitter.network.NetworkApiProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NetworkModule {
    @Provides
    @Singleton
    fun provideNetworkApi(): NetworkApi {
        return NetworkApiProvider()
    }
}
