package com.twitter.network

import android.content.Context
import com.github.pwittchen.reactivenetwork.library.ConnectivityStatus
import rx.Observable

interface NetworkApi {
    fun isConnectedToInternet(context: Context): Boolean

    fun observeConnectivity(context: Context): Observable<ConnectivityStatus>
}
