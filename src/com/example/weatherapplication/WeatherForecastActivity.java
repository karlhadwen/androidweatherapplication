package com.example.weatherapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.twitter.PrepareRequestTokenActivity;
import com.android.twitter.TwitterUtils;
import com.example.weatherapplication.adapters.CitiesListAdapter;
import com.example.weatherapplication.models.City;
import com.example.weatherapplication.utils.Utils;

public class WeatherForecastActivity extends Activity {
	ListView listView;
	ArrayList<String> Weather_Title;
	ArrayList<String> Weather_Subtitle;
	ArrayList<String> Weather_Condition;
	ListAdapter adapter;

	// weather urls, and a fix for the multiple cities (weather_url <-- fixes multiple cities with the same name)
	String WEATHER_URL = "http://api.wunderground.com/api/180dde448747af27/forecast/q/";
	String WEATHER_URL_1 = "http://api.wunderground.com/api/180dde448747af27/forecast";

	String URL = "";
	String ResponseForecast = "";
	Button save, refresh;
	TextView title;
	String CityName = "";
	String CountryCode = "";
	private ArrayList<City> cities;
	private Dialog dialog2;
	private String CityLocation;

	private Dialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_forecast);

		// return an activity result
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		listView = (ListView) findViewById(R.id.listView);
		title = (TextView) findViewById(R.id.location);
		refresh = (Button) findViewById(R.id.refresh);

		Weather_Title = new ArrayList<String>();
		Weather_Subtitle = new ArrayList<String>();
		Weather_Condition = new ArrayList<String>();

		adapter = new ListAdapter(this, R.layout.list_item_layout, Weather_Title);
		listView.setAdapter(adapter);

		// get the intentions of this result and the extras it provides
		if (getIntent().getExtras() != null) {
			CityName = getIntent().getExtras().getString("city");
			CountryCode = getIntent().getExtras().getString("iso");
			CityLocation = getIntent().getExtras().getString("loc");

			Utils.CityName = CityName;
			Utils.CountryCode = CountryCode;
			Utils.CityLocation = CityLocation;
		} else {
			CityName = Utils.CityName;
			CountryCode = Utils.CountryCode;
			CityLocation = Utils.CityLocation;
		}

		if (TextUtils.isEmpty(CityLocation)) {
			URL = WEATHER_URL + CountryCode + "/" + CityName.replaceAll("\\s", "_") + ".json";
			Log.d("url", URL);
		} else {
			URL = WEATHER_URL_1 + CityLocation + ".json";
			Log.d("url", URL);
		}

		// set title
		title.setText("Forecast For " + CityName.toUpperCase());
		new Downloader().execute();

		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Downloader().execute();
			}
		});
	}
	
	// didn't want to crash the main thread by blocking it for too long
	public class Downloader extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;

		protected void onPreExecute() {
			// here's some more HCI!
			pd = ProgressDialog.show(WeatherForecastActivity.this, "Downloading", "Please Wait...");
		};

		@Override
		protected Void doInBackground(Void... params) {
			ResponseForecast = JSONfunctions.getJSONfromURL(URL);
			return null;
		}

		protected void onPostExecute(Void result) {
			pd.dismiss();
			if (ResponseForecast != null) {
				ParseJSON(ResponseForecast);
				adapter.notifyDataSetChanged();
			}
		};
	}

	// parse json
	public void ParseJSON(String jsonString) {
		try {
			// get json object
			JSONObject object = new JSONObject(jsonString);

			JSONObject response = object.getJSONObject("response");
			if (response.has("results")) {

				JSONArray results = response.getJSONArray("results");
				Log.e("", "results arrray = " + results.length());

				cities = new ArrayList<City>();

				for (int i = 0; i < results.length(); i++) {
					JSONObject obj = results.getJSONObject(i);

					City city = new City();
					city.setName(obj.getString("name"));
					city.setCity(obj.getString("city"));
					city.setState(obj.getString("state"));
					city.setCountry(obj.getString("country"));
					city.setCountry_iso3166(obj.getString("country_iso3166"));
					city.setCountry_name(obj.getString("country_name"));
					city.setZmw(obj.getString("zmw"));
					city.setL(obj.getString("l"));
					
					cities.add(city);
				}

				Collections.sort(cities, new Comparator<City>() {
				    public int compare(City result1, City result2) {
				        return result1.getCity().compareToIgnoreCase(result2.getCity());
				    }
				});
				
				showDialog();
				return;

			} else if (object.has("forecast")) {
				// get forecast object
				JSONObject forecast = object.getJSONObject("forecast");
				// get simpleforecast object
				JSONObject simpleforecast = forecast.getJSONObject("simpleforecast");
				// get array for the forecastday
				JSONArray array = simpleforecast.getJSONArray("forecastday");
				int count = array.length();

				Weather_Title.clear();
				Weather_Subtitle.clear();
				Weather_Condition.clear();

				// for loop to go over all the conditions for the next 5 days
				for (int i = 0; i < count; i++) {
					JSONObject ob = array.getJSONObject(i).getJSONObject("date");
					String title = ob.getString("weekday") + " : " + array.getJSONObject(i).getString("conditions");
					Weather_Title.add(title);
					Weather_Condition.add(array.getJSONObject(i).getString("conditions"));
					JSONObject high = array.getJSONObject(i).getJSONObject("high");
					JSONObject low = array.getJSONObject(i).getJSONObject("low");
					String temp = "High : " + high.getString("celsius") + "C (" + high.getString("fahrenheit") + "F) ";
					temp = temp.concat("Low : " + low.getString("celsius") + "C (" + low.getString("fahrenheit") + "F) ");
					Log.d("Temp", temp);
					Weather_Subtitle.add(temp);
				}
			} else {
				// broke the json!
				Toast.makeText(this, "There was an error getting a response from the server.", Toast.LENGTH_SHORT).show();
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, "There was an error getting a response from the server.", Toast.LENGTH_SHORT).show();
		}
	}

	public class ListAdapter extends ArrayAdapter<String> {
		ArrayList<String> Dates;
		Context context;

		public ListAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
			super(context, R.layout.list_item_layout, objects);

			Dates = objects;
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.list_item_layout, parent, false);

			TextView date = (TextView) view.findViewById(R.id.textViewName);
			TextView condition = (TextView) view.findViewById(R.id.textViewDescription);
			ImageView icon = (ImageView) view.findViewById(R.id.imageView1);

			date.setText(Dates.get(position));
			condition.setText(Weather_Subtitle.get(position));

			// check weather condition to update the ui
			if (Weather_Condition.get(position).equalsIgnoreCase("Rain")) {
				icon.setImageResource(R.drawable.rain);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Fog"))) {
				icon.setImageResource(R.drawable.fog);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Chance of Rain"))) {
				icon.setImageResource(R.drawable.rain);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Mostly Cloudy"))) {
				icon.setImageResource(R.drawable.mostlycloudly);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Partly Cloudy"))) {
				icon.setImageResource(R.drawable.partlycloudly);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Sleet"))) {
				icon.setImageResource(R.drawable.sleet);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Rain Showers"))) {
				icon.setImageResource(R.drawable.rain);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Snow"))) {
				icon.setImageResource(R.drawable.snow);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Clear"))) {
				icon.setImageResource(R.drawable.sun);
			} else if ((Weather_Condition.get(position).equalsIgnoreCase("Chance of a Thunderstorm"))) {
				icon.setImageResource(R.drawable.tstorms);
			} else {
				icon.setVisibility(View.GONE);
			}
			return view;
		}
	}

	// fix for multiple cities, it will show a dialog that fixes and gives you multiple cities to choose from
	private void showDialog() {
		dialog2 = new Dialog(this);

		LinearLayout linear = new LinearLayout(this);
		linear.setBackgroundColor(getResources().getColor(android.R.color.white));

		ListView citiesListView = new ListView(this);
		citiesListView.setCacheColorHint(android.R.color.transparent);
		citiesListView.setBackgroundColor(getResources().getColor(android.R.color.transparent));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Mulitple Cities");
		CitiesListAdapter adapter = new CitiesListAdapter(this, R.layout.cities_list_row_layout, cities);

		citiesListView.setAdapter(adapter);

		citiesListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				City city = cities.get(position);

				URL = WEATHER_URL_1 + city.getL() + ".json";
				Log.d("url", URL);

				CityName = city.getCity();
				CountryCode = city.getCountry_iso3166();
				CityLocation = city.getL();

				DataBaseHelper helper = new DataBaseHelper(WeatherForecastActivity.this);
				helper.openDataBase();
				Log.w("Spinner value:", city.getCountry_iso3166());
				helper.insert(CityName, CountryCode, R.drawable.red_bg, CityLocation);
				helper.close();

				dialog2.dismiss();

				new Downloader().execute();
			}
		});

		citiesListView.setAdapter(adapter);
		builder.setView(citiesListView);
		dialog2 = builder.create();
		dialog2.setCancelable(true);
		dialog2.show();
	}

	// as with the above, show an alert for multiple cities
	private void showAlert() {
		alertDialog = new Dialog(this, R.style.cutomDialogStyle);
		alertDialog.setContentView(R.layout.cities_list_dialog);
		alertDialog.setTitle("Mulitple Cities");

		ListView citiesListView = (ListView) alertDialog.findViewById(R.id.listView);
		CitiesListAdapter adapter = new CitiesListAdapter(this, R.layout.cities_list_row_layout, cities);

		citiesListView.setAdapter(adapter);
		citiesListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				City city = cities.get(position);

				URL = WEATHER_URL_1 + city.getL() + ".json";
				Log.d("url", URL);

				CityName = city.getCity();
				CountryCode = city.getCountry_iso3166();
				CityLocation = city.getL();

				DataBaseHelper helper = new DataBaseHelper(WeatherForecastActivity.this);
				helper.openDataBase();
				Log.w("Spinner value:", city.getCountry_iso3166());
				helper.insert(CityName, CountryCode, R.drawable.red_bg, CityLocation);
				helper.close();

				alertDialog.dismiss();

				new Downloader().execute();
			}
		});

		citiesListView.setAdapter(adapter);

		alertDialog.setCancelable(true);
		alertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_weather_forecast, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_twitter) {

			if (TwitterUtils.isAuthenticated(prefs)) {
				sendTweet();
			} else {
				Intent i = new Intent(getApplicationContext(), PrepareRequestTokenActivity.class);
				i.putExtra("tweet_msg", getTweetMsg());
				startActivity(i);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	// send tweet in a new thread so we don't crash the main thread
	public void sendTweet() {
		Thread t = new Thread() {
			public void run() {

				try {
					TwitterUtils.sendTweet(prefs, getTweetMsg());
					mTwitterHandler.post(mUpdateTwitterNotification);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		};
		t.start();
	}

	private String getTweetMsg() {
		String weather = Weather_Subtitle.get(0);
		Log.e("", "" + weather);

		// twitter message
		return "The forecast for " + CityName + ", " + CountryCode + " is: " + Weather_Subtitle.get(0) + " - Provided by Karl Hadwen's Weather App";
	}

	// twitter prefs
	private SharedPreferences prefs;

	// manage between threads
	private final Handler mTwitterHandler = new Handler();

	private TextView loginStatus;

	final Runnable mUpdateTwitterNotification = new Runnable() {
		public void run() {
			// a little HCI added
			Toast.makeText(getBaseContext(), "Tweet sent !", Toast.LENGTH_LONG).show();
		}
	};
}