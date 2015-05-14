package com.belwadi.sciencefun.view;


import com.belwadi.sciencefun.R;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {	
	
	public static void makeCustomToastShort(Activity activity, String message) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.my_toast_layout,
				(ViewGroup) activity.findViewById(R.id.toast_layout_root));
		TextView text = (TextView) layout.findViewById(R.id.toast_text);
		text.setText(message);

		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.CENTER, 0, 150);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
	
	public static void makeCustomToastLong(Activity activity, String message) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.my_toast_layout,
				(ViewGroup) activity.findViewById(R.id.toast_layout_root));
		TextView text = (TextView) layout.findViewById(R.id.toast_text);
		text.setText(message);

		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.CENTER, 0, 150);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}	
}