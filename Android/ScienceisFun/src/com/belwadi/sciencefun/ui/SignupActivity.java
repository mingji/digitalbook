package com.belwadi.sciencefun.ui;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.adapter.SchoolAdapter;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Grade;
import com.belwadi.sciencefun.entity.Place;
import com.belwadi.sciencefun.entity.PlaceDetails;
import com.belwadi.sciencefun.entity.PlacesAutoComp;
import com.belwadi.sciencefun.entity.PlacesList;
import com.belwadi.sciencefun.entity.School;
import com.belwadi.sciencefun.entity.Scientist;
import com.belwadi.sciencefun.entity.Syllabus;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Beans.CheckMailResult;
import com.belwadi.sciencefun.entity.Beans.SignUpResult;
import com.belwadi.sciencefun.entity.Place.AddressComponent;
import com.belwadi.sciencefun.entity.PlacesAutoComp.PrePlace;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ConnectionDetector;
import com.belwadi.sciencefun.utils.GPSTracker;
import com.belwadi.sciencefun.utils.GooglePlaces;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.utils.Utility;
import com.belwadi.sciencefun.view.AlertDialogManager;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.HyIconView;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.BaseTask;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.Server;
import com.belwadi.sciencefun.webservice.BaseTask.TaskListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SignupActivity extends FragmentActivity implements OnClickListener{

	private static final int PICK_FROM_ALBUM = 0;

	WaitDialog mWaitDlg;

	AlertDialogManager mAlertDiag = new AlertDialogManager();
	
	private DatabaseManager mDbMgr;
	private AppPreferences mPrefs;

	// title Bar
	TextView mTxtTitle, mTxtDisplayName;
	HyIconView mImgUser;
	
	PopupMenu mPopMenu;
	
	Scientist mScientist;

	// Step label
	TextView mTxtStepNum;

	// SignIn and Guest
	Button mBtnSignin, mBtnCancel;

	// Layouts by steps
	RelativeLayout mLayStep1, mLayStep2_1, mLayStep2_2, mLayStep3, mLayStep4, mLayFinal;

	// Layout bottom
	RelativeLayout mLayBottom;

	Bitmap mBpPhoto;

	ImageButton mBtnStep1Next, mBtnStep21Next, mBtnStep21Prev, mBtnStep22Next, mBtnStep22Prev, mBtnStep3Next, mBtnStep3Prev;
	Button mBtnSubmit, mBtnSearchSchool, mBtnDisplaySchool, mBtnGoLesson;

	ImageView mImgPhoto, mImgEdit;
	TextView mTxtFinalName, mTxtFinalMsg;

	TextView mTxtManualSchool;

	TextView mTxtSelectedSchoolName, mTxtSelectedSchoolAddr;

	AutoCompleteTextView mTxtSearchName;
	ArrayList<String> mArrayPreplaces;
	ArrayAdapter<String> mAdapterPreplaces;

	private Spinner mSpinSyllabus;
	private ArrayList<String> mArraySyllabus;
	
	private Spinner mSpinGrade;
	private ArrayList<String> mArrayGrade;

	private Spinner mSpinCountry;
	private ArrayList<String> mArrayCountry;

	private ArrayList<School> mItemSchool = null;
	private SchoolAdapter mAdapterSchool;
	private ListView mListviewSchool;
	private HashMap<Marker, School> markerMap = new HashMap<Marker, School>();

	EditText mTxtYourname, mTxtMail, mTxtPasswd, mTxtConfirmPwd;
	EditText mTxtSchool, mTxtAddr, mTxtCity, mTxtPostcode, mTxtSection;

	private Step mStep;
	public enum Step {
		STEP1, STEP2_1, STEP2_2, STEP3, STEP4, FINAL, NONE
	};

	boolean isShowMap;

	private GoogleMap mGoogleMap;
	private SupportMapFragment mMapFragment;

	// Search School via Google Place API
	ConnectionDetector mConnectionDetect;
	Boolean isInternetPresent = false;
	GooglePlaces mGooglePlaces;
	GPSTracker mGps;
	PlacesList mPlacesLst;
	PlaceDetails mPlaceDetails;
	Place mPlaceSelected;
	PlacesAutoComp mPlacesAutocomp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_signup);
		
		mPrefs = new AppPreferences(this);
		mDbMgr = new DatabaseManager(this);
		mScientist = GlobalValue.getInstance().getScientist(this);

		mStep = Step.STEP1;

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

		mBpPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.empty_pic);

		initView();
		initViewGuestUser();
		initializeMap();
	}

	private void initView()
	{
		mTxtTitle = (TextView) findViewById(R.id.txtTitle);
		mTxtDisplayName = (TextView) findViewById(R.id.txtUserName);
		mImgUser = (HyIconView) findViewById(R.id.imgUserIcon);
		mImgUser.setOnClickListener(this);

		mTxtTitle.setText("Sign Up");
		
		mPopMenu = new PopupMenu(this, mImgUser);
		mPopMenu.getMenuInflater().inflate(R.menu.popup, mPopMenu.getMenu());
		mPopMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				
				onClickPopupMenu(item.getTitle().toString());
				
				return true;
			}
		});

		mTxtStepNum = (TextView) findViewById(R.id.txtSignStepNum);

		mBtnSignin = (Button) findViewById(R.id.btnSignIn);
		mBtnCancel = (Button) findViewById(R.id.btnWithGuest);

		mLayStep1 = (RelativeLayout) findViewById(R.id.viewSignupStep1);
		mLayStep2_1 = (RelativeLayout) findViewById(R.id.viewSignupStep2_1);
		mLayStep2_2 = (RelativeLayout) findViewById(R.id.viewSignupStep2_2);
		mLayStep3 = (RelativeLayout) findViewById(R.id.viewSignupStep3);
		mLayStep4 = (RelativeLayout) findViewById(R.id.viewSignupStep4);
		mLayFinal = (RelativeLayout) findViewById(R.id.viewSignupFinal);

		mLayBottom = (RelativeLayout) findViewById(R.id.layBottom);

		mBtnStep1Next = (ImageButton) findViewById(R.id.btnStep1Next);
		mBtnStep21Next = (ImageButton) findViewById(R.id.btnStep21Next);
		mBtnStep21Prev = (ImageButton) findViewById(R.id.btnStep21Prev);
		mBtnStep22Next = (ImageButton) findViewById(R.id.btnStep22Next);
		mBtnStep22Prev = (ImageButton) findViewById(R.id.btnStep22Prev);
		mBtnStep3Next = (ImageButton) findViewById(R.id.btnStep3Next);
		mBtnStep3Prev = (ImageButton) findViewById(R.id.btnStep3Prev);

		mBtnSubmit = (Button) findViewById(R.id.btnSubmit);
		mBtnDisplaySchool = (Button) findViewById(R.id.btnDisplaySchool);
		mBtnSearchSchool = (Button) findViewById(R.id.btnSearchSchool);
		mBtnGoLesson = (Button) findViewById(R.id.btnGoLesson);

		mTxtSearchName = (AutoCompleteTextView)  findViewById(R.id.txtSearchName);
		mTxtSelectedSchoolName = (TextView) findViewById(R.id.txtSelectedSchoolName);
		mTxtSelectedSchoolAddr = (TextView) findViewById(R.id.txtSelectedSchoolAddr);
		mTxtManualSchool = (TextView) findViewById(R.id.txtManualSchool);

		mTxtSearchName.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.item_preplace));

		mImgPhoto = (ImageView) findViewById(R.id.imgUserPic);
		mImgEdit = (ImageView) findViewById(R.id.btnEditPic);

		mImgEdit.setOnClickListener(this);

		mBtnSignin.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);

		mBtnStep1Next.setOnClickListener(this);
		mBtnStep21Next.setOnClickListener(this);
		mBtnStep21Prev.setOnClickListener(this);
		mBtnStep22Next.setOnClickListener(this);
		mBtnStep22Prev.setOnClickListener(this);
		mBtnStep3Next.setOnClickListener(this);
		mBtnStep3Prev.setOnClickListener(this);
		mBtnSubmit.setOnClickListener(this);
		mBtnSearchSchool.setOnClickListener(this);
		mBtnDisplaySchool.setOnClickListener(this);
		mBtnGoLesson.setOnClickListener(this);
		mTxtManualSchool.setOnClickListener(this);

		mSpinSyllabus = (Spinner) findViewById(R.id.spinSyllabus);
		mSpinSyllabus.setPrompt("Select Board");

		mArraySyllabus = new ArrayList<String>();
		ArrayList<Syllabus> syllabuses = mDbMgr.getSyllabus();
		for (Syllabus item : syllabuses)
			mArraySyllabus.add(item.title);

		ArrayAdapter<String> adapterSyllabus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mArraySyllabus);
		adapterSyllabus.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mSpinSyllabus.setAdapter(adapterSyllabus);
		
		mSpinGrade = (Spinner) findViewById(R.id.spinGrade);
		mSpinGrade.setPrompt("Select Grade");

		mArrayGrade = new ArrayList<String>();
		ArrayList<Grade> grades = mDbMgr.getGrade();
		for (Grade item : grades)
			mArrayGrade.add(item.grade_name);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mArrayGrade);
		adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mSpinGrade.setAdapter(adapter);

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

		mItemSchool = new ArrayList<School>();

		mAdapterSchool = new SchoolAdapter(SignupActivity.this, R.layout.item_school, mItemSchool);
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

		mTxtYourname = (EditText) findViewById(R.id.txtYourName);
		mTxtMail = (EditText) findViewById(R.id.txtEmailAddr);
		mTxtPasswd = (EditText) findViewById(R.id.txtPassword);
		mTxtConfirmPwd = (EditText) findViewById(R.id.txtConfirmPassword);

		mTxtSchool = (EditText) findViewById(R.id.txtSchoolName);
		mTxtAddr = (EditText) findViewById(R.id.txtSchoolAddr);
		mTxtCity = (EditText) findViewById(R.id.txtSchoolCity);
		mTxtPostcode = (EditText) findViewById(R.id.txtSchoolPostalCode);

		mTxtSection = (EditText) findViewById(R.id.txtClassroom);

		mTxtFinalName = (TextView) findViewById(R.id.txtFinalName);
		mTxtFinalMsg = (TextView) findViewById(R.id.txtFinalMsg);
	}
	
	private void initViewGuestUser()
	{
		if (mPrefs.isLogIn())
			return;
		
		if (mScientist.name != null && !mScientist.name.isEmpty())
			mTxtDisplayName.setText(mScientist.name);
		
		mImgUser.imageView.setImageResource(R.drawable.empty_pic);
		if (mScientist.imageUrl != null && !mScientist.imageUrl.isEmpty())
			ImageLoader.getInstance().displayImage(mScientist.imageUrl, mImgUser, 0, 0);
	}

	private void initializeMap() {

		if (mGoogleMap == null) {

			mMapFragment = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapSchool));
			mGoogleMap = mMapFragment.getMap();			

			if (mGoogleMap == null) {
				Toast.makeText(this, "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
			} else {
				LatLng loc = new LatLng(mGps.getLatitude(), mGps.getLongitude()); 
				CameraPosition cp = new CameraPosition.Builder().target((loc)).zoom(10).build();
				mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));

				mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
				mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
				mGoogleMap.getUiSettings().setZoomControlsEnabled(false);		
				mGoogleMap.setMyLocationEnabled(true);
			}

			mMapFragment.getView().setVisibility(View.GONE);
		}
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
	
	private void onClickPopupMenu(String title)
	{
		if (title.equalsIgnoreCase("Profile")) {
			if (mPrefs.isLogIn())
				;
			else
				openGuestProfile();
		} else if (title.equalsIgnoreCase("Sign In")) {
			onClickSignIn();			
		}		
	}
	
	private void openGuestProfile()
	{
		if (mScientist == null || mScientist.name == null)
			return;
		
		String searchName = mScientist.name.replace(" ", "%20");
		String url = "http://www.google.com/search?q=" + searchName;
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		int id = v.getId();

		switch(id)
		{
		case R.id.btnSignIn:
			onClickSignIn();
			break;
		case R.id.btnWithGuest:
			onClickWithGuest();
			break;
		case R.id.btnStep1Next:
			if (!checkStep1()) {
				CustomToast.makeCustomToastShort(this, "Please input all the required information.");
			} else if (!checkPasswordLength()) {
				CustomToast.makeCustomToastShort(this, "Password should have a minimum of 6 characters.");
			} else {
				checkMailAddress(mTxtMail.getText().toString().trim());
//				setStep (Step.STEP2_1);
//
//				if (mItemSchool.size() == 0 && mPlaceSelected == null)
//					new LoadPlaces().execute("");
			}
			break;
		case R.id.btnStep21Next:
			if (checkStep2())
				setStep (Step.STEP3);
			else
				CustomToast.makeCustomToastShort(this, "Please input all the required information.");
			break;
		case R.id.btnStep21Prev:
			setStep (Step.STEP1);
			break;
		case R.id.txtManualSchool:
			setStep (Step.STEP2_2);
			break;
		case R.id.btnStep22Next:
			if (checkStep2())
				setStep (Step.STEP3);
			else
				CustomToast.makeCustomToastShort(this, "Please input all the required information.");
			break;
		case R.id.btnStep22Prev:
			setStep (Step.STEP1);
			break;
		case R.id.btnStep3Next:
			if (checkStep3())
				setStep (Step.STEP4);
			else
				CustomToast.makeCustomToastShort(this, "Please input all the required information.");
			break;
		case R.id.btnStep3Prev:
			setStep (Step.STEP2_1);
			break;
		case R.id.btnSubmit:
			onClickSubmit();
			//			setStep (Step.FINAL);
			break;
		case R.id.btnDisplaySchool:
			onClickDisplaySchool();
			break;
		case R.id.btnSearchSchool:
			onClickSearchSchool();
			break;
		case R.id.btnGoLesson:
//			onGoLessons();
			onClickSignIn();
			break;
		case R.id.btnEditPic:
			onClickAddPhoto();
			break;
		case R.id.imgUserIcon:
			mPopMenu.show();
			break;
		default:
			break;
		}
	}

	private void onClickAddPhoto()
	{
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(intent, PICK_FROM_ALBUM);
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

	private void onClickSignIn()
	{
		finish();
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);	
	}

	private void onGoLessons()
	{
		Intent intent = new Intent();
		intent.setClass(this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra("menu", 1);
		finish();
		startActivity(intent);		
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);	
	}

	private void onClickWithGuest()
	{
		Intent prevIntent = getIntent();
		prevIntent.putExtra("next_page", "grade");
		setResult(RESULT_OK, prevIntent);

		Intent intent = new Intent();
		intent.setClass(this, GradeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
		finish();
	}

	private void setStep (Step newStep)
	{
		if (newStep == mStep)
			return;

		mStep = newStep;

		showDetailedStep(mStep);
	}

	private void showDetailedStep(Step step)
	{
		mLayStep1.setVisibility(View.GONE);
		mLayStep2_1.setVisibility(View.GONE);
		mLayStep2_2.setVisibility(View.GONE);
		mLayStep3.setVisibility(View.GONE);
		mLayStep4.setVisibility(View.GONE);
		mLayFinal.setVisibility(View.GONE);

		mLayBottom.setVisibility(View.GONE);

		switch(step)
		{
		case STEP1:
			mLayStep1.setVisibility(View.VISIBLE);
			mTxtStepNum.setText("1");
			mLayBottom.setVisibility(View.VISIBLE);
			break;
		case STEP2_1:
			mLayStep2_1.setVisibility(View.VISIBLE);
			mTxtStepNum.setText("2");
			break;
		case STEP2_2:
			mLayStep2_2.setVisibility(View.VISIBLE);
			mTxtStepNum.setText("2");
			break;
		case STEP3:
			mLayStep3.setVisibility(View.VISIBLE);
			mTxtStepNum.setText("3");
			break;
		case STEP4:
			mLayStep4.setVisibility(View.VISIBLE);
			mTxtStepNum.setText("4");
			mTxtFinalName.setText(mTxtYourname.getText().toString());
			break;
		case FINAL:
			mLayFinal.setVisibility(View.VISIBLE);
			mTxtDisplayName.setText(mTxtYourname.getText().toString());
			mImgUser.imageView.setImageBitmap(mBpPhoto);
			String msg = getString(R.string.signup_success);
			msg = msg.replace("mail_address_value", mTxtMail.getText().toString().trim());
			mTxtFinalMsg.setText(msg);
			break;
		default:
			break;
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != Activity.RESULT_OK)
		{
			return;
		}

		switch(requestCode)
		{
		case PICK_FROM_ALBUM:

//			Uri photoUri = data.getData();
//
//			File original_file = Utility.getImageFile(this, photoUri);
//
//			photoUri = Utility.createSaveCropFile();
//			File cpoy_file = new File(photoUri.getPath()); 
//
//			Utility.copyFile(original_file , cpoy_file);
//
//			BitmapFactory.Options bfo = new BitmapFactory.Options();
//			bfo.inSampleSize = 2;
//			Bitmap bm = BitmapFactory.decodeFile(photoUri.getPath(), bfo);
//
//			mBpPhoto = Bitmap.createScaledBitmap(bm, 120, 120, true);
//			mImgPhoto.setImageBitmap(mBpPhoto);
			Uri photoUri = data.getData();
//			String path = Utility.getPath(this, photoUri);
			
			/*if (Build.VERSION.SDK_INT < 19) {

				File original_file = Utility.getImageFile(getActivity(), photoUri);
				if (original_file == null)
					return;

				photoUri = Utility.createSaveCropFile();
				File copy_file = new File(photoUri.getPath());

				Utility.copyFile(original_file , copy_file);

				BitmapFactory.Options bfo = new BitmapFactory.Options();
				bfo.inSampleSize = 2;
				Bitmap bm = BitmapFactory.decodeFile(photoUri.getPath(), bfo);

				copy_file.delete();

				if (bm == null)
					return;

				mBpPhoto = Bitmap.createScaledBitmap(bm, 120, 120, true);
				mImgPhoto2.setImageBitmap(mBpPhoto);
			} else */{
				ParcelFileDescriptor parcelFileDescriptor;
                try {
                    parcelFileDescriptor = getContentResolver().openFileDescriptor(photoUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    BitmapFactory.Options bfo = new BitmapFactory.Options();
    				bfo.inSampleSize = 2;
    				Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bfo);
                    parcelFileDescriptor.close();

                    if (image != null) {
                    	mBpPhoto = Bitmap.createScaledBitmap(image, 120, 120, true);
                    	mImgPhoto.setImageBitmap(mBpPhoto);
                    }    				

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }
			}

			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean bool;
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			bool = super.onKeyDown(keyCode, event);
		} else {
			finish();
			overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);			
			bool = true;
		}
		return bool;
	}	

	private void onClickSubmit()
	{		
		UserInfo userInfo = new UserInfo();

		//		makeTempUserInfo(userInfo);

		userInfo.name = mTxtYourname.getText().toString().trim();
		userInfo.mail = mTxtMail.getText().toString().trim();
		userInfo.passwd = mTxtPasswd.getText().toString();
		userInfo.confirmPasswd = mTxtConfirmPwd.getText().toString();

		if(mPlaceSelected == null) {
			userInfo.schoolName = mTxtSchool.getText().toString().trim();
			userInfo.address = mTxtAddr.getText().toString().trim();
			userInfo.city = mTxtCity.getText().toString().trim();
			userInfo.country = mSpinCountry.getSelectedItem().toString();
			userInfo.postcode = mTxtPostcode.getText().toString().trim();
		} else {
			userInfo.schoolName = mPlaceSelected.name;			
			userInfo.address = mPlaceSelected.vicinity;
			getSchoolInfoFromPlace(mPlaceSelected, userInfo);
		}
		
		userInfo.syllabus = mSpinSyllabus.getSelectedItem().toString();
		userInfo.syllabus_id = mDbMgr.getSyllabusByTitle(userInfo.syllabus).id;
		
		userInfo.grade = mSpinGrade.getSelectedItem().toString();
		userInfo.grade_id = mDbMgr.getGradeByName(userInfo.grade).id;
		userInfo.section = mTxtSection.getText().toString().trim();

		userInfo.photo = mBpPhoto;

		if (!checkSignupParameter(userInfo)) {
			CustomToast.makeCustomToastShort(this, "Please input all the required information.");
			setStep (Step.STEP3);
			return;
		}

		onSignUpTask(userInfo);

	}

	private boolean checkStep1()
	{
		if (!Utility.checkString(mTxtYourname.getText().toString().trim())
				|| !Utility.checkString(mTxtMail.getText().toString().trim())
				|| !Utility.checkString(mTxtPasswd.getText().toString())
				|| !Utility.checkString(mTxtConfirmPwd.getText().toString())
				)
			return false;

		if (mTxtPasswd.getText().toString().compareTo(mTxtConfirmPwd.getText().toString()) != 0)
			return false;

		return true;
	}

	private boolean checkPasswordLength()
	{
		if (mTxtPasswd.getText().toString().length() < 6)
			return false;

		return true;
	}

	private boolean checkStep2()
	{
		if (mStep == Step.STEP2_1) {
			if (mPlaceSelected == null)
				return false;
		}

		if (mStep == Step.STEP2_2) {
			if (!Utility.checkString(mTxtSchool.getText().toString().trim())
					|| !Utility.checkString(mTxtAddr.getText().toString().trim())
					|| !Utility.checkString(mTxtCity.getText().toString().trim())
					|| !Utility.checkString(mSpinCountry.getSelectedItem().toString())
					|| !Utility.checkString(mTxtPostcode.getText().toString().trim())
					)
				return false;
		}

		return true;
	}

	private boolean checkStep3()
	{		
		if (!Utility.checkString(mSpinSyllabus.getSelectedItem().toString())
				|| !Utility.checkString(mSpinGrade.getSelectedItem().toString())
				|| !Utility.checkString(mTxtSection.getText().toString().trim())
				)
			return false;

		return true;
	}

	private boolean checkSignupParameter(UserInfo userInfo)
	{
		if (!Utility.checkString(userInfo.name)
				|| !Utility.checkString(userInfo.mail)
				|| !Utility.checkString(userInfo.passwd)
				|| !Utility.checkString(userInfo.confirmPasswd)
				|| !Utility.checkString(userInfo.schoolName)
				|| !Utility.checkString(userInfo.address)
				|| !Utility.checkString(userInfo.city)
				|| !Utility.checkString(userInfo.country)
				|| !Utility.checkString(userInfo.postcode)
				|| !Utility.checkString(userInfo.syllabus)
				|| !Utility.checkString(userInfo.grade)
				|| !Utility.checkString(userInfo.section)
				|| userInfo.photo == null
				)
			return false;

		if (userInfo.passwd.compareTo(userInfo.confirmPasswd) != 0)
			return false;

		return true;
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

	private void onSignUpTask(final UserInfo userInfo){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SignUp(userInfo);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onSignUpResult(result, userInfo);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(SignupActivity.this);
				mWaitDlg.setMessage("SignUp...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}

	private void onSignUpResult(Object obj, UserInfo userInfo) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			setStep (Step.STEP3);
			return;
		}

		if (obj != null){
			SignUpResult resultBean = (SignUpResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				CustomToast.makeCustomToastLong(this, resultBean.message);
				GlobalValue.getInstance().setCurrentUser(userInfo);
				setStep (Step.FINAL);

			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
				setStep (Step.STEP3);
			}
		}		
	}
	
	private void checkMailAddress(final String mail){


		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.CheckMailAddress(mail);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onCheckMailResult(result);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}

	private void onCheckMailResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			CheckMailResult resultBean = (CheckMailResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS && resultBean.duplicated == false) {
				setStep (Step.STEP2_1);
				if (mItemSchool.size() == 0 && mPlaceSelected == null)
					new LoadPlaces().execute("");

			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
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
				mWaitDlg = new WaitDialog(SignupActivity.this);
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
			mWaitDlg = new WaitDialog(SignupActivity.this);
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
						mAlertDiag.showAlertDialog(SignupActivity.this, "Places Error",
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
			mAlertDiag.showAlertDialog(SignupActivity.this, "Search School",
					"Sorry no places found. Try to change the types of places",
					false);
		}
		else if(status.equals("UNKNOWN_ERROR"))
		{
			mAlertDiag.showAlertDialog(SignupActivity.this, "Search School",
					"Sorry unknown error occured.",
					false);
		}
		else if(status.equals("OVER_QUERY_LIMIT"))
		{
			mAlertDiag.showAlertDialog(SignupActivity.this, "Search School",
					"Sorry query limit to google places is reached",
					false);
		}
		else if(status.equals("REQUEST_DENIED"))
		{
			mAlertDiag.showAlertDialog(SignupActivity.this, "Search School",
					"Sorry error occured. Request is denied",
					false);
		}
		else if(status.equals("INVALID_REQUEST"))
		{
			//			mAlertDiag.showAlertDialog(SignupActivity.this, "Search School",
			//					"Sorry error occured. Invalid Request",
			//					false);
		}
		else
		{
			mAlertDiag.showAlertDialog(SignupActivity.this, "Search School",
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
