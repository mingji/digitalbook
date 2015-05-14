package com.belwadi.sciencefun.ui;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Grade;
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

public class FriendProfileActivity extends Activity implements OnClickListener, CompoundButton.OnCheckedChangeListener {

	DatabaseManager mDbMgr;
	AppPreferences mPrefs;

	private UserInfo mFriendUser, mCurrentUser;

	private Button mBtnBack;
	private TextView mTxtUserName, mTxtFriendName, mTxtSchool, mTxtGrade, mTxtSection, mTxtMail, mTxtReport;
	private ImageView mImgUser;
	private HyIconView mImgFriend;
	
	PopupMenu mPopMenu;

	private ToggleButton mSwhReceive;
	
	private String mReportType;

	WaitDialog mWaitDlg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_friend_profile);

		mDbMgr = new DatabaseManager(this);
		mPrefs = new AppPreferences(this);

		String userId = getIntent().getStringExtra("user_id");
		mReportType = getIntent().getStringExtra("report_type");
		if (mReportType == null)
			mReportType = "class";

		mCurrentUser = GlobalValue.getInstance().getCurrentUser();
		mFriendUser = mDbMgr.getUser(userId);
		Grade grade = mDbMgr.getGrade(mFriendUser.grade_id);
		if (grade != null)
			mFriendUser.grade = grade.grade_name;
		else
			mFriendUser.grade = "--";
		
		initView();
		initViewPopupMenu();
	}

	private void initView()
	{
		mTxtUserName = (TextView) findViewById(R.id.txtUserName);
		mTxtFriendName = (TextView) findViewById(R.id.txtFriendName);
		mTxtSchool = (TextView) findViewById(R.id.txtSchool);
		mTxtGrade = (TextView) findViewById(R.id.txtGrade);
		mTxtSection = (TextView) findViewById(R.id.txtClass);
		mTxtMail = (TextView) findViewById(R.id.txtMail);

		mTxtReport = (TextView) findViewById(R.id.txtReport);
		mTxtReport.setOnClickListener(this);

		mTxtUserName.setText(mCurrentUser.name);
		mTxtFriendName.setText(mFriendUser.name);
		mTxtSchool.setText(mFriendUser.schoolName);
		mTxtGrade.setText("Grade " + mFriendUser.grade);
		mTxtSection.setText("Section " + mFriendUser.section);
		mTxtMail.setText(mFriendUser.mail);

		mImgUser = (ImageView) findViewById(R.id.imgUserIcon);
		mImgFriend = (HyIconView) findViewById(R.id.imgFriendPic);

		mImgUser.setOnClickListener(this);

		if (mCurrentUser.photo != null)
			mImgUser.setImageBitmap(mCurrentUser.photo);
		else
			mImgUser.setImageResource(R.drawable.empty_pic);

		if (mFriendUser.photo_url != null && !mFriendUser.photo_url.isEmpty())
			ImageLoader.getInstance().displayImage(mFriendUser.photo_url, mImgFriend, 0, 0);
		else
			mImgFriend.imageView.setImageResource(R.drawable.empty_pic);

		mSwhReceive = (ToggleButton) findViewById(R.id.switchReceive);
		mSwhReceive.setChecked(isReceiveUpdate());
		mSwhReceive.setOnCheckedChangeListener(this);		

		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(this);
		
		mPopMenu = new PopupMenu(this, mImgUser);
		mPopMenu.getMenuInflater().inflate(R.menu.popup, mPopMenu.getMenu());
		mPopMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				
				onClickPopupMenu(item.getTitle().toString());
				
				return true;
			}
		});
	}
	
	private void initViewPopupMenu()
	{
		MenuItem item = mPopMenu.getMenu().getItem(1);
		
		if (mPrefs.isLogIn())
			item.setTitle("Sign Out");
		else
			item.setTitle("Sign In");
	}

	private boolean isReceiveUpdate()
	{
		UserInfo currentUser = mDbMgr.getUser(mCurrentUser._id);

		if (currentUser == null)
			return false;

		if (mFriendUser.update_on_user.contains(currentUser._id))
			return true;
		else
			return false;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		int id = view.getId();

		switch (id)
		{
		case R.id.txtReport:
			onClickReportUser();
			break;
		case R.id.btnBack:
			finishActivity("");
			break;
		case R.id.imgUserIcon:
			mPopMenu.show();
//			if (mPrefs.isLogIn())
//				finishActivity("profile");
//			else
//				finishActivity("sign");
		default:

			break;
		}
	}
	
	private void onClickPopupMenu (String result)
	{
		if (result.equalsIgnoreCase("Profile")) {
			finishActivity("Profile");			
		} else if (result.equalsIgnoreCase("Sign In")) {
			finishActivity("Sign In");			
		} else if (result.equalsIgnoreCase("Sign Out")) {
			finishActivity("Sign Out");
		}
	}

	private void finishActivity(String goPage)
	{
		Intent prevIntent = getIntent();

		prevIntent.putExtra("go_page", goPage);

		setResult(RESULT_OK, prevIntent);

		finish();
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView != mSwhReceive)
			return;

		setReceiveTask(mPrefs.getToken(), mFriendUser._id, mSwhReceive.isChecked());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean bool;
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			bool = super.onKeyDown(keyCode, event);
		} else {
			finishActivity("");
			bool = true;
		}
		return bool;
	}

	private void onClickReportUser()
	{
		ReportUserDialog reportDiag =new ReportUserDialog(this, this, mFriendUser._id, mReportType);
		reportDiag.show();
	}

	private void setReceiveTask(final String token, final String friendId, final boolean isReceive){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SetReceive(token, friendId, isReceive);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onSetReceiveResult(result);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(FriendProfileActivity.this);
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onSetReceiveResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			return;
		}

		if (obj != null){
			SetReceiveResult resultBean = (SetReceiveResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				getUserList();
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void getUserList(){

		final long checksum = mDbMgr.getUserCheckSum();

		Log.d ("Debug", "Chceksum : " + checksum);

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.GetAllUserList(checksum, 0, 0);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				getUserListResult(result);
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

	private void getUserListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			GetAllUserLstResult resultBean = (GetAllUserLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				mDbMgr.resetUserTable();

				for (UserResult item : resultBean.users)
				{
					mDbMgr.addUserItem(item);
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

	}
}
