package com.belwadi.sciencefun.ui;

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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.adapter.MenuAdapter;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Grade;
import com.belwadi.sciencefun.entity.MainMenu;
import com.belwadi.sciencefun.entity.Syllabus;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Beans.ChapterResult;
import com.belwadi.sciencefun.entity.Beans.ConceptResult;
import com.belwadi.sciencefun.entity.Beans.GetChapterLstParam;
import com.belwadi.sciencefun.entity.Beans.GetChapterLstResult;
import com.belwadi.sciencefun.entity.Beans.GetConceptLstParam;
import com.belwadi.sciencefun.entity.Beans.GetConceptLstResult;
import com.belwadi.sciencefun.entity.Beans.GetLinkLstParam;
import com.belwadi.sciencefun.entity.Beans.GetLinkLstResult;
import com.belwadi.sciencefun.entity.Beans.GetNoteLstParam;
import com.belwadi.sciencefun.entity.Beans.GetNoteLstResult;
import com.belwadi.sciencefun.entity.Beans.GetProfileResult;
import com.belwadi.sciencefun.entity.Beans.GetRefLstParam;
import com.belwadi.sciencefun.entity.Beans.GetRefLstResult;
import com.belwadi.sciencefun.entity.Beans.GetVideoLstParam;
import com.belwadi.sciencefun.entity.Beans.GetVideoLstResult;
import com.belwadi.sciencefun.entity.Beans.LinkResult;
import com.belwadi.sciencefun.entity.Beans.NoteResult;
import com.belwadi.sciencefun.entity.Beans.ReferenceResult;
import com.belwadi.sciencefun.entity.Beans.VideoResult;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.CustomViewPager;
import com.belwadi.sciencefun.view.MainControllerGestureEventListener;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.BaseTask;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.Server;
import com.belwadi.sciencefun.webservice.WebApiCallback;
import com.belwadi.sciencefun.webservice.WebApiInstance;
import com.belwadi.sciencefun.webservice.BaseTask.TaskListener;
import com.belwadi.sciencefun.webservice.WebApiInstance.Type;
import com.google.android.gcm.GCMRegistrar;

import static com.belwadi.sciencefun.CommonUtilities.*;

public class MainActivity extends FragmentActivity implements OnClickListener, WebApiCallback{

	private AppPreferences mPrefs;
	private DatabaseManager mDbMgr;

	FrameLayout mFrameGesture;

	// title Bar
	Button mBtnMenu;
	TextView mTxtGrade, mTxtSyllabus, mTxtUserName;
	ImageView mImgUser;

	PopupMenu mPopMenu;

	CustomViewPager mViewPager;
	MainFragmentAdapter mPageAdapter;

	// Menu layout
	LinearLayout mLayMenu;	
	private ListView mListviewMenu;
	private ArrayList<MainMenu> mMenuArray = null;
	private MenuAdapter mAdapterMenu;

	private State mState;
	private boolean isShowMenu;

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
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		isShowMenu = false;

		mPrefs = new AppPreferences(this);
		mDbMgr = new DatabaseManager(this);

		linkCnt = chapterCnt = conceptCnt = videoCnt = noteCnt = refCnt = 0;

		int menuIndex = getIntent().getIntExtra("menu", 1);

		State newState;
		if (menuIndex < State.values().length)
			newState = State.values()[menuIndex];
		else
			newState = State.LESSONS;

		mMenuArray = new ArrayList<MainMenu>();
		mMenuArray.add(new MainMenu(getString(R.string.menu_profile)));
		mMenuArray.add(new MainMenu(getString(R.string.menu_lesson)));
		mMenuArray.add(new MainMenu(getString(R.string.menu_class)));
		//		mMenuArray.add(new MainMenu(getString(R.string.menu_updates)));
		if (mPrefs.isLogIn())
			mMenuArray.add(new MainMenu(getString(R.string.menu_signout)));
		else
			mMenuArray.add(new MainMenu(getString(R.string.menu_signin)));		
		mMenuArray.add(new MainMenu(getString(R.string.menu_about)));

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

		initPushNotification();

		setState(newState);
		setCaptionText();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		unregisterReceiver (mHandleMessageReceiver);
		GCMRegistrar.onDestroy(getApplicationContext());
		super.onDestroy();
	}

	private void initView()
	{
		mFrameGesture = (FrameLayout) findViewById(R.id.frame_gesture);

		mTxtGrade = (TextView) findViewById(R.id.txtGrade);
		mTxtSyllabus = (TextView) findViewById(R.id.txtSyllabus);

		mTxtUserName = (TextView) findViewById(R.id.txtUserName);

		mImgUser = (ImageView) findViewById(R.id.imgUserIcon);
		mImgUser.setOnClickListener(this);

		mPopMenu = new PopupMenu(this, mImgUser);
		mPopMenu.getMenuInflater().inflate(R.menu.popup, mPopMenu.getMenu());
		mPopMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				Toast.makeText(MainActivity.this, "Clicked popup menu item " + item.getTitle(),
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});
//		addOptionsMenuHackerInflaterFactory();

		mBtnMenu = (Button) findViewById(R.id.btnMenu);
		mBtnMenu.setOnClickListener(this);

		mLayMenu = (LinearLayout) findViewById(R.id.layMenu);
		mLayMenu.setVisibility(View.GONE);

		mAdapterMenu = new MenuAdapter(MainActivity.this, R.layout.item_menu, mMenuArray);
		mListviewMenu = (ListView) findViewById(R.id.lstviewMenu);
		mListviewMenu.setAdapter(mAdapterMenu);
		mListviewMenu.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				onClickMenu(position);
			}
		});

		mViewPager = (CustomViewPager) findViewById(R.id.vpPager);
		if (mPrefs.isLogIn())
			mViewPager.setPagingEnabled(true);
		else
			mViewPager.setPagingEnabled(false);
		mPageAdapter = new MainFragmentAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPageAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageScrollStateChanged(int state) {}
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

			public void onPageSelected(int position) {
				// Check if this is the page you want.

				State newState = getStateFromIndex(position);
				mState = newState;				
				setTitleText(mState);
				if (mState == State.PROFILE) {
					ProfileFragment profileFrag = (ProfileFragment)mPageAdapter.profileFrag;
					profileFrag.updateProfile(GlobalValue.getInstance().getCurrentUser());
				}
				//				else if (mState == State.UPDATES) {
				//					UpdatesFragment updateFrag = (UpdatesFragment)mPageAdapter.updateFrag;
				//					updateFrag.updateCnt = 0;
				//					updateFrag.getUpdateList(0);
				//				}					
			}
		});
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

	private State getStateFromIndex(int index)
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		int id = v.getId();

		switch(id)
		{
		case R.id.btnMenu:
			if (!isShowMenu)
				openMenu();
			else
				closeMenu();
			break;
		case R.id.imgUserIcon:
			mPopMenu.show();
			//			if (mPrefs.isLogIn())
			//				setState(State.PROFILE);
			//			else
			//				setState(State.SIGNINOUT);
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
			showCloseDialog();			
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
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void onClickMenu(int position)
	{
		State state;

		state = State.values()[position];

		if (state == State.PROFILE || state == State.CLASS /*|| state == State.UPDATES*/) {
			if (!mPrefs.isLogIn()){
				CustomToast.makeCustomToastShort(this, "Please sign in.");
				return;
			}
		}

		setState(state);
	}

	public void setState(State newState)	
	{
		State oldState = mState;	

		if (oldState == newState) {
			if(isShowMenu)
				closeMenu();
			return;
		}

		mState = newState;

		setTitleText(mState);
		mViewPager.setCurrentItem(getIndexFromState(mState));

		if(isShowMenu)
			closeMenu();
	}

	private void setTitleText(State newState)
	{
		String strTitle = "";

		switch(newState)
		{
		case PROFILE:
			strTitle = "Profile";
			break;
		case LESSONS:
			strTitle = "Lessons";
			break;
		case CLASS:
			strTitle = "My Class";
			break;
			//		case UPDATES:
			//			strTitle = "Updates";
			//			break;
		case SIGNINOUT:
			if (mPrefs.isLogIn())
				strTitle = getString(R.string.menu_signout);
			else
				strTitle = getString(R.string.menu_signin);
			break;
		case ABOUT:
			strTitle = "About";
			break;
		default:
			break;
		}
	}

	public void openMenu() {
		mLayMenu.setVisibility(View.VISIBLE);
		Animation moveRight = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.leftswipe_to_right);
		moveRight.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mLayMenu.clearAnimation();
			}
		});
		mLayMenu.startAnimation(moveRight);
		isShowMenu = true;
	}

	public void closeMenu() {

		Animation moveLeft = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.leftswipe_to_left);
		moveLeft.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mLayMenu.clearAnimation();
				mLayMenu.setVisibility(View.GONE);
			}
		});
		mLayMenu.startAnimation(moveLeft);

		isShowMenu = false;
	}

	public void onUpdateUserInfo()
	{
		UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();

		if (userInfo.name != null && !userInfo.name.isEmpty())
			mTxtUserName.setText(userInfo.name);

		if (userInfo.photo != null)
			mImgUser.setImageBitmap(userInfo.photo);
		else
			mImgUser.setImageResource(R.drawable.empty_pic);

		setCaptionText();
	}

	public void AfterSignIn()
	{
		String token = mPrefs.getToken();
		getProfileTask (token);

		setState(State.LESSONS);

		mViewPager.setPagingEnabled(true);

		MainMenu signMenu = mMenuArray.get(getIndexFromState(State.SIGNINOUT));
		signMenu.name = getString(R.string.menu_signout);
		mAdapterMenu.notifyDataSetChanged();
		setTitleText(mState);

		updateContents();
	}

	public void AfterSignOut()
	{
		mViewPager.setPagingEnabled(false);

		UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();

		userInfo.isValid = false;

		mTxtUserName.setText("Guest");
		mImgUser.setImageResource(R.drawable.guest);

		MainMenu signMenu = mMenuArray.get(getIndexFromState(State.SIGNINOUT));
		signMenu.name = getString(R.string.menu_signin);
		mAdapterMenu.notifyDataSetChanged();

		setTitleText(mState);
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
				mWaitDlg = new WaitDialog(MainActivity.this);
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

	private void goBackToGradeActivity()
	{
		Intent intent = new Intent();
		intent.setClass(this, GradeActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
		finish();
	}

	public void updateContents()
	{
		getConceptLinkList(linkCnt);
		getChapterList(chapterCnt);
		getConceptList(conceptCnt);
	}

	private void setLoadPercent(Type type)
	{
		int hitCount =  0;

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


		if (hitCount == 3 && mState == State.LESSONS) {
			LessonFragment lessonFrag = (LessonFragment)mPageAdapter.lessonFrag;
			lessonFrag.updateChapter();
		}
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

	private void getVideoList(int start) {

		Grade grade = GlobalValue.getInstance().getCurrentGrade();

		GetVideoLstParam param = new GetVideoLstParam();
		param.checksum = mDbMgr.getVideoCheckSumByGrade(grade.id);
		param.grade = grade.id;
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_VIDEOS, param, this);
	}

	private void getVideoListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetVideoLstResult resultBean = (GetVideoLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (videoCnt == 0) {
					Grade grade = GlobalValue.getInstance().getCurrentGrade();
					mDbMgr.delVideoByGrade(grade.id);
				}

				for (VideoResult item : resultBean.videos)
				{
					mDbMgr.addVideoItem(item);
					videoCnt++;
				}

				if (resultBean.eof == false) {
					getVideoList (videoCnt);
					return;
				}

			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		videoCnt = 0;
	}

	private void getReferenceList(int start) {

		Grade grade = GlobalValue.getInstance().getCurrentGrade();

		GetRefLstParam param = new GetRefLstParam();
		param.checksum = mDbMgr.getRefCheckSumByGrade(grade.id);
		param.grade = grade.id;
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_REFS, param, this);
	}

	private void getReferenceListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetRefLstResult resultBean = (GetRefLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (refCnt == 0) {
					Grade grade = GlobalValue.getInstance().getCurrentGrade();
					mDbMgr.delReferenceByGrade(grade.id);
				}

				for (ReferenceResult item : resultBean.references)
				{
					mDbMgr.addReferenceItem(item);
					refCnt++;
				}

				if (resultBean.eof == false) {
					getReferenceList (refCnt);
					return;
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		refCnt = 0;
	}

	private void getNoteList(int start) {

		Grade grade = GlobalValue.getInstance().getCurrentGrade();

		GetNoteLstParam param = new GetNoteLstParam();
		param.checksum = mDbMgr.getNoteCheckSumByGrade(grade.id);
		param.grade = grade.id;
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_NOTES, param, this);
	}

	private void getNoteListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetNoteLstResult resultBean = (GetNoteLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (noteCnt == 0) {
					Grade grade = GlobalValue.getInstance().getCurrentGrade();
					mDbMgr.delNoteByGrade(grade.id);
				}

				for (NoteResult item : resultBean.notes)
				{
					mDbMgr.addNoteItem(item);
					noteCnt++;
				}

				if (resultBean.eof == false) {
					getNoteList (noteCnt);
					return;
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		noteCnt = 0;
	}

	public class MainFragmentAdapter extends FragmentStatePagerAdapter  {

		Fragment profileFrag, lessonFrag, classFrag, updateFrag, termsFrag, signFrag, aboutFrag;

		public MainFragmentAdapter (FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return State.values().length;
		}

		@Override
		public Fragment getItem(int position) {
			// TODO Auto-generated method stub
			Fragment newFragment = null;

			State state = State.values()[position];

			switch(state)
			{
			case PROFILE:
				newFragment = new ProfileFragment();
				profileFrag = newFragment;
				break;
			case LESSONS:
				newFragment = new LessonFragment();
				lessonFrag = newFragment;
				break;
			case CLASS:
				newFragment = new ClassFragment();
				classFrag = newFragment;
				break;
				//			case UPDATES:
				//				newFragment = new UpdatesFragment();
				//				updateFrag = newFragment;
				//				break;
			case SIGNINOUT:
//				newFragment = new SigninActivity();
//				signFrag = newFragment;
				break;
			case ABOUT:
				newFragment = new AboutFragment();
				aboutFrag = newFragment;
				break;
			default:
				break;
			}

			return newFragment;
		}

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
		case GET_VIDEOS:
			getVideoListResult(result);			
			break;
		case GET_REFS:
			getReferenceListResult(result);			
			break;
		case GET_NOTES:
			getNoteListResult(result);			
			break;
		default:
			break;
		}
	}
}
