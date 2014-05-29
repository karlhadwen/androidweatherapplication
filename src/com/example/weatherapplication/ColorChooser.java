package com.example.weatherapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ColorChooser extends Activity {
	// implement images instead of colours but use some constant or something to represent them?
	String[] ColorNames = { "Blue" , "Green", "Orange", "Purple", "Yellow", "Red" };
	int[] ColorCodes = { R.drawable.blue_bg , R.drawable.green_bg, R.drawable.orange_bg, R.drawable.purple_bg, R.drawable.yellow_bg, R.drawable.red_bg };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.color);
		
		ListView listView = (ListView)findViewById(R.id.list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 , ColorNames);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position ,
					long arg3) {
				Intent intent = new Intent();
				intent.putExtra("color", ColorCodes[position]);
				intent.putExtra("position", getIntent().getExtras().getInt("position"));
				setResult(RESULT_OK , intent);
				finish();
			}
		});
	}
}