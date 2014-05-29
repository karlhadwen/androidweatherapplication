package com.example.weatherapplication.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog {
	private ProgressDialog pDialog = null;
	private static CustomProgressDialog me;

	public void showDialog(Context context, String message) {
		try {
			pDialog = ProgressDialog.show(context, "", message, true);
		} catch (Exception e) {
			System.out.println("LocalliteProgressDialog on showDialog ERROR = " + e);
		}
	}

	public void hideDialog() {
		if (pDialog != null)
			pDialog.dismiss();
	}

	public static CustomProgressDialog getInstanse() {
		if (me == null) {
			me = new CustomProgressDialog();
		}
		return me;
	}

	public void setMessage(String string) {
		pDialog.setMessage(string);
	}
}