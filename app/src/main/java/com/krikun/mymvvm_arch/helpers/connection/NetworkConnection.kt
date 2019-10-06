package com.krikun.mymvvm_arch.helpers.connection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NoInternetConnectionException : RuntimeException("No internet connection")

class NetworkConnection(private val context: Context, private val connectivityManager: ConnectivityManager) {

    /** Observes connection status changes */
    val connectionLiveData: LiveData<Boolean> = ConnectionChangesLiveData()

    /** @return Connection status */
    val connected: Boolean
        get() {
            var isConnected: Boolean? = false // Initial Value
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            if (activeNetwork != null && activeNetwork.isConnected)
                isConnected = true
            return isConnected ?: false
        }

    val wifiEnabled: Boolean
        get() {
            val info = getNetworkInfo(context)
            return if (info?.isConnected == true) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    connectivityManager
                        .getNetworkCapabilities(connectivityManager.activeNetwork)
                        .hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                } else {
                    info.type == ConnectivityManager.TYPE_WIFI
                }
            } else {
                false
            }
        }

    inline fun doOnlineOr(
        online: () -> Unit,
        offline: () -> Unit = {}
    ) {
        if (connected) {
            online()
        } else {
            offline()
        }
    }

    fun getNetworkSpeed(): NetworkSpeed {
        val info = getNetworkInfo(context)
        return if (info?.isConnected == true) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                when {
                    connectivityManager
                        .getNetworkCapabilities(connectivityManager.activeNetwork)
                        .hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        NetworkSpeed.FAST
                    }
                    connectivityManager
                        .getNetworkCapabilities(connectivityManager.activeNetwork)
                        .hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        getCellularConnectionSpeed(info.subtype)
                    }
                    else -> NetworkSpeed.SLOW
                }
            } else {
                when (info.type) {
                    ConnectivityManager.TYPE_WIFI -> NetworkSpeed.FAST
                    ConnectivityManager.TYPE_MOBILE -> getCellularConnectionSpeed(info.subtype)
                    else -> NetworkSpeed.SLOW
                }
            }

        } else {
            NetworkSpeed.NONE
        }
    }

    private fun getCellularConnectionSpeed(type: Int) = when (type) {
        TelephonyManager.NETWORK_TYPE_1xRTT -> NetworkSpeed.SLOW // ~ 50-100 kbps
        TelephonyManager.NETWORK_TYPE_CDMA -> NetworkSpeed.SLOW // ~ 14-64 kbps
        TelephonyManager.NETWORK_TYPE_EDGE -> NetworkSpeed.SLOW // ~ 50-100 kbps
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> NetworkSpeed.NORMAL // ~ 400-1000 kbps
        TelephonyManager.NETWORK_TYPE_EVDO_A -> NetworkSpeed.NORMAL // ~ 600-1400 kbps
        TelephonyManager.NETWORK_TYPE_GPRS -> NetworkSpeed.SLOW // ~ 100 kbps
        TelephonyManager.NETWORK_TYPE_HSDPA -> NetworkSpeed.NORMAL // ~ 2-14 Mbps
        TelephonyManager.NETWORK_TYPE_HSPA -> NetworkSpeed.NORMAL // ~ 700-1700 kbps
        TelephonyManager.NETWORK_TYPE_HSUPA -> NetworkSpeed.NORMAL // ~ 1-23 Mbps
        TelephonyManager.NETWORK_TYPE_UMTS -> NetworkSpeed.NORMAL // ~ 400-7000 kbps
        TelephonyManager.NETWORK_TYPE_EHRPD -> NetworkSpeed.NORMAL // ~ 1-2 Mbps
        TelephonyManager.NETWORK_TYPE_EVDO_B -> NetworkSpeed.NORMAL // ~ 5 Mbps
        TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkSpeed.NORMAL // ~ 10-20 Mbps
        TelephonyManager.NETWORK_TYPE_IDEN -> NetworkSpeed.SLOW // ~25 kbps
        TelephonyManager.NETWORK_TYPE_LTE -> NetworkSpeed.FAST // ~ 10+ Mbps
        // Unknown
        TelephonyManager.NETWORK_TYPE_UNKNOWN -> NetworkSpeed.SLOW
        else -> NetworkSpeed.SLOW
    }

    /**
     * Get the network info
     * @param context
     * @return
     */
    private fun getNetworkInfo(context: Context): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    enum class NetworkSpeed {
        NONE,
        SLOW,
        NORMAL,
        FAST
    }

    private inner class ConnectionChangesLiveData : MutableLiveData<Boolean>() {
        private val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                postValue(connected)
            }
        }

        init {
            postValue(connected)
        }

        override fun onActive() {
            super.onActive()
            context.registerReceiver(receiver, IntentFilter(CONNECTIVITY_CHANGE_ACTION))
        }

        override fun onInactive() {
            super.onInactive()
            context.unregisterReceiver(receiver)
        }
    }

    companion object {
        private const val CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    }
}

