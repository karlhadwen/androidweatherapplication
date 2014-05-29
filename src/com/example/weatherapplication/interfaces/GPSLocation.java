package com.example.weatherapplication.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPSLocation implements LocationListener {
	private static GPSLocation _instance;

	private static double mLatitude;
	private static double mLongitude;

	public static Location location;

	private static String provider;

	private Context context;

	private CallBack callBack;

	public GPSLocation(LocationManager locationManager, Context context) {
//		_instance.context = context;
		locationManagerService(locationManager);
	}

	public static GPSLocation get(LocationManager locationManager, Context context, CallBack callBack) {
		if (_instance == null) {
			_instance = new GPSLocation(locationManager, context);
			_instance.context = context;
			_instance.callBack = callBack;
		}
		return _instance;
	}

	public static void locationManagerService(LocationManager locationManager) {
		Criteria criteria = new Criteria();
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);

		setProvider(locationManager.getBestProvider(criteria, true));
		location = locationManager.getLastKnownLocation(provider);

		if (location != null) {
			setLatitude(location.getLatitude());
			setLongitude(location.getLongitude());
		}
	}

	public void onLocationChanged(Location location) {
		if (location != null) {
			setLatitude(location.getLatitude());
			setLongitude(location.getLongitude());
			
			Geocoder gcd = new Geocoder(context, Locale.getDefault());
			
			List<Address> addresses;
			
			try {
				addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if (addresses.size() > 0) {
//					if (addresses.get(0).getLocality() != null) {
//						System.out.println(addresses.get(0).getLocality());
//						CurrentCity = addresses.get(0).getLocality();
						Log.e("", addresses.get(0).getCountryCode());
//						Log.e("", addresses.get(0).get);
//						countryCode = addresses.get(0).getCountryCode();
						
						callBack.notify("", addresses.get(0).getCountryCode() + "-" + addresses.get(0).getLocality());

//					} else {
//						CurrentCity = "";
//					}

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void onProviderDisabled(String provider) {

	}

	public void onProviderEnabled(String provider) {

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public void requestLocationUpdates(LocationManager locationManager) {
		if (locationManager != null) {
			locationManager.requestLocationUpdates(provider, 10000, 1, this);
		}
	}

	public static String getProvider() {
		return provider;
	}

	public static void setProvider(String provider) {
		GPSLocation.provider = provider;
	}

	public static double getLatitude() {
		return mLatitude;
	}

	public static void setLatitude(double mLatitude) {
		GPSLocation.mLatitude = mLatitude;
	}

	public static double getLongitude() {
		return mLongitude;
	}

	public static void setLongitude(double mLongitude) {
		GPSLocation.mLongitude = mLongitude;
	}

}
