package com.belwadi.sciencefun.ui;


import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Grade;
import com.belwadi.sciencefun.entity.Scientist;
import com.belwadi.sciencefun.entity.Syllabus;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Beans.ChapterResult;
import com.belwadi.sciencefun.entity.Beans.ConceptResult;
import com.belwadi.sciencefun.entity.Beans.GetAllUserLstResult;
import com.belwadi.sciencefun.entity.Beans.GetChapterLstParam;
import com.belwadi.sciencefun.entity.Beans.GetChapterLstResult;
import com.belwadi.sciencefun.entity.Beans.GetConceptLstParam;
import com.belwadi.sciencefun.entity.Beans.GetConceptLstResult;
import com.belwadi.sciencefun.entity.Beans.GetLinkLstParam;
import com.belwadi.sciencefun.entity.Beans.GetLinkLstResult;
import com.belwadi.sciencefun.entity.Beans.GetProfileResult;
import com.belwadi.sciencefun.entity.Beans.LinkResult;
import com.belwadi.sciencefun.entity.Beans.SignInResult;
import com.belwadi.sciencefun.entity.Beans.UserResult;
import com.belwadi.sciencefun.ui.HomeActivity.State;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.HyIconView;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.BaseTask;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.Server;
import com.belwadi.sciencefun.webservice.WebApiCallback;
import com.belwadi.sciencefun.webservice.WebApiInstance;
import com.belwadi.sciencefun.webservice.BaseTask.TaskListener;
import com.belwadi.sciencefun.webservice.WebApiInstance.Type;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SigninActivity extends Activity implements OnClickListener, WebApiCallback
{
	
	private final int SIGNUP_ACTIVITY = 0;

	Button mBtnSignUp, mBtnSignIn, mBtnCancel;
	TextView mTxtForgot;
	EditText mTxtUsername, mTxtPassword;
	
	TextView mTxtUserName;
	HyIconView mImgUser;
	
	PopupMenu mPopMenu;
	
	Scientist mScientist;

	WaitDialog mWaitDlg;

	RelativeLayout mLaySignIn, mLayBottom;
	
	private AppPreferences mPrefs;
	private DatabaseManager mDbMgr;
	
	int linkCnt, chapterCnt, conceptCnt;
	int mProgHit = 0;
	
	int FLAG_LINK		= 0x01;
	int FLAG_CHAPTER	= 0x02;
	int FLAG_CONCEPT 	= 0x04;
	
	String mPrevActivity;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_signin);

		mPrefs = new AppPreferences(this);
		mDbMgr = new DatabaseManager(this);
		mScientist = GlobalValue.getInstance().getScientist(this);
		
		mPrevActivity = getIntent().getStringExtra("from");
		
		initView();
		initViewGuestUser();
	}

	private void initView()
	{
		mLaySignIn = (RelativeLayout) findViewById(R.id.laySignIn);
		mLayBottom = (RelativeLayout) findViewById(R.id.layBottom);

		mTxtUsername = (EditText) findViewById(R.id.txtUsername);
		mTxtPassword = (EditText) findViewById(R.id.txtPassword);

		mBtnSignIn = (Button) findViewById(R.id.btnSignIn);
		mBtnSignUp = (Button) findViewById(R.id.btnSignUp);
		mBtnCancel = (Button) findViewById(R.id.btnWithGuest);

		mTxtForgot = (TextView) findViewById(R.id.txtForgotPwd);

		mBtnSignIn.setOnClickListener(this);
		mBtnSignUp.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);

		mTxtForgot.setOnClickListener(this);
		
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
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode != Activity.RESULT_OK)
		{
			return;
		}

		if (requestCode == SIGNUP_ACTIVITY) {
			String strPage = data.getStringExtra("next_page");
			if (strPage != null && strPage.compareTo("grade") == 0) {
				finish();
			}
		}
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
		case R.id.btnSignUp:
			onClickSingUp();
			break;
		case R.id.btnWithGuest:
			goBackToGradeActivity();
			break;
		case R.id.txtForgotPwd:
			onClickForgotPwd();
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
			if (mPrevActivity != null && mPrevActivity.equalsIgnoreCase("grade_activity"))
				goBackToGradeActivity();
			else {
				finish();
				overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
			}
			bool = true;
		}
		return bool;
	}
	
	private void onClickPopupMenu(String title)
	{
		if (title.equalsIgnoreCase("Profile")) {
			if (mPrefs.isLogIn())
				;
			else
				openGuestProfile();
		} else if (title.equalsIgnoreCase("Sign In")) {
			;			
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
	
	private void loadContents()
	{
		mWaitDlg = new WaitDialog(SigninActivity.this);
		mWaitDlg.setMessage("loading 0%");
		mWaitDlg.show();

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

	private void setLoadPercent(int flag)
	{
		int hitCount =  0;
		String msg;

		mProgHit |= flag;

		if ((mProgHit & FLAG_LINK) == FLAG_LINK)
			hitCount ++;
		if ((mProgHit & FLAG_CHAPTER) == FLAG_CHAPTER)
			hitCount ++;
		if ((mProgHit & FLAG_CONCEPT) == FLAG_CONCEPT)
			hitCount ++;


		msg = "loading " + hitCount * 100 / 3 + "%";
		setWaitingMessage (msg);

		if (hitCount == 3) {

			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			startHomeActivity(1); // lesson fragment
		}
	}

	private void onClickSignIn()
	{
		if (TextUtils.isEmpty(mTxtUsername.getText().toString())) {
			CustomToast.makeCustomToastShort(this, "Please enter your email address.");
			return;
		}

		if (TextUtils.isEmpty(mTxtPassword.getText().toString())) {
			CustomToast.makeCustomToastShort(this, "Please enter the password.");
			return;
		}

		onSignInTask(mTxtUsername.getText().toString(), mTxtPassword.getText().toString(), mPrefs.getGcmRegId());
	}

	private void onClickSingUp()
	{
		Intent intent = new Intent();
		intent.setClass(this, SignupActivity.class);
		startActivityForResult(intent, SIGNUP_ACTIVITY);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
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

	private void onClickForgotPwd()
	{
		ResetPasswordDialog resetDiag =new ResetPasswordDialog(this, this);
		resetDiag.show();
	}

	private void AfterSignIn()
	{
		loadContents();
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
	
	private void startHomeActivity(int menuIndex) {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(this, HomeActivity.class);
		intent.putExtra("menu", menuIndex);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
		finish();
	}

	private void onSignInTask(final String username, final String passwd, final String gcmRegId){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SignIn(username, passwd, gcmRegId);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onSignInResult(result);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(SigninActivity.this);
				mWaitDlg.setMessage("SignIn...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}

	private void onSignInResult(Object obj) {
		
		mTxtPassword.setText("");
		
		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			SignInResult resultBean = (SignInResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				CustomToast.makeCustomToastShort(this, resultBean.message);
				
				mTxtUsername.setText("");
				
				mPrefs.setToken(resultBean.token);
				mPrefs.setLogIn(true);
				
				getUserList();
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
				mWaitDlg = new WaitDialog(SigninActivity.this);
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
				
				GlobalValue.getInstance().setCurrentUser(userInfo);
				
				mPrefs.setUserId(userInfo._id);

				setCurrentSyllabus(userInfo._id);
				setCurrentGrade(userInfo._id);
				
				AfterSignIn();
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
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
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				getUserListResult(result);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(SigninActivity.this);
				mWaitDlg.setMessage("Loading ...");
				mWaitDlg.show();
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
				for (UserResult item : resultBean.users)
				{
					mDbMgr.addUserItem(item);
				}
				
				getProfileTask(mPrefs.getToken());
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
				getProfileTask(mPrefs.getToken());
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
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

		setLoadPercent(FLAG_LINK);
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

		setLoadPercent(FLAG_CHAPTER);
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

		setLoadPercent(FLAG_CONCEPT);
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

}
