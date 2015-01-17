package hr.bpervan.novaeva.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionChecker {
	
	/** 
	 * @author Branimir
	 * @param context - Calling activity context
	 * @return True if there is a way of reaching internet, no matter how, false otherwise
	 * */
	public static boolean hasConnection(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    	NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    	if (wifiNetwork != null && wifiNetwork.isConnected()) {
	      		return true;
	    	}

	    	NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    	if (mobileNetwork != null && mobileNetwork.isConnected()) {
	      		return true;
	    	}

	    	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    	if (activeNetwork != null && activeNetwork.isConnected()) {
	      		return true;
	    	}

	    	return false;
	}
}