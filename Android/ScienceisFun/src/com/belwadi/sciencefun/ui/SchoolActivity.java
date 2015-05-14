package com.belwadi.sciencefun.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.adapter.SchoolAdapter;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Place;
import com.belwadi.sciencefun.entity.PlaceDetails;
import com.belwadi.sciencefun.entity.PlacesAutoComp;
import com.belwadi.sciencefun.entity.PlacesList;
import com.belwadi.sciencefun.entity.School;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Place.AddressComponent;
import com.belwadi.sciencefun.entity.PlacesAutoComp.PrePlace;
import com.belwadi.sciencefun.utils.GPSTracker;
import com.belwadi.sciencefun.utils.GooglePlaces;
import com.belwadi.sciencefun.utils.Utility;
import com.belwadi.sciencefun.view.AlertDialogManager;
import com.belwadi.sciencefun.view.WaitDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SchoolActivity extends FragmentActivity implements OnClickListener {

	private WaitDialog mWaitDlg;

	private AlertDialogManager mAlertDiag = new AlertDialogManager();

	private Button mBtnBack;

	// title Bar
	private TextView mTxtUserName;
	private ImageView mIconUser;

	private Button mBtnDone, mBtnCancel, mBtnSearchSchool, mBtnDisplaySchool;

	private GoogleMap mGoogleMap;
	private SupportMapFragment mMapFragment;

	private boolean isShowMap, isManual;

	private RelativeLayout mLaySearch, mLayManual;
	private LinearLayout mLayAddManual;

	private TextView mTxtManualSchool;
	private TextView mTxtSelectedSchoolName, mTxtSelectedSchoolAddr;
	
	private EditText mTxtSchool, mTxtAddr, mTxtCity, mTxtPostcode;
	private Spinner mSpinCountry;
	private ArrayList<String> mArrayCountry;

	private AutoCompleteTextView mTxtSearchName;

	private ArrayList<School> mItemSchool = null;
	private SchoolAdapter mAdapterSchool;
	private ListView mListviewSchool;
	private HashMap<Marker, School> markerMap = new HashMap<Marker, School>();

	private GooglePlaces mGooglePlaces;
	private GPSTracker mGps;
	private PlacesList mPlacesLst;
	private PlaceDetails mPlaceDetails;
	private Place mPlaceSelected;
	private PlacesAutoComp mPlacesAutocomp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_school);

		isShowMap = true;

		mGps = new GPSTracker(this);

		// check if GPS location can get
		if (mGps.canGetLocation()) {
			Log.d("Your Location", "latitude:" + mGps.getLatitude() + ", longitude: " + mGps.getLongitude());
		} else {
			// Can't get user's current location
			mAlertDiag.showAlertDialog(this, "GPS Status",
					"Couldn't get location information. Please enable GPS",
					false);
			// stop executing code by return
			//			return;
		}

		// creating Places class object
		mGooglePlaces = new GooglePlaces();

		initView();

		initializeMap();

		updateProfile(GlobalValue.getInstance().getCurrentUser());

		// Search school
		new LoadPlaces().execute("");
	}

	private void initView()
	{		
		mLaySearch = (RelativeLayout) findViewById(R.id.laySearchDetail);
		mLayManual = (RelativeLayout) findViewById(R.id.layManualDetail);
		mLayAddManual = (LinearLayout) findViewById(R.id.layManual);

		isManual = false;
		mLaySearch.setVisibility(View.VISIBLE);
		mLayManual.setVisibility(View.GONE);

		mTxtManualSchool = (TextView) findViewById(R.id.txtManualSchool);

		mTxtUserName = (TextView) findViewById(R.id.txtUserName);
		mIconUser = (ImageView) findViewById(R.id.imgUserIcon);

		mBtnBack = (Button) findViewById(R.id.btnBack);		
		mBtnDone = (Button) findViewById(R.id.btnDone);
		mBtnCancel = (Button) findViewById(R.id.btnCancel);

		mBtnDisplaySchool = (Button) findViewById(R.id.btnDisplaySchool);
		mBtnSearchSchool = (Button) findViewById(R.id.btnSearchSchool);

		mBtnBack.setOnClickListener(this);
		mBtnDone.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);

		mBtnDisplaySchool.setOnClickListener(this);
		mBtnSearchSchool.setOnClickListener(this);

		mTxtManualSchool.setOnClickListener(this);

		mTxtSearchName = (AutoCompleteTextView)  findViewById(R.id.txtSearchName);
		mTxtSelectedSchoolName = (TextView) findViewById(R.id.txtSelectedSchoolName);
		mTxtSelectedSchoolAddr = (TextView) findViewById(R.id.txtSelectedSchoolAddr);

		mTxtSearchName.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.item_preplace));

		mItemSchool = new ArrayList<School>();

		mAdapterSchool = new SchoolAdapter(SchoolActivity.this, R.layout.item_school, mItemSchool);
		mListviewSchool = (ListView) findViewById(R.id.lstSchool);
		mListviewSchool.setAdapter(mAdapterSchool);
		mListviewSchool.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				mTxtSelectedSchoolName.setText("");
				mTxtSelectedSchoolAddr.setText("");
				mPlaceSelected = null;

				new LoadSinglePlaceDetails().execute(mItemSchool.get(position).googleInfo.reference);
			}
		});
		
		mTxtSchool = (EditText) findViewById(R.id.txtSchoolName);
		mTxtAddr = (EditText) findViewById(R.id.txtSchoolAddr);
		mTxtCity = (EditText) findViewById(R.id.txtSchoolCity);
		mTxtPostcode = (EditText) findViewById(R.id.txtSchoolPostalCode);
		
		mSpinCountry= (Spinner) findViewById(R.id.spinCountry);
		mSpinCountry.setPrompt("Select Country");

		Locale[] locale = Locale.getAvailableLocales();
		mArrayCountry = new ArrayList<String>();
		String country;
		for( Locale loc : locale ){
		    country = loc.getDisplayCountry();
		    if( country.length() > 0 && !mArrayCountry.contains(country) ){
		    	mArrayCountry.add( country );
		    }
		}
		Collections.sort(mArrayCountry, String.CASE_INSENSITIVE_ORDER);

		ArrayAdapter<String> adapterCountry = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mArrayCountry);
		adapterCountry.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mSpinCountry.setAdapter(adapterCountry);
	}

	private void initializeMap() {

		if (mGoogleMap == null) {

			mMapFragment = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapSchool));
			mGoogleMap = mMapFragment.getMap();			

			if (mGoogleMap == null) {
				Toast.makeText(this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
			} else {

				mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
				mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
				mGoogleMap.getUiSettings().setZoomControlsEnabled(false);		
				mGoogleMap.setMyLocationEnabled(true);
			}

			mMapFragment.getView().setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();

		switch (id)
		{
		case R.id.btnBack:
			finish();
			overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
			break;
		case R.id.btnDone:
			onClickDone();
			break;
		case R.id.btnCancel:
			Intent prevIntent = getIntent();
			setResult(RESULT_CANCELED, prevIntent);
			finish();
			overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
			break;
		case R.id.btnDisplaySchool:
			onClickDisplaySchool();
			break;
		case R.id.btnSearchSchool:
			onClickSearchSchool();
			break;
		case R.id.txtManualSchool:
			isManual = true;
			mLaySearch.setVisibility(View.GONE);
			mLayManual.setVisibility(View.VISIBLE);
			mLayAddManual.setVisibility(View.GONE);
			break;
		default:

			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean bool;
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			bool = super.onKeyDown(keyCode, event);
		} else {
			Intent prevIntent = getIntent();
			setResult(RESULT_CANCELED, prevIntent);
			finish();
			overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);			
			bool = true;
		}
		return bool;
	}

	private void onClickDone()
	{
		UserInfo userInfo = new UserInfo();

		if (isManual == true) {
			userInfo.schoolName = mTxtSchool.getText().toString().trim();
			userInfo.address = mTxtAddr.getText().toString().trim();
			userInfo.city = mTxtCity.getText().toString().trim();
			userInfo.country = mSpinCountry.getSelectedItem().toString();
			userInfo.postcode = mTxtPostcode.getText().toString().trim();
			
			if (!checkSchoolParameter(userInfo)) {
				mAlertDiag.showAlertDialog(this, "Select School", "Please input required informations!", false);
				return;
			}
			
		} else {
			if(mPlaceSelected == null){
				mAlertDiag.showAlertDialog(this, "Select School", "Please select the school.", false);
				return;
			}

			userInfo.schoolName = mPlaceSelected.name;			
			userInfo.address = mPlaceSelected.vicinity;
			getSchoolInfoFromPlace(mPlaceSelected, userInfo);
		}
		
		Intent prevIntent = getIntent();

		prevIntent.putExtra("schoolname", userInfo.schoolName);
		prevIntent.putExtra("address", userInfo.address);
		prevIntent.putExtra("country", userInfo.country);
		prevIntent.putExtra("city", userInfo.city);
		prevIntent.putExtra("postalcode", userInfo.postcode);

		setResult(RESULT_OK, prevIntent);

		finish();
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);	
	}
	
	private boolean checkSchoolParameter(UserInfo userInfo)
	{
		if (!Utility.checkString(userInfo.schoolName)
				|| !Utility.checkString(userInfo.address)
				|| !Utility.checkString(userInfo.city)
				|| !Utility.checkString(userInfo.country)
				|| !Utility.checkString(userInfo.postcode)
				)
			return false;

		return true;
	}


	private void onClickDisplaySchool ()
	{
		if (isShowMap == true) {
			mBtnDisplaySchool.setBackgroundResource(R.drawable.btn_list);
			mMapFragment.getView().setVisibility(View.VISIBLE);
			mListviewSchool.setVisibility(View.GONE);
			isShowMap = false;
		}		
		else {
			mBtnDisplaySchool.setBackgroundResource(R.drawable.btn_map);
			mMapFragment.getView().setVisibility(View.GONE);
			mListviewSchool.setVisibility(View.VISIBLE);
			isShowMap = true;
		}
	}

	private void onClickSearchSchool()
	{
		/**
		 * Clear Places List
		 * */
		mItemSchool.clear();
		mTxtSelectedSchoolName.setText("");
		mTxtSelectedSchoolAddr.setText("");
		mPlaceSelected = null;

		new LoadPlaces().execute("");
	}

	private void getAdditionalPlaces (String pageToken)
	{
		new LoadPlaces().execute(pageToken);
	}

	private void updateProfile (UserInfo userInfo)
	{
		mTxtUserName.setText(userInfo.name);

		if (userInfo.photo != null)
			mIconUser.setImageBitmap(userInfo.photo);
	}

	private void getSchoolInfoFromPlace(Place place, UserInfo userInfo)
	{
		if (place == null || place.address_components == null)
			return;

		for (AddressComponent component : place.address_components)
		{
			for (String type : component.types)
			{
				if (type.compareToIgnoreCase("postal_town") == 0
						|| type.compareToIgnoreCase("locality") == 0)
					userInfo.city = component.long_name;
				else if (type.compareToIgnoreCase("country") == 0)
					userInfo.country = component.long_name;
				else if (type.compareToIgnoreCase("postal_code") == 0)
					userInfo.postcode = component.long_name;
			}
		}

		if(userInfo.city == null)
			userInfo.city = "none";
		if(userInfo.country == null)
			userInfo.country = "none";
		if(userInfo.postcode == null)
			userInfo.postcode = "none";
	}

	private void setupPin() {
		mGoogleMap.clear();
		markerMap.clear();

		double longitude, latitude;
		for(int i = 0 ; i < mItemSchool.size(); i++){
			School item = mItemSchool.get(i);
			String address;
			if (item.googleInfo.vicinity != null && !item.googleInfo.vicinity.isEmpty())
				address = item.googleInfo.vicinity;
			else if (item.googleInfo.formatted_address != null && !item.googleInfo.formatted_address.isEmpty())
				address = item.googleInfo.formatted_address;
			else
				address = "";

			latitude = item.googleInfo.geometry.location.lat;
			longitude = item.googleInfo.geometry.location.lng;
			LatLng sfLatLng = new LatLng(latitude, longitude);

			Marker marker = mGoogleMap.addMarker(new MarkerOptions()
			.position(sfLatLng)
			.title(item.googleInfo.name)
			.snippet(address)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_red)));

			markerMap.put(marker, item);

			mGoogleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {

					mTxtSelectedSchoolName.setText("");
					mTxtSelectedSchoolAddr.setText("");
					mPlaceSelected = null;

					School school = markerMap.get(marker);

					new LoadSinglePlaceDetails().execute(school.googleInfo.reference);
				}
			});

			if (i == mItemSchool.size() / 2)
			{
				LatLng cameraLatLng = sfLatLng;
				float cameraZoom = 10;
				mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraLatLng,
						cameraZoom));
			}
		}
	}



	/**
	 * Background Async Task to Search Google places
	 * */
	class LoadPlaces extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mWaitDlg == null) {
				mWaitDlg = new WaitDialog(SchoolActivity.this);
				mWaitDlg.setMessage("Searching ... ");
				mWaitDlg.setCancelable(false);
				mWaitDlg.show();
			}
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {

			String strSearchQuery = mTxtSearchName.getText().toString().trim();
			String pageToken = args[0];

			try {

				String types = "school"; // Listing places only schools

				// Radius in meters - increase this value if you don't find any places
				int radius = 10000; // 10000 meters (this value is maximum)

				Log.d("LoadPlaces", "Queary : " + strSearchQuery + ", pageToken : " + pageToken);

				if (strSearchQuery.isEmpty()) {
					// get nearest places
					mPlacesLst = mGooglePlaces.searchNearBy(mGps.getLatitude(), mGps.getLongitude(), radius, types, pageToken);
				} else {
					// get places by text search
					String query = "school " + strSearchQuery;
					mPlacesLst = mGooglePlaces.searchByText(query, types, pageToken);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * and show the data in UI
		 * Always use runOnUiThread(new Runnable()) to update UI from background
		 * thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {			

			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {					

					// Get JSON response status
					String status = mPlacesLst.status;

					// Check for all possible status
					if(status.equals("OK")){
						// Successfully got places details
						if (mPlacesLst.results != null) {
							// loop through each place
							for (Place p : mPlacesLst.results) {

								mItemSchool.add(new School(p));								
							}
						}

						if (mPlacesLst.next_page_token != null
								&& !mPlacesLst.next_page_token.isEmpty()) {
							getAdditionalPlaces(mPlacesLst.next_page_token);
						} else {
							// dismiss the dialog after getting all products
							if (mWaitDlg != null) {
								mWaitDlg.dismiss();
								mWaitDlg = null;
							}
						}						
					}
					else {
						if (mWaitDlg != null) {
							mWaitDlg.dismiss();
							mWaitDlg = null;
						}

						showAlertPlaceError(status);
					}

					setupPin();
					mAdapterSchool.notifyDataSetChanged();
				}
			});

		}
	}

	class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mWaitDlg = new WaitDialog(SchoolActivity.this);
			mWaitDlg.setMessage("Loading Detail ...");
			mWaitDlg.setCancelable(false);
			mWaitDlg.show();
		}

		/**
		 * getting Profile JSON
		 * */
		protected String doInBackground(String... args) {
			String reference = args[0];

			// Check if used is connected to Internet
			try {
				mPlaceDetails = mGooglePlaces.getPlaceDetails(reference);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}

			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					if(mPlaceDetails != null){
						String status = mPlaceDetails.status;

						// check place details status
						// Check for all possible status
						if(status.equals("OK")){
							if (mPlaceDetails.result != null) {
								mPlaceSelected = mPlaceDetails.result;								
								mTxtSelectedSchoolName.setText(mPlaceSelected.name);								
								mTxtSelectedSchoolAddr.setText(mPlaceSelected.vicinity);
							}
						}
						else {
							showAlertPlaceError(status);
						}
					}else{
						mAlertDiag.showAlertDialog(SchoolActivity.this, "Places Error",
								"Sorry error occured.",
								false);
					}


				}
			});

		}

	}

	private void showAlertPlaceError(String status)
	{
		if(status.equals("ZERO_RESULTS")){
			// Zero results found
			mAlertDiag.showAlertDialog(SchoolActivity.this, "Search School",
					"Sorry no places found. Try to change the types of places",
					false);
		}
		else if(status.equals("UNKNOWN_ERROR"))
		{
			mAlertDiag.showAlertDialog(SchoolActivity.this, "Search School",
					"Sorry unknown error occured.",
					false);
		}
		else if(status.equals("OVER_QUERY_LIMIT"))
		{
			mAlertDiag.showAlertDialog(SchoolActivity.this, "Search School",
					"Sorry query limit to google places is reached",
					false);
		}
		else if(status.equals("REQUEST_DENIED"))
		{
			mAlertDiag.showAlertDialog(SchoolActivity.this, "Search School",
					"Sorry error occured. Request is denied",
					false);
		}
		else if(status.equals("INVALID_REQUEST"))
		{
			//			mAlertDiag.showAlertDialog(SchoolActivity.this, "Search School",
			//					"Sorry error occured. Invalid Request",
			//					false);
		}
		else
		{
			mAlertDiag.showAlertDialog(SchoolActivity.this, "Search School",
					"Sorry error occured.",
					false);
		}
	}

	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
		private ArrayList<String> resultList;

		public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			resultList = new ArrayList<String>();
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			if (resultList.size() < 1)
				return null;

			return resultList.get(index);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {

						resultList.clear();

						String types = "geocode";
						String componenets = "country:in"; // India

						try {
							mPlacesAutocomp = mGooglePlaces.getAutoCompletePlaces(mGps.getLatitude(), mGps.getLongitude(), constraint.toString(), types, componenets);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String status = mPlacesAutocomp.status;

						// Check for all possible status
						if(status.equals("OK")){
							// Successfully got places details
							if (mPlacesAutocomp.predictions != null) {
								// loop through each place
								for (PrePlace p : mPlacesAutocomp.predictions) {
									resultList.add(new String(p.description));
								}

							}
						}
						else {
							//							showAlertPlaceError(status);
						}


						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					}
					else {
						notifyDataSetInvalidated();
					}
				}};
				return filter;
		}
	}
}
