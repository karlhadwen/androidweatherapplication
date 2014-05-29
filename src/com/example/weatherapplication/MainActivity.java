package com.example.weatherapplication;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weatherapplication.interfaces.CallBack;
import com.example.weatherapplication.interfaces.GPSLocation;

@SuppressLint("DefaultLocale")
public class MainActivity extends Activity implements CallBack
{
	private LocationManager locationManager;

	String countryCode;

	ListView listView;

	DataBaseHelper helper;

	NigAdapter adapter;

	ArrayList<String> Cities, CountryCodes, CityLoc;

	ArrayList<Integer> ColorCodes;

	String CurrentCity = "";

	private GPSLocation gpsLocation;

	private double mlatitude;
	private double mlongitude;

	private static final String NO_GPS_MESSAGE = "No GPS set for default location";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.listView);

		helper = new DataBaseHelper(this);
		helper.createDataBase();
		helper.openDataBase();
		helper.close();

		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		countryCode = tm.getSimCountryIso();
		// countryCode = countryCode.toUpperCase();
		countryCode = NO_GPS_MESSAGE;
		startLocationService();

		Log.d("Country Code", countryCode);

		Cities = new ArrayList<String>();
		CountryCodes = new ArrayList<String>();
		CityLoc = new ArrayList<String>();
		ColorCodes = new ArrayList<Integer>();

		adapter = new NigAdapter(this, R.layout.country_list_row, Cities);
		listView.setAdapter(adapter);

		getCitiesFromDB();

		// on click go to the more detailed forecast
		listView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
			{
				if (position == 0 && countryCode.equals(NO_GPS_MESSAGE))
				{

				}
				else
				{
					Intent intent = new Intent(MainActivity.this, WeatherForecastActivity.class);
					intent.putExtra("city", Cities.get(position));
					intent.putExtra("iso", CountryCodes.get(position));
					if(position == 0){
						intent.putExtra("loc", "");
					} else{
						intent.putExtra("loc", CityLoc.get(position - 1));
					}
					startActivityForResult(intent, 3);
				}
			}
		});

		// for changing colour/deleting
		listView.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3)
			{

				final int pos = position;

				if (pos > 0)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Options");
					builder.setPositiveButton("Delete Location", new OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{

							DataBaseHelper helper = new DataBaseHelper(MainActivity.this);
							helper.openDataBase();
							helper.DeleteCity(Cities.get(pos), CountryCodes.get(pos));
							helper.close();

							getCitiesFromDB();

							dialog.dismiss();
						}
					});

					builder.setNegativeButton("Choose Color", new OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Intent intent = new Intent(MainActivity.this, ColorChooser.class);
							intent.putExtra("position", pos);
							startActivityForResult(intent, 2);
						}
					});

					builder.show();
					return true;
				}
				else
				{
					return false;
				}

			}
		});
	}

	private void startLocationService()
	{
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		System.out.println("Is GPS Enable = " + isGPSEnabled());

		if (!isGPSEnabled())
		{
			runOnUiThread(showMessageGPS("Error", "GPS not enabled\nEnable?", this));
		}
		else
		{
			GPSstart();

			System.out.println("lat Sir = " + mlatitude);
			System.out.println("lon Sir = " + mlongitude);
		}
	}

	/* Request updates at startup */
	@Override
	protected void onResume()
	{
		super.onResume();
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause()
	{
		super.onPause();
	}

	private class NigAdapter extends ArrayAdapter<String>
	{
		Context context;

		ArrayList<String> Cities;

		public NigAdapter(Context context, int layoutID, ArrayList<String> objects)
		{
			super(context, R.layout.country_list_row, objects);
			this.context = context;
			Cities = objects;
		}

		@Override
		public View getView(int position, View Convertview, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.country_list_row, parent, false);

			TextView city = (TextView) view.findViewById(R.id.city);
			city.setText(Cities.get(position));
			TextView country = (TextView) view.findViewById(R.id.country);
			country.setText(CountryCodes.get(position));
			LinearLayout ll = (LinearLayout) view.findViewById(R.id.layout);
			ll.setBackgroundResource(ColorCodes.get(position));

			return view;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0)
		{
			getCitiesFromDB();
		}
		else if (requestCode == 2 && resultCode == RESULT_OK)
		{
			int color = data.getExtras().getInt("color");
			int position = data.getExtras().getInt("position");
			DataBaseHelper helper = new DataBaseHelper(MainActivity.this);
			helper.openDataBase();
			helper.UpdateColor(color, Cities.get(position), CountryCodes.get(position));
			helper.close();
			getCitiesFromDB();
		}
		else if (requestCode == 3)
		{
			getCitiesFromDB();
		}

	}

	void getCitiesFromDB()
	{
		Cities.clear();
		CountryCodes.clear();
		ColorCodes.clear();
		CityLoc.clear();

		Cities.add(CurrentCity);

		CountryCodes.add(countryCode);
		ColorCodes.add(R.drawable.blue_bg);

		DataBaseHelper helper = new DataBaseHelper(this);
		helper.openDataBase();

		Cursor c = helper.getAllCities();

		if (c.getCount() > 0)
		{
			c.moveToFirst();
			while (!c.isAfterLast())
			{
				Cities.add(c.getString(1));
				CountryCodes.add(c.getString(2));
				ColorCodes.add(c.getInt(3));
				CityLoc.add(c.getString(4));

				Log.e("", "country = " + c.getString(2));
				Log.e("", "location = " + c.getString(4));

				c.moveToNext();
			}
		}

		c.close();
		helper.close();

		adapter.notifyDataSetChanged();
		listView.invalidate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_settings)
		{
			Intent intent = new Intent(MainActivity.this, AddCity.class);
			startActivityForResult(intent, 0);
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean isGPSEnabled()
	{
		try
		{
			String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			System.out.println("CheckEnableGPS Provider = " + provider);
			if (!provider.equals(""))
			{
				return true;
			}
		}
		catch (Exception e)
		{
			System.out.println("ERROR = " + e);
		}

		return false;
	}

	private Runnable showMessageGPS(final String title, final String message, final Context context)
	{
		Runnable populate = new Runnable()
		{
			public void run()
			{
				Builder alertdialog = new AlertDialog.Builder(context);
				alertdialog.setTitle(title);
				alertdialog.setMessage(message);
				alertdialog.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						try
						{
							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
						catch (Exception e)
						{
							System.out.println("Settings.ACTION_LOCATION_SOURCE_SETTINGS Culd'nt Start Because of " + e);
						}

						dialog = null;
					}
				});

				alertdialog.setNegativeButton("No", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						dialog.dismiss();
						dialog = null;
					}
				});

				alertdialog.show();
			}
		};
		return populate;
	}

	private void GPSstart()
	{
		// gpsLocation = new GPSLocation(locationManager, this);
		gpsLocation = GPSLocation.get(locationManager, this, this);
		mlatitude = GPSLocation.getLatitude();
		mlongitude = GPSLocation.getLongitude();
		gpsLocation.requestLocationUpdates(locationManager);
		gpsLocation.onLocationChanged(GPSLocation.location);
	}

	@Override
	public void notify(Object obj, String type)
	{
		String[] location = type.split("-");
		Log.e("", "" + location[0]);
		Log.e("", "" + location[1]);

		if (!TextUtils.isEmpty(location[1]))
		{
			countryCode = location[0];
			CurrentCity = location[1];
		}
		else
		{
			Toast.makeText(this, "Location is not available", Toast.LENGTH_SHORT).show();
		}
	}
}
