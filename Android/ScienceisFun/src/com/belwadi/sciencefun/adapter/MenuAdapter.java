package com.belwadi.sciencefun.adapter;

import java.util.ArrayList;





import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.entity.MainMenu;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MenuAdapter extends ArrayAdapter<MainMenu> {
	Activity activity;
	int layoutResourceId;
	ArrayList<MainMenu> item = new ArrayList<MainMenu>();

	// constuctor
	public MenuAdapter(Activity activity, int layoutId, ArrayList<MainMenu> items) {
		super(activity, layoutId, items);
		item = items;
		this.activity = activity;
		this.layoutResourceId = layoutId;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		MenuHolder holder = null;	

		MainMenu menuItem = getItem(position);

		// inflate the view
		if (convertView == null) {
			LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
			convertView = inflater.inflate(R.layout.item_menu, null);
			holder = new MenuHolder();
			holder.txtNumber = (TextView) convertView.findViewById(R.id.txtNumber);
			holder.txtName = (TextView) convertView.findViewById(R.id.txtMenu);
			
			convertView.setTag(holder);
		} 
		else
			holder = (MenuHolder) convertView.getTag();

		int number = position + 1;
		holder.txtNumber.setText(number + "." );
		holder.txtName.setText(menuItem.name);
		return convertView;
	}

	public static class MenuHolder {
		TextView txtNumber;
		TextView txtName;
	}
}
