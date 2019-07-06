package com.twitter.network

import android.content.Context
import com.github.pwittchen.reactivenetwork.library.ConnectivityStatus
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import rx.Observable

class NetworkApiProvider : NetworkApi {

    private val reactiveNetwork: ReactiveNetwork

    init {
        this.reactiveNetwork = ReactiveNetwork()
    }

    override fun isConnectedToInternet(context: Context): Boolean {
        val status = reactiveNetwork.getConnectivityStatus(context, true)
        val connectedToWifi = status == ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET
        val connectedToMobile = status == ConnectivityStatus.MOBILE_CONNECTED
        return connectedToWifi || connectedToMobile
    }

    override fun observeConnectivity(context: Context): Observable<ConnectivityStatus> {
        return ReactiveNetwork().enableInternetCheck().observeConnectivity(context)
    }
}
