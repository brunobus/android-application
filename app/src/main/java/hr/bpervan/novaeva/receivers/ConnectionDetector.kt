package hr.bpervan.novaeva.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import hr.bpervan.novaeva.EventPipelines

/**
 *
 */
class ConnectionDetector : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
        networkInfo ?: return
        if (networkInfo.state == NetworkInfo.State.CONNECTED) {
            EventPipelines.connectedToNetwork.onNext(Unit)
        }
    }
}