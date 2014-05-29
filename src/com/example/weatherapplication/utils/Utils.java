package com.example.weatherapplication.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

public class Utils {

	public static String CityName = null;
	public static String CountryCode = null;
	public static String CityLocation = null;

	/**
	 * @return isInternetAvailable
	 * 
	 *         Check for available Internet connections. WIFI, Mobile network or
	 *         any other connection. Return true if Internet connection is
	 *         available from any source otherwise false.
	 */
	public static boolean hasConnection(Context context) {
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

	// check to see if gps is enable for phones...
	public static boolean isGPSEnabled(Context context) {
		try {
			// find gps provider and make sure it returns a list of location providers
			String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			System.out.println("CheckEnableGPS Provider = " + provider);
			if (!provider.equals("")) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("ERROR = " + e);
		}
		return false;
	}
}