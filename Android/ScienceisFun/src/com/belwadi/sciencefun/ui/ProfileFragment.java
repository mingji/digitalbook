package com.belwadi.sciencefun.ui;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Grade;
import com.belwadi.sciencefun.entity.Syllabus;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.HyIconView;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.BaseTask;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.Server;
import com.belwadi.sciencefun.webservice.BaseTask.TaskListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class ProfileFragment extends Fragment implements OnClickListener
{
	private static final int PICK_FROM_ALBUM = 0;
	private static final int SCHOOL_ACTIVITY = 1;

	String mToken;
	WaitDialog mWaitDlg;

	RelativeLayout mLayGuest, mLayBody;

	TextView mTxtGuestMsg;
	
	RelativeLayout mLayProfileShow, mLayProfileEdit;

	EditText mTxtYourName, mTxtMail, mTxtSchoolName, mTxtSyllabus, mTxtGrade, mTxtSection;
	EditText mTxtAddr, mTxtCity, mTxtCountry, mTxtPostcode;
	EditText mTxtOldPwd, mTxtNewPwd, mTxtConfirmPwd;

	EditText mEditYourName, mEditMail, mEditSchoolName, mEditSection;
	EditText mEditAddr, mEditCity, mEditCountry, mEditPostcode;

	Button mBtnEditProfile, mBtnSaveProfile, mBtnCancel, mBtnChangePwd, mBtnGoSchool;

	private Spinner mSpinSyllabus;
	private ArrayList<String> mArraySyllabus;
	
	private Spinner mSpinGrade;
	private ArrayList<String> mArrayGrade;

	ImageView mImgEditPhoto;
	HyIconView mImgPhoto1, mImgPhoto2;
	Bitmap mBpPhoto;

	boolean isEditing = false;

	private AppPreferences mPrefs;
	private DatabaseManager mDbMgr;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_profile, null);

		mPrefs = new AppPreferences(getActivity());
		mDbMgr = new DatabaseManager(getActivity());

		mToken = mPrefs.getToken();

		UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();

		if (userInfo.photo != null)
			mBpPhoto = userInfo.photo;

		initView(view);

		if (mPrefs.isLogIn())
			updateProfile(GlobalValue.getInstance().getCurrentUser());

		return view;
	}

	private void initView(View view)
	{
		mLayGuest = (RelativeLayout) view.findViewById(R.id.layGuestMsg);
		mLayBody= (RelativeLayout) view.findViewById(R.id.layBody);

		mTxtGuestMsg = (TextView) view.findViewById(R.id.txtGuestMsg);
		initGuestMsg();
		
		mLayProfileShow = (RelativeLayout) view.findViewById(R.id.layProfileShow);
		mLayProfileEdit = (RelativeLayout) view.findViewById(R.id.layProfileEdit);

		mLayProfileShow.setVisibility(View.VISIBLE);
		mLayProfileEdit.setVisibility(View.GONE);

		mTxtYourName = (EditText) view.findViewById(R.id.txtYourname);
		mTxtMail = (EditText) view.findViewById(R.id.txtMail);
		mTxtSchoolName = (EditText) view.findViewById(R.id.txtSchoolname);
		mTxtSyllabus = (EditText) view.findViewById(R.id.txtSyllabus);
		mTxtGrade = (EditText) view.findViewById(R.id.txtGrade);
		mTxtSection = (EditText) view.findViewById(R.id.txtSection);
		mTxtAddr = (EditText) view.findViewById(R.id.txtSchoolAddr);
		mTxtCity = (EditText) view.findViewById(R.id.txtSchoolCity);
		mTxtCountry = (EditText) view.findViewById(R.id.txtCountry);
		mTxtPostcode = (EditText) view.findViewById(R.id.txtSchoolCode);

		mTxtOldPwd = (EditText) view.findViewById(R.id.txtOldPwd);
		mTxtNewPwd = (EditText) view.findViewById(R.id.txtNewPwd);
		mTxtConfirmPwd = (EditText) view.findViewById(R.id.txtConfirmPwd);

		mEditYourName = (EditText) view.findViewById(R.id.editYourname);
		mEditMail = (EditText) view.findViewById(R.id.editMail);
		mEditSchoolName = (EditText) view.findViewById(R.id.editSchoolname);
		mEditSection = (EditText) view.findViewById(R.id.editSection);
		mEditAddr = (EditText) view.findViewById(R.id.editSchoolAddr);
		mEditCity = (EditText) view.findViewById(R.id.editSchoolCity);
		mEditCountry = (EditText) view.findViewById(R.id.editCountry);
		mEditPostcode = (EditText) view.findViewById(R.id.editSchoolCode);

		mBtnEditProfile = (Button) view.findViewById(R.id.btnEditProfile);
		mBtnSaveProfile = (Button) view.findViewById(R.id.btnSaveProfile);
		mBtnCancel = (Button) view.findViewById(R.id.btnCancelProfile);
		mBtnChangePwd = (Button) view.findViewById(R.id.btnChangePwd);
		mBtnGoSchool = (Button) view.findViewById(R.id.btnGoSchool);

		mImgPhoto1 = (HyIconView) view.findViewById(R.id.imgUserPic);
		mImgPhoto2 = (HyIconView) view.findViewById(R.id.imgUserPic2);
		mImgEditPhoto = (ImageView) view.findViewById(R.id.imgEditPic);

		mBtnEditProfile.setOnClickListener(this);
		mBtnSaveProfile.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mBtnChangePwd.setOnClickListener(this);
		mBtnGoSchool.setOnClickListener(this);
		mImgEditPhoto.setOnClickListener(this);

		mSpinSyllabus = (Spinner) view.findViewById(R.id.spinSyllabus);
		mSpinSyllabus.setPrompt("Select Board");

		mArraySyllabus = new ArrayList<String>();
		ArrayList<Syllabus> syllabuses = mDbMgr.getSyllabus();
		for (Syllabus item : syllabuses)
			mArraySyllabus.add(item.title);

		ArrayAdapter<String> adapterSyllabus = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mArraySyllabus);
		adapterSyllabus.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mSpinSyllabus.setAdapter(adapterSyllabus);
		
		mSpinGrade = (Spinner) view.findViewById(R.id.spinGrade);
		mSpinGrade.setPrompt("Select Grade");

		mArrayGrade = new ArrayList<String>();
		ArrayList<Grade> grades = mDbMgr.getGrade();
		for (Grade item : grades)
			mArrayGrade.add(item.grade_name);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mArrayGrade);
		adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mSpinGrade.setAdapter(adapter);
	}
	
	public void resetView() 
	{
		if (mLayGuest == null || mLayBody == null)
			return;		
		
		if (mPrefs.isLogIn()) {
			mLayGuest.setVisibility(View.GONE);
			mLayBody.setVisibility(View.VISIBLE);

			UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();
			if (userInfo.photo != null)
				mBpPhoto = userInfo.photo;

			updateProfile(userInfo);			
		} else {
			mLayGuest.setVisibility(View.VISIBLE);
			mLayBody.setVisibility(View.GONE);
		}
		

	}
	
	private void initGuestMsg()
	{
		String strNote = "You have not enrolled in any class. \nSign In  or  Sign Up for Free.";
		SpannableString ss = new SpannableString(strNote);

		ClickableSpan signinSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				HomeActivity parent = (HomeActivity)getActivity();
				parent.startSigninActivity();
			}
		};

		ClickableSpan signupSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				HomeActivity parent = (HomeActivity)getActivity();
				parent.startSignupActivity();
			}
		};

		int start1 = strNote.indexOf("Sign In");
		int start2 = strNote.indexOf("Sign Up");		
		ss.setSpan(signinSpan, start1, start1 + "Sign In".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ss.setSpan(signupSpan, start2, start2 + "Sign Up".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTxtGuestMsg.setText(ss);
		mTxtGuestMsg.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		int id = v.getId();

		switch(id)
		{
		case R.id.btnEditProfile:
			onClickEditProfile();
			break;
		case R.id.btnSaveProfile:
			onClickSaveProfile();
			break;
		case R.id.btnCancelProfile:
			onClickCancel();
			break;
		case R.id.btnChangePwd:
			onClickChangePassword();
			break;
		case R.id.imgEditPic:
			onClickEditPhoto();
			break;
		case R.id.btnGoSchool:
			onClickGoSchool();
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

			Uri photoUri = data.getData();
//			String path = Utility.getPath(getActivity(), photoUri);
			
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
                    parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(photoUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    BitmapFactory.Options bfo = new BitmapFactory.Options();
    				bfo.inSampleSize = 2;
    				Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bfo);
                    parcelFileDescriptor.close();

                    if (image != null) {
                    	mBpPhoto = Bitmap.createScaledBitmap(image, 120, 120, true);
                    	mImgPhoto2.imageView.setImageBitmap(mBpPhoto);
                    }    				

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }
			}

			break;

		case SCHOOL_ACTIVITY:

			String schoolName = data.getStringExtra("schoolname");
			String address = data.getStringExtra("address");
			String city = data.getStringExtra("city");
			String country = data.getStringExtra("country");
			String postalcode = data.getStringExtra("postalcode");

			mEditSchoolName.setText(schoolName);
			mEditAddr.setText(address);
			mEditCity.setText(city);
			mEditCountry.setText(country);
			mEditPostcode.setText(postalcode);

			break;
		}
	}

	private void onClickGoSchool()
	{
		Intent intent = new Intent();
		intent.setClass(getActivity(), SchoolActivity.class);
		startActivityForResult(intent, SCHOOL_ACTIVITY);
		getActivity().overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}

	private void onClickEditPhoto()
	{
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		//		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(intent, PICK_FROM_ALBUM);
	}

	private void onClickEditProfile()
	{
		mLayProfileShow.setVisibility(View.GONE);
		mLayProfileEdit.setVisibility(View.VISIBLE);
	}

	private void onClickCancel()
	{
		mLayProfileShow.setVisibility(View.VISIBLE);
		mLayProfileEdit.setVisibility(View.GONE);
	}

	private void onClickChangePassword()
	{
		String oldPwd, newPwd, confirm;

		oldPwd = mTxtOldPwd.getText().toString();
		newPwd = mTxtNewPwd.getText().toString();
		confirm = mTxtConfirmPwd.getText().toString();

		if (!checkString(oldPwd)
				|| !checkString(newPwd)
				|| !checkString(confirm)) {
			CustomToast.makeCustomToastShort(getActivity(), "Please enter the passwords");
			return;
		}

		if (newPwd.compareTo(confirm) != 0) {
			CustomToast.makeCustomToastShort(getActivity(), "New passwords not match.");
			return;
		}

		onChangePwdTask(mToken, oldPwd, newPwd, confirm);
	}

	private void onClickSaveProfile()
	{
		UserInfo userInfo = new UserInfo();

		userInfo._id = GlobalValue.getInstance().getCurrentUser()._id;
		userInfo.name = mEditYourName.getText().toString();
		userInfo.mail = mEditMail.getText().toString();

		userInfo.schoolName = mEditSchoolName.getText().toString();
		userInfo.address = mEditAddr.getText().toString();
		userInfo.city = mEditCity.getText().toString();
		userInfo.country = mEditCountry.getText().toString();
		userInfo.postcode = mEditPostcode.getText().toString();

		userInfo.syllabus = mSpinSyllabus.getSelectedItem().toString();
		userInfo.syllabus_id = mDbMgr.getSyllabusByTitle(userInfo.syllabus).id;
		
		userInfo.grade = mSpinGrade.getSelectedItem().toString();
		userInfo.grade_id = mDbMgr.getGradeByName(userInfo.grade).id;
		userInfo.section = mEditSection.getText().toString();

		mBpPhoto = ((BitmapDrawable)mImgPhoto2.imageView.getDrawable()).getBitmap();
		userInfo.photo = mBpPhoto; 

		if (!checkProfileParameter(userInfo)) {
			CustomToast.makeCustomToastShort(getActivity(), "Please input all the required information.");
			return;
		}

		onSaveProfileTask(mToken, userInfo);
	}

	private boolean checkProfileParameter(UserInfo userInfo)
	{
		if (!checkString(userInfo.name)
				|| !checkString(userInfo.schoolName)
//				|| !checkString(userInfo.address)
//				|| !checkString(userInfo.city)
//				|| !checkString(userInfo.country)
//				|| !checkString(userInfo.postcode)
				|| !checkString(userInfo.syllabus)
				|| !checkString(userInfo.grade)
				|| !checkString(userInfo.section)
				|| userInfo.photo == null
				)
			return false;

		return true;
	}

	private boolean checkString (String str)
	{
		if (str == null || str.isEmpty())
			return false;

		return true;
	}


	public void updateProfile (UserInfo userInfo)
	{
		mTxtYourName.setText(userInfo.name);
		mTxtMail.setText(userInfo.mail);

		mTxtSchoolName.setText(userInfo.schoolName);
		Syllabus syllabus = mDbMgr.getSyllabus(userInfo.syllabus_id);
		if (syllabus != null)
			mTxtSyllabus.setText(syllabus.title);
		else
			mTxtSyllabus.setText("--");
		
		Grade grade = mDbMgr.getGrade(userInfo.grade_id);
		if (grade != null)
			mTxtGrade.setText(mDbMgr.getGrade(userInfo.grade_id).grade_name);
		else
			mTxtGrade.setText("--");
		
		mTxtSection.setText(userInfo.section);

		mTxtAddr.setText(userInfo.address);
		mTxtCity.setText(userInfo.city);
		mTxtCountry.setText(userInfo.country);
		mTxtPostcode.setText(userInfo.postcode);

		if (mBpPhoto != null) {
			mImgPhoto1.imageView.setImageBitmap(mBpPhoto);
		} else {
			if (userInfo.photo_url != null && !userInfo.photo_url.isEmpty())
				ImageLoader.getInstance().displayImage(userInfo.photo_url, mImgPhoto1, 0, 0);
			else
				mImgPhoto1.imageView.setImageResource(R.drawable.empty_pic);
		}

		mEditYourName.setText(userInfo.name);
		mEditMail.setText(userInfo.mail);

		mEditSchoolName.setText(userInfo.schoolName);
		
		if (syllabus != null)
			mSpinSyllabus.setSelection(mArraySyllabus.indexOf(syllabus.title));
		if (grade != null)
			mSpinGrade.setSelection(mArrayGrade.indexOf(grade.grade_name));
		
		mEditSection.setText(userInfo.section);

		mEditAddr.setText(userInfo.address);
		mEditCity.setText(userInfo.city);
		mEditCountry.setText(userInfo.country);
		mEditPostcode.setText(userInfo.postcode);
		
		if (mBpPhoto != null) {
			mImgPhoto2.imageView.setImageBitmap(mBpPhoto);
		} else {
			if (userInfo.photo_url != null && !userInfo.photo_url.isEmpty())
				ImageLoader.getInstance().displayImage(userInfo.photo_url, mImgPhoto2, 0, 0);
			else
				mImgPhoto2.imageView.setImageResource(R.drawable.empty_pic);
		}
	}

	private void onUpdateUserInfo(UserInfo userInfo)
	{
		updateProfile (userInfo);

		HomeActivity parentActivity = (HomeActivity) getActivity();
		parentActivity.onUpdateUserInfo();
		parentActivity.updateContents();
	}

	private void setCurrentGrade(String userId)
	{
		UserInfo user = mDbMgr.getUser(userId);
		Grade grade = mDbMgr.getGrade(user.grade_id);				
		GlobalValue.getInstance().setCurrentGrade(grade);
	}
	
	private void setCurrentSyllabus(String userId)
	{
		UserInfo user = mDbMgr.getUser(userId);
		Syllabus syllabus = mDbMgr.getSyllabus(user.syllabus_id);				
		GlobalValue.getInstance().setCurrentSyllabus(syllabus);
	}

	private void updateUserTable(UserInfo userInfo)
	{
		UserInfo tmp = mDbMgr.getUser(userInfo._id);
		Syllabus syllabus = mDbMgr.getSyllabusByTitle(userInfo.syllabus);
		Grade grade = mDbMgr.getGradeByName(userInfo.grade);

		tmp.name = userInfo.name;
		tmp.mail = userInfo.mail;
		tmp.schoolName = userInfo.schoolName;
		tmp.address = userInfo.address;
		tmp.city = userInfo.city;
		tmp.country = userInfo.country;
		tmp.postcode = userInfo.postcode;
		tmp.syllabus_id = syllabus.id;
		tmp.grade_id = grade.id;
		tmp.section = userInfo.section;

		mDbMgr.editUserItem(tmp);

	}

	private void onSaveProfileTask(final String token, final UserInfo userInfo){


		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SaveProfile(token, userInfo);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onSaveProfileResult(result, userInfo);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(getActivity());
				mWaitDlg.setMessage("Saving...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}

	private void onSaveProfileResult(Object obj, UserInfo userInfo) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			SaveProfilepResult resultBean = (SaveProfilepResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);

				GlobalValue.getInstance().setCurrentUser(userInfo);
				updateUserTable(userInfo);
				setCurrentSyllabus(userInfo._id);
				setCurrentGrade(userInfo._id);

				onUpdateUserInfo(userInfo);
				mLayProfileShow.setVisibility(View.VISIBLE);
				mLayProfileEdit.setVisibility(View.GONE);
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}		
	}

	private void onChangePwdTask(final String token, final String oldPwd, final String newPwd, final String confirm){


		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.ChangePassword(token, oldPwd, newPwd, confirm);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onChangePwdResult(result);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(getActivity());
				mWaitDlg.setMessage("Changing...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}

	private void onChangePwdResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			ChangePwdResult resultBean = (ChangePwdResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
				mTxtOldPwd.setText("");
				mTxtNewPwd.setText("");
				mTxtConfirmPwd.setText("");
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}		
	}

}
