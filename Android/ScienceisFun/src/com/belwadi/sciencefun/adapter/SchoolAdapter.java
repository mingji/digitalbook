package com.belwadi.sciencefun.adapter;

import java.util.ArrayList;






import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.entity.School;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SchoolAdapter extends ArrayAdapter<School> {
	Activity activity;
	int layoutResourceId;
	ArrayList<School> item = new ArrayList<School>();

	// constuctor
	public SchoolAdapter(Activity activity, int layoutId, ArrayList<School> items) {
		super(activity, layoutId, items);
		item = items;
		this.activity = activity;
		this.layoutResourceId = layoutId;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		MenuHolder holder = null;	

		School schoolItem = getItem(position);
		String address;
		
		if (schoolItem.googleInfo.vicinity != null && !schoolItem.googleInfo.vicinity.isEmpty())
			address = schoolItem.googleInfo.vicinity;
		else if (schoolItem.googleInfo.formatted_address != null && !schoolItem.googleInfo.formatted_address.isEmpty())
			address = schoolItem.googleInfo.formatted_address;
		else
			address = "";

		// inflate the view
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
			convertView = inflater.inflate(R.layout.item_school, null);
			holder = new MenuHolder();
			holder.txtName = (TextView) convertView.findViewById(R.id.txtSchoolName);
			holder.txtAddr = (TextView) convertView.findViewById(R.id.txtSchoolAddr);
			
			convertView.setTag(holder);
		} 
		else
			holder = (MenuHolder) convertView.getTag();

		holder.txtName.setText(schoolItem.googleInfo.name);
		holder.txtAddr.setText(address);
		return convertView;
	}

	public static class MenuHolder {
		TextView txtName;
		TextView txtAddr;
	}
}
