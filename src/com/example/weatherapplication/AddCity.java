package com.example.weatherapplication;

import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddCity extends Activity {
	EditText name;
	Button add;
	String textSpin;
	Spinner sp;

	// ISOCodes - this took forever!
	String[] ISOCODES = new String[]{"AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH", "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BQ", "BA", "BW", "BV", "BR", "IO", "BN", "BG", "BF", "BI", "KH", "CM", "CA", "CV", "KY", "CF", "TD", "CL", "CN", "CX", "CC", "CO", "KM", "CG", "CD", "CK", "CR", "CI", "HR", "CU", "CW", "CY", "CZ", "DK", "DJ", "DM", "DO", "EC", "EG", "SV", "GQ", "ER", "EE", "ET", "FK", "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GG", "GN", "GW", "GY", "HT", "HM", "VA", "HN", "HK", "HU", "IS", "IN", "ID", "IR", "IQ", "IE", "IM", "IL", "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW", "KG", "LA", "LV", "LB", "LS", "LR", "LY", "LI", "LT", "LU", "MO", "MK", "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM", "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP", "NL", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MP", "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR", "QA", "RE", "RO", "RU", "RW", "BL", "SH", "KN", "LC", "MF", "PM", "VC", "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG", "SX", "SK", "SI", "SB", "SO", "ZA", "GS", "SS", "ES", "LK", "SD", "SR", "SJ", "SZ", "SE", "CH", "SY", "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR", "TM", "TC", "TV", "UG", "UA", "AE", "GB", "US", "UM", "UY", "UZ", "VU", "VE", "VN", "VG", "VI", "WF", "EH", "YE", "ZM", "ZW"};

	String FORCAST_URL = "http://api.wunderground.com/api/180dde448747af27/conditions/q/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addcity);

		name = (EditText) findViewById(R.id.name);
		add = (Button) findViewById(R.id.add);
		sp = (Spinner) findViewById(R.id.countryList);

		Arrays.sort(ISOCODES);

		// populate spinner with ISOCODES
		sp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ISOCODES));

		add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (name.getText().toString().matches("") || sp.getSelectedItem().toString().matches("")) {
					Toast.makeText(AddCity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
				} else {
					textSpin = sp.getSelectedItem().toString();

					URL = FORCAST_URL + textSpin + "/" + name.getText().toString() + ".json";

					new AsyncCheckCity().execute();
				}
			}
		});
	}

	String URL = "";
	String ResponseForecast = "";

	public class AsyncCheckCity extends AsyncTask<Void, Void, Void> {
		ProgressDialog pd;

		protected void onPreExecute() {
			pd = ProgressDialog.show(AddCity.this, "Checking City", "Please Wait...");
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
			}
		};
	}

	public void ParseJSON(String jsonString) {
		try {
			// get json object
			JSONObject object = new JSONObject(jsonString);

			JSONObject response = object.getJSONObject("response");
			
			Log.e("AddCity Class", response.toString());
			
			if (response.has("results") || object.has("forecast")) {
				DataBaseHelper helper = new DataBaseHelper(AddCity.this);
				helper.openDataBase();
				Log.w("Spinner value:", textSpin);
				helper.insert(name.getText().toString(), textSpin, R.drawable.red_bg, "");
				helper.close();

				finish();

			} else {
				Toast.makeText(this, "There is no city exists with this name.", Toast.LENGTH_SHORT).show();

				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, "There was an error getting a response from the server.", Toast.LENGTH_SHORT).show();
		}
	}

}