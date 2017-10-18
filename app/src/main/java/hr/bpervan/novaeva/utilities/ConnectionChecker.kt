package hr.bpervan.novaeva.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object ConnectionChecker {

    /**
     * @author Branimir
     * @param context - Calling activity context
     * @return True if there is a way of reaching internet, no matter how, false otherwise
     */
    fun hasConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiNetwork != null && wifiNetwork.isConnected) {
            return true
        }

        val mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (mobileNetwork != null && mobileNetwork.isConnected) {
            return true
        }

        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected

    }
}
