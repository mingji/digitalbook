package com.belwadi.sciencefun.ui;


import static com.belwadi.sciencefun.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.belwadi.sciencefun.CommonUtilities.SENDER_ID;
import static com.belwadi.sciencefun.CommonUtilities.TAG;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.*;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.ui.MainActivity.State;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.CustomViewPager;
import com.belwadi.sciencefun.view.HyIconView;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.BaseTask;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.Server;
import com.belwadi.sciencefun.webservice.WebApiCallback;
import com.belwadi.sciencefun.webservice.WebApiInstance;
import com.belwadi.sciencefun.webservice.BaseTask.TaskListener;
import com.belwadi.sciencefun.webservice.WebApiInstance.Type;
import com.google.android.gcm.GCMRegistrar;

public class HomeActivity extends SherlockFragmentActivity implements OnClickListener, WebApiCallback{

	private AppPreferences mPrefs;
	private DatabaseManager mDbMgr;

	TabHost mTabHost;
	CustomViewPager mViewPager;
	TabsAdapter mTabsAdapter;

	HorizontalScrollView mScrollTabs;

	// title Bar
	Button mBtnMenu;
	TextView mTxtGrade, mTxtSyllabus, mTxtUserName;
	HyIconView mImgUser;
	
	Scientist mScientist;

	PopupMenu mPopMenu;

	private State mState;

	public enum State {
		PROFILE, LESSONS, CLASS, /*UPDATES,*/ SIGNINOUT, ABOUT
	};

	WaitDialog mWaitDlg;

	int linkCnt, chapterCnt, conceptCnt, videoCnt, noteCnt, refCnt;
	int mProgHit = 0;

	int FLAG_LINK		= 0x01;
	int FLAG_CHAPTER	= 0x02;	
	int FLAG_CONCEPTS	= 0x04;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Sherlock___Theme_Light);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);

		mPrefs = new AppPreferences(this);
		mDbMgr = new DatabaseManager(this);
		
		mScientist = GlobalValue.getInstance().getScientist(this);

		linkCnt = chapterCnt = conceptCnt = videoCnt = noteCnt = refCnt = 0;

		int menuIndex = getIntent().getIntExtra("menu", 1);

		State newState;
		if (menuIndex < State.values().length)
			newState = State.values()[menuIndex];
		else
			newState = State.LESSONS;

		initView();

		if (mPrefs.isLogIn()) {
			UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();

			if (userInfo.isValid == false) {
				String token = mPrefs.getToken();
				getProfileTask (token);
			} else {
				GetPhotoTask(userInfo.photo_url);
				onUpdateUserInfo();
			}
		}

//		initPushNotification();

		setState(newState);
		setCaptionText();        

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

//		unregisterReceiver (mHandleMessageReceiver);
//		GCMRegistrar.onDestroy(getApplicationContext());
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (!mPrefs.isLogIn())
			initViewGuestUser();
		
		initViewPopupMenu();
//		viewTabWidget();
	}

	@Override
	protected void onPause() {
		super.onPause();		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}    

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initView()
	{
		mScrollTabs = (HorizontalScrollView) findViewById(R.id.scrollTabs);

		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (CustomViewPager)findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager, mScrollTabs);

		mTabsAdapter.addTab(mTabHost.newTabSpec("profile").setIndicator(getTabIndicator("Profile")),
				ProfileFragment.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec("lessons").setIndicator(getTabIndicator("Lessons")),
				LessonFragment.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec("myclass").setIndicator(getTabIndicator("My Class")),
				ClassFragment.class, null);

		mTxtGrade = (TextView) findViewById(R.id.txtGrade);
		mTxtSyllabus = (TextView) findViewById(R.id.txtSyllabus);

		mTxtUserName = (TextView) findViewById(R.id.txtUserName);

		mImgUser = (HyIconView) findViewById(R.id.imgUserIcon);
		mImgUser.setOnClickListener(this);

		mPopMenu = new PopupMenu(this, mImgUser);
		mPopMenu.getMenuInflater().inflate(R.menu.popup, mPopMenu.getMenu());
		mPopMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				
				onClickPopupMenu(item.getTitle().toString());
				
				return true;
			}
		});

		mBtnMenu = (Button) findViewById(R.id.btnMenu);
		mBtnMenu.setOnClickListener(this);
	}
	
	private void initViewGuestUser()
	{
		if (mPrefs.isLogIn())
			return;
		
		if (mScientist.name != null && !mScientist.name.isEmpty())
			mTxtUserName.setText(mScientist.name);
		
		mImgUser.imageView.setImageResource(R.drawable.empty_pic);
		if (mScientist.imageUrl != null && !mScientist.imageUrl.isEmpty())
			ImageLoader.getInstance().displayImage(mScientist.imageUrl, mImgUser, 0, 0);
	}
	
	private void initViewPopupMenu()
	{
		MenuItem item = mPopMenu.getMenu().getItem(1);
		
		if (mPrefs.isLogIn())
			item.setTitle("Sign Out");
		else
			item.setTitle("Sign In");
	}

	private View getTabIndicator(String title) 
	{
		View tabView = LayoutInflater.from(this).inflate (R.layout.tab_view, null);
		TextView textView = (TextView) tabView.findViewById(R.id.tabLabel);
		textView.setText(title);
//		endrobolt : tabView.setEnabled(false); 
		return tabView;
	}
	
	private void viewTabWidget()
	{
		TabWidget tabWidget = mTabHost.getTabWidget();
		TextView txtProfile = (TextView) tabWidget.getChildAt(getIndexFromState(State.PROFILE))
				.findViewById(R.id.tabLabel);
		TextView txtClass = (TextView) tabWidget.getChildAt(getIndexFromState(State.CLASS))
				.findViewById(R.id.tabLabel);
		
		if (!mPrefs.isLogIn()) {			
			tabWidget.setEnabled(false);
			mViewPager.setPagingEnabled(false);
			txtProfile.setTextColor(getResources().getColor(R.color.lightgray));
			txtClass.setTextColor(getResources().getColor(R.color.lightgray));
			
			setState(State.LESSONS);
		} else {
			tabWidget.setEnabled(true);
			mViewPager.setPagingEnabled(true);
			txtProfile.setTextColor(getResources().getColor(R.color.black));
			txtClass.setTextColor(getResources().getColor(R.color.black));
		}
		
	}

	private void setCaptionText()
	{
		Syllabus syllabus = GlobalValue.getInstance().getCurrentSyllabus();
		if (syllabus != null) {
			mTxtSyllabus.setText(syllabus.title);
		}

		Grade grade = GlobalValue.getInstance().getCurrentGrade();
		if (grade != null) {
			mTxtGrade.setText(grade.grade_name + " th Grade");
		}
	}

	public static State getStateFromIndex(int index)
	{
		State state = State.LESSONS;

		if (State.values().length <= index)
			state = State.LESSONS;
		else
			state = State.values()[index];

		return state;
	}

	private int getIndexFromState(State state)
	{
		String name = state.name();
		int index = State.valueOf(name).ordinal();

		return index;
	}

	private void initPushNotification()
	{
		checkNotNull(SENDER_ID, "SENDER_ID");
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		registerReceiver(mHandleMessageReceiver,
				new IntentFilter(DISPLAY_MESSAGE_ACTION));        

		final String regId = GCMRegistrar.getRegistrationId(getApplicationContext());
		Log.i(TAG, "regId:" + regId);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(getApplicationContext(), SENDER_ID);
		} else {
			Log.i(TAG, "regId:" + regId);
		}
	}

	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(
					getString(R.string.error_config, name));
		}
	}

	private final BroadcastReceiver mHandleMessageReceiver =
			new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			//            mDisplay.append(newMessage + "\n");
		}
	};
	
	public void porgressResult (String result)
	{
		if (result.equalsIgnoreCase("Profile")) {
			if (mPrefs.isLogIn())
				setState(State.PROFILE);
			else
				openGuestProfile();
		} else if (result.equalsIgnoreCase("Sign In")) {
			startSigninActivity();			
		} else if (result.equalsIgnoreCase("Sign Out")) {
			onSignOutTask();
		} else if (result.equalsIgnoreCase("Sign Up")) {
			startSignupActivity();
		}		
	}
	
	private void onClickPopupMenu(String title)
	{
		porgressResult(title);		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub		
		int id = v.getId();

		switch(id)
		{
		case R.id.btnMenu:
			break;
		case R.id.imgUserIcon:
			mPopMenu.show();
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
			if (mPrefs.isLogIn())
				showCloseDialog();
			else
				goBackToGradeActivity();
			bool = true;
		}
		return bool;
	}

	private void showCloseDialog() {
		new AlertDialog.Builder(this)
		.setTitle("Exit App")
		.setMessage("Are you sure you want to exit?")
		.setNegativeButton("Yes",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
				finishApp();
			}
		})
		.setPositiveButton("No",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {
			}
		})
		.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					return false;
				}
				return false;
			}
		}).show();
	}

	private void finishApp() {
		finish();
		Log.i("ScienceisFun", "close application");		
//		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void setState(State newState)	
	{
		State oldState = mState;	

		if (oldState == newState) {
			return;
		}

		mState = newState;

		mViewPager.setCurrentItem(getIndexFromState(mState));
	}

	public void onUpdateUserInfo()
	{
		UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();

		if (userInfo.name != null && !userInfo.name.isEmpty())
			mTxtUserName.setText(userInfo.name);

		if (userInfo.photo != null)
			mImgUser.imageView.setImageBitmap(userInfo.photo);
		else
			mImgUser.imageView.setImageResource(R.drawable.empty_pic);

		setCaptionText();
		
		ClassFragment fragment = (ClassFragment) mTabsAdapter.classFrag;
		if (fragment != null)
			fragment.resetView();
	}

	public void AfterSignIn()
	{
		String token = mPrefs.getToken();
		getProfileTask (token);

		setState(State.LESSONS);

		updateContents();
	}

	private void AfterSignOut()
	{
		UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();

		userInfo.isValid = false;
		
		goBackToGradeActivity();
	}

	private void setCurrentSyllabus(String userId)
	{
		UserInfo user = mDbMgr.getUser(userId);
		Syllabus syllabus = mDbMgr.getSyllabus(user.syllabus_id);				
		GlobalValue.getInstance().setCurrentSyllabus(syllabus);
	}

	private void setCurrentGrade(String userId)
	{
		UserInfo user = mDbMgr.getUser(userId);
		Grade grade = mDbMgr.getGrade(user.grade_id);				
		GlobalValue.getInstance().setCurrentGrade(grade);
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
	
	private void goBackToGradeActivity()
	{
		Intent intent = new Intent();
		intent.setClass(this, GradeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
		finish();
	}
	
	public void startSigninActivity() {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(this, SigninActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}
	
	public void startSignupActivity() {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(this, SignupActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}
	
	public void updateContents()
	{
		mWaitDlg = new WaitDialog(HomeActivity.this);
		mWaitDlg.setMessage("loading 0%");
		mWaitDlg.show();
		
		mProgHit = 0;
		
		getConceptLinkList(linkCnt);
		getChapterList(chapterCnt);
		getConceptList(conceptCnt);
	}
	
	private void setWaitingMessage (String msg)
	{
		if (mWaitDlg == null)
			return;

		mWaitDlg.setMessage(msg);
	}

	private void setLoadPercent(Type type)
	{
		int hitCount =  0;
		String msg;

		switch (type)
		{
		case GET_LINKS:
			mProgHit |= FLAG_LINK;			
			break;
		case GET_CHAPTERS:
			mProgHit |= FLAG_CHAPTER;			
			break;
		case GET_CONCEPTS:
			mProgHit |= FLAG_CONCEPTS;			
			break;
		default:
			break;
		}

		if ((mProgHit & FLAG_LINK) == FLAG_LINK)
			hitCount ++;
		if ((mProgHit & FLAG_CHAPTER) == FLAG_CHAPTER)
			hitCount ++;
		if ((mProgHit & FLAG_CONCEPTS) == FLAG_CONCEPTS)
			hitCount ++;
		
		msg = "loading " + hitCount * 100 / 3 + "%";
		setWaitingMessage (msg);
		
		if (hitCount == 3) {
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			
			LessonFragment fragment = (LessonFragment) mTabsAdapter.lessonFrag;
			if (fragment != null)
				fragment.updateChapter();
		}
	}
	
	private void onSignOutTask(){
		
		final String token = mPrefs.getToken();

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SignOut(token);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onSignOutResult(result);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(HomeActivity.this);
				mWaitDlg.setMessage("SignOut...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}

	private void onSignOutResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			SignOutResult resultBean = (SignOutResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				CustomToast.makeCustomToastShort(this, resultBean.message);
				
				mPrefs.setToken("");				
				mPrefs.setLogIn(false);
				mPrefs.setUserId("");

				AfterSignOut();
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}		
	}

	private void getProfileTask(final String token){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.GetProfile(token);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onGetProfileResult(result);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(HomeActivity.this);
				mWaitDlg.setMessage("Get Profile ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onGetProfileResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			GetProfileResult resultBean = (GetProfileResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				GetPhotoTask(resultBean.profile.photo);

				UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();
				userInfo._id= resultBean.profile.id;
				userInfo.name = resultBean.profile.name;
				userInfo.mail = resultBean.profile.email;
				userInfo.schoolName = resultBean.profile.school_name;
				userInfo.address = resultBean.profile.school_addr;
				userInfo.city = resultBean.profile.school_city;
				userInfo.country = resultBean.profile.school_country;
				userInfo.postcode = resultBean.profile.school_postalcode;
				userInfo.syllabus_id = resultBean.profile.syllabus;
				userInfo.grade_id = resultBean.profile.grade;
				userInfo.section = resultBean.profile.section;
				userInfo.photo_url = resultBean.profile.photo;

				userInfo.isValid = true;

				mPrefs.setUserId(userInfo._id);

				setCurrentSyllabus(userInfo._id);
				setCurrentGrade(userInfo._id);				

				onUpdateUserInfo();
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
				mPrefs.setToken("");
				mPrefs.setLogIn(false);
				mPrefs.setUserId("");
				goBackToGradeActivity();
			}
		}		
	}

	private void GetPhotoTask(final String strUrl){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				Bitmap bm;
				try {					
					URL url = new URL(strUrl);
					URLConnection conn = url.openConnection();				
					conn.connect();

					BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
					bm = BitmapFactory.decodeStream(bis);
					bis.close();
				}
				catch (Exception e)
				{
					System.out.println("Exce="+e);
					return null;
				}
				return bm;
			}

			@Override
			public void onTaskResult(int taskId, Object result) {

				if (result != null)
				{
					Bitmap photo = (Bitmap)result;

					GlobalValue.getInstance().getCurrentUser().photo = photo;
					onUpdateUserInfo();
				}				
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

	private void getConceptLinkList(int start)	{
		Syllabus syllabus = GlobalValue.getInstance().getCurrentSyllabus();
		Grade grade = GlobalValue.getInstance().getCurrentGrade();		

		GetLinkLstParam param = new GetLinkLstParam();
		param.kind = "concept";
		param.syllabus = syllabus.id;		
		param.grade = grade.id;
		param.start = start;
		param.count = Constants.COUNT_UNIT;
		param.checksum = mDbMgr.getConceptLinkCheckSum(syllabus.id, grade.id);

		WebApiInstance.getInstance().executeAPI(Type.GET_LINKS, param, this);
	}

	private void getLinkListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetLinkLstResult resultBean = (GetLinkLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (linkCnt == 0) {
					Syllabus syllabus = GlobalValue.getInstance().getCurrentSyllabus();
					Grade grade = GlobalValue.getInstance().getCurrentGrade();
					mDbMgr.delConceptLinksByParam(syllabus.id, grade.id);
				}

				for (LinkResult item : resultBean.links)
				{
					mDbMgr.addLinkItem(item);
					linkCnt++;
				}				

				if (resultBean.eof == false) {
					getConceptLinkList (linkCnt);
					return;
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		linkCnt = 0;
		setLoadPercent(Type.GET_LINKS);
	}

	private void getChapterList(int start){

		Syllabus syllabus = GlobalValue.getInstance().getCurrentSyllabus();
		Grade grade = GlobalValue.getInstance().getCurrentGrade();

		GetChapterLstParam param = new GetChapterLstParam();
		param.checksum = mDbMgr.getChapterCheckSum(syllabus.id, grade.id);
		param.syllabus = syllabus.id;
		param.grade = grade.id;
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_CHAPTERS, param, this);
	}

	private void getChapterListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetChapterLstResult resultBean = (GetChapterLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (chapterCnt == 0) {
					mDbMgr.delChapterByParam(resultBean.syllabus, resultBean.grade);
				}

				for (ChapterResult item : resultBean.chapters)
				{
					mDbMgr.addChapterItem(item);
					chapterCnt++;
				}

				if (resultBean.eof == false) {
					getChapterList (chapterCnt);
					return;
				}

			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		chapterCnt = 0;
		setLoadPercent(Type.GET_CHAPTERS);
	}

	private void getConceptList(int start){

		Syllabus syllabus = GlobalValue.getInstance().getCurrentSyllabus();
		Grade grade = GlobalValue.getInstance().getCurrentGrade();

		GetConceptLstParam param = new GetConceptLstParam();
		param.checksum = mDbMgr.getConceptCheckSum(grade.id, null);
		param.syllabus = syllabus.id;
		param.grade = grade.id;
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_CONCEPTS, param, this);
	}

	private void getConceptListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetConceptLstResult resultBean = (GetConceptLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (conceptCnt == 0) {
					Syllabus syllabus = GlobalValue.getInstance().getCurrentSyllabus();
					Grade grade = GlobalValue.getInstance().getCurrentGrade();
					mDbMgr.delConceptByParam(syllabus.id, grade.id);
				}

				for (ConceptResult item : resultBean.concepts)
				{
					mDbMgr.addConceptItem(item);
					conceptCnt++;
				}

				if (resultBean.eof == false) {
					getConceptList (conceptCnt);
					return;
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		conceptCnt = 0;
		setLoadPercent(Type.GET_CONCEPTS);
	}

	@Override
	public void onPreProcessing(Type type, Object parameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResultProcessing(Type type, Object parameter, Object result) {
		// TODO Auto-generated method stub
		switch (type)
		{
		case GET_PROFILE:
			onGetProfileResult(result);
			break;

		case GET_LINKS:
			getLinkListResult(result);			
			break;
		case GET_CHAPTERS:
			getChapterListResult(result);			
			break;
		case GET_CONCEPTS:
			getConceptListResult(result);			
			break;
		default:
			break;
		}
	}


	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost.  It relies on a
	 * trick.  Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show.  This is not sufficient for switching
	 * between pages.  So instead we make the content part of the tab host
	 * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
	 * view to show as the tab content.  It listens to changes in tabs, and takes
	 * care of switch to the correct paged in the ViewPager whenever the selected
	 * tab changes.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter
	implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		private final HorizontalScrollView mScrollView;
		
		Fragment profileFrag, lessonFrag, classFrag;

		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager, HorizontalScrollView scrollview) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mScrollView = scrollview;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {

			TabInfo info = mTabs.get(position);
			State state = State.values()[position];
			
			Fragment newFragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
			
			switch(state)
			{
			case PROFILE:
				profileFrag = newFragment;
				break;
			case LESSONS:
				lessonFrag = newFragment;
				break;
			case CLASS:
				classFrag = newFragment;
				break;
			default:
				break;
			}
			
			return newFragment;
		}

		@Override
		public void onTabChanged(String tabId) {
			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
			
			if (getStateFromIndex(position) == State.CLASS) {
				ClassFragment fragment = (ClassFragment) classFrag;
				if (fragment != null)
					fragment.resetView();
			} else if (getStateFromIndex(position) == State.PROFILE) {
				ProfileFragment fragment = (ProfileFragment) profileFrag;
				if (fragment != null)
					fragment.resetView();
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			// Unfortunately when TabHost changes the current tab, it kindly
			// also takes care of putting focus on it when not in touch mode.
			// The jerk.
			// This hack tries to prevent this from pulling focus out of our
			// ViewPager.
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);            

			int width = widget.getWidth();
			int count = widget.getTabCount();

			int unit = width / count + 10;
			mScrollView.smoothScrollTo(unit * (position-1), 0);
			
			State newState = ((HomeActivity)mContext).getStateFromIndex(position);
			((HomeActivity)mContext).mState = newState;				
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
	}

}
