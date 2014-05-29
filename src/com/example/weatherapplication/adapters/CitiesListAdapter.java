package com.example.weatherapplication.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.weatherapplication.R;
import com.example.weatherapplication.models.City;

// simple list adapter for the cities
public class CitiesListAdapter extends ArrayAdapter<City> {
	Context context;
	ArrayList<City> Cities;

	public CitiesListAdapter(Context context, int layoutID, ArrayList<City> objects) {
		super(context, R.layout.cities_list_row_layout, objects);
		this.context = context;
		Cities = objects;
	}

	@Override
	public View getView(int position, View Convertview, ViewGroup parent) {
		// using layoutInflater for a custom listview (took a while to find this actually)
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.cities_list_row_layout, parent, false);

		City city = Cities.get(position);

		TextView cityText = (TextView) view.findViewById(R.id.name);
		cityText.setText(city.getCity() + ", " + city.getState() + ", " + city.getCountry_iso3166());

		return view;
	}
}