package com.example.weatherapplication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DataBaseHelper extends SQLiteOpenHelper {

	// wp path and data paths
	private static final String pkg = "com.example.weatherapplication";
	private static String DB_PATH = "/data/data/" + pkg + "/databases/";

	private static String DB_NAME = "weather.sqlite";
	int[] dbfiles = {R.raw.weather};

	// create database
	private SQLiteDatabase myDataBase;
	private final Context myContext;

	// constructor to grab the context and db name
	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	// create database function
	public void createDataBase() {
		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist - should be holding saved
			// cities
		} else {
			this.getReadableDatabase();
			try {
				CopyDataBase();
			} catch (IOException e) {
				// note to self: i removed the toast message here that was making a fuss if there
				// wasn't a city on first load.
				Log.d("Create DB", e.getMessage());
			}
		}
	}
	
	// check for database
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		// try for database and open if there is one
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		} catch (SQLiteException e) {
			// note to self: i removed the toast message here that was making a fuss if there
			// wasn't a city on first load.
			Log.d("Check DB", e.getMessage());
		}

		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	private void CopyDataBase() throws IOException {
		InputStream databaseInput = null;
		Resources resources = myContext.getResources();
		String outFileName = DB_PATH + DB_NAME;

		OutputStream databaseOutput = new FileOutputStream(outFileName);

		byte[] buffer = new byte[1024];
		int length;

		for (int i = 0; i < dbfiles.length; i++) {
			databaseInput = resources.openRawResource(dbfiles[i]);
			while ((length = databaseInput.read(buffer)) > 0) {
				databaseOutput.write(buffer, 0, length);
				databaseOutput.flush();
			}
			databaseInput.close();
		}

		databaseOutput.flush();
		databaseOutput.close();
	}

	// open db and load in cities
	public void openDataBase() throws SQLException {
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}

	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	// insert into the database and save the values such as the json url (part of it at least)
	public void insert(String name, String country, int color, String loc) {
		String queryStatement = "INSERT INTO city (name, country , color) " + "VALUES ('" + name + "'" + ", " + "'" + country + "'" + ",'" + color + "'" + ")";

		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("country", country);
		values.put("color", color);
		values.put("loc", loc);

		int rowsUpdated = myDataBase.update("city", values, "name like " + "'" + name + "'", null);

		if (rowsUpdated == 0) {
			Log.d("DataBase Helper", "Query : " + queryStatement);

			myDataBase.execSQL(queryStatement);
		} else {
			Log.d("DataBase Helper", "updated rows : " + rowsUpdated);
		}

	}
	
	// pull all cities in (i'm bad for using a select * :P)
	public Cursor getAllCities() {
		return myDataBase.rawQuery("SELECT * FROM city", null);
	}

	// get city location w
	public String getCityLocation(String city) {
		Cursor cursor = myDataBase.rawQuery("SELECT * FROM city where name like '" + city + "'", null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();

			return cursor.getString(1);
		}

		cursor.close();
		return "";
	}

	// update the color as well! HCI!!
	public void UpdateColor(int color, String name, String country) {
		String sql = "UPDATE city SET color=" + "'" + color + "'" + " WHERE name=" + "'" + name + "' AND country='" + country + "'";
		myDataBase.execSQL(sql);
	}

	// delete city from the db
	public void DeleteCity(String name, String country) {
		myDataBase.execSQL("DELETE FROM city WHERE name=" + "'" + name + "'" + " AND country=" + "'" + country + "'");
	}
}