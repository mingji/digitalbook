package com.belwadi.sciencefun.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

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
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.WebApiCallback;
import com.belwadi.sciencefun.webservice.WebApiInstance;
import com.belwadi.sciencefun.webservice.WebApiInstance.Type;

public class SplashActivity extends Activity implements WebApiCallback{

	int[] seconds = {3};
	final Handler handler = new Handler();
	Runnable runnable;

	private DatabaseManager mDbMgr;
	private AppPreferences mPrefs;
	WaitDialog mWaitDlg;

	int userCnt, gradelinkCnt, concentlinkCnt, chapterCnt, conceptCnt;

	int mProgHit = 0;

	int FLAG_GRADE_LINK	= 0x01;
	int FLAG_SYLLABUS	= 0x02;
	int FLAG_GRADE	 	= 0x04;
	int FLAG_CHAPTER	= 0x08;	
	int FLAG_USERS		= 0x10;
	int FLAG_PROFILE	= 0x20;
	int FLAG_CONCEPT	= 0x40;
	int FLAG_CONCEPT_LINK	= 0x80;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_splash);
		ImageLoader.getInstance().init(getApplicationContext());
		WebApiInstance.getInstance().init(getApplicationContext(), 8);

		mPrefs = new AppPreferences(this);
		mDbMgr = new DatabaseManager(this);

		userCnt = gradelinkCnt = concentlinkCnt = chapterCnt = conceptCnt = 0;

		runnable = new Runnable() {
			@Override
			public void run() {
				/* do what you need to do */
				seconds[0]--;
				if (seconds[0] > 0) {
					/* and here comes the "trick" */
					handler.postDelayed(this, 300);
				} else {
					updateDatabase();
				}

			}
		};

		handler.postDelayed(runnable, 100);
	}

	private void setLoadPercent(int flag)
	{
		int hitCount =  0;
		String msg;

		mProgHit |= flag;
		//
		//		switch (type)
		//		{
		//		case GET_PROFILE:
		//			mProgHit |= FLAG_PROFILE;
		//			break;
		//
		//		case GET_LINKS:
		//			mProgHit |= FLAG_LINK;
		//			break;
		//		case GET_SYLLABUSES:
		//			mProgHit |= FLAG_SYLLABUS;
		//			break;
		//		case GET_GREADES:
		//			mProgHit |= FLAG_GRADE;
		//			break;
		//		case GET_CHAPTERS:
		//			mProgHit |= FLAG_CHAPTER;			
		//			break;
		//		case GET_CONCEPTS:
		//			mProgHit |= FLAG_CONCEPTS;			
		//			break;
		//
		//		case GET_USERS:
		//			mProgHit |= FLAG_USERS;
		//			break;
		//		default:
		//			break;
		//		}

		if ((mProgHit & FLAG_PROFILE) == FLAG_PROFILE)
			hitCount ++;
		if ((mProgHit & FLAG_GRADE_LINK) == FLAG_GRADE_LINK)
			hitCount ++;
		if ((mProgHit & FLAG_CONCEPT_LINK) == FLAG_CONCEPT_LINK)
			hitCount ++;
		if ((mProgHit & FLAG_SYLLABUS) == FLAG_SYLLABUS)
			hitCount ++;
		if ((mProgHit & FLAG_GRADE) == FLAG_GRADE)
			hitCount ++;		
		if ((mProgHit & FLAG_CHAPTER) == FLAG_CHAPTER)
			hitCount ++;		
		if ((mProgHit & FLAG_CONCEPT) == FLAG_CONCEPT)
			hitCount ++;
		if ((mProgHit & FLAG_USERS) == FLAG_USERS)
			hitCount ++;


		if (mPrefs.isLogIn()) {
			msg = "loading " + hitCount * 100 / 8 + "%";
			setWaitingMessage (msg);

			if (hitCount == 8)
				startNextActivity();
		} else {
			msg = "loading " + hitCount * 100 / 4 + "%";
			setWaitingMessage (msg);

			if (hitCount == 4)
				startNextActivity();
		}		
	}

	private void setWaitingMessage (String msg)
	{
		if (mWaitDlg == null)
			return;

		mWaitDlg.setMessage(msg);
	}

	private void updateDatabase()
	{
		mWaitDlg = new WaitDialog(SplashActivity.this);
		mWaitDlg.setMessage("loading 0%");
		mWaitDlg.show();

		getGradeLinkList(gradelinkCnt);
		getSyllabusList();
		getGradeList();
		getUserList(userCnt);

		if (mPrefs.isLogIn()) {
			Log.d("Debug", "TOKEN : " + mPrefs.getToken());
			getProfileTask(mPrefs.getToken());
		}
	}

	// should load the contents by grade after get user's profile
	private void loadContents()
	{
		getConceptLinkList(concentlinkCnt);
		getChapterList(chapterCnt);
		getConceptList(conceptCnt);
	}

	private void startNextActivity() {

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}

		// Run next activity
		if (mPrefs.isLogIn()) {
			startHomeActivity();
		} else {
			startGradeActivity();
		}
	}

	private void startHomeActivity() {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(this, HomeActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
		finish();
	}

	private void startGradeActivity() {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(this, GradeActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
		finish();
	}

	private void setCurrentSyllabus(String userId)	{
		UserInfo user = mDbMgr.getUser(userId);
		Syllabus syllabus = mDbMgr.getSyllabus(user.syllabus_id);
		GlobalValue.getInstance().setCurrentSyllabus(syllabus);
	}

	private void setCurrentGrade(String userId)	{
		UserInfo user = mDbMgr.getUser(userId);
		Grade grade = mDbMgr.getGrade(user.grade_id);				
		GlobalValue.getInstance().setCurrentGrade(grade);
	}

	private void getGradeLinkList(int start)	{
		GetLinkLstParam param = new GetLinkLstParam();
		param.kind = "grade";
		param.start = start;
		param.count = Constants.COUNT_UNIT;
		param.checksum = mDbMgr.getGradeLinkCheckSum();

		WebApiInstance.getInstance().executeAPI(Type.GET_LINKS, param, this);
	}

	private void getGradeLinkListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetLinkLstResult resultBean = (GetLinkLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (gradelinkCnt == 0) {
					mDbMgr.delGradeLinks();
				}

				for (LinkResult item : resultBean.links)
				{
					mDbMgr.addLinkItem(item);
					gradelinkCnt++;
				}				

				if (resultBean.eof == false) {
					getGradeLinkList (gradelinkCnt);
					return;
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		setLoadPercent(FLAG_GRADE_LINK);
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

	private void getConceptLinkListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetLinkLstResult resultBean = (GetLinkLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (concentlinkCnt == 0) {
					Syllabus syllabus = GlobalValue.getInstance().getCurrentSyllabus();
					Grade grade = GlobalValue.getInstance().getCurrentGrade();
					mDbMgr.delConceptLinksByParam(syllabus.id, grade.id);
				}

				for (LinkResult item : resultBean.links)
				{
					mDbMgr.addLinkItem(item);
					concentlinkCnt++;
				}				

				if (resultBean.eof == false) {
					getConceptLinkList (concentlinkCnt);
					return;
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		concentlinkCnt = 0;
		setLoadPercent(FLAG_CONCEPT_LINK);
	}

	private void getSyllabusList()	{
		GetSyllabusLstParam param = new GetSyllabusLstParam();
		param.checksum = mDbMgr.getSyllabusCheckSum();

		WebApiInstance.getInstance().executeAPI(Type.GET_SYLLABUSES, param, this);
	}

	private void getSyllabusListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetAllSyllabusLstResult resultBean = (GetAllSyllabusLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				mDbMgr.resetSyllabusTable();

				for (SyllabusResult item : resultBean.syllabuses)
				{
					mDbMgr.addSyllabusItem(item);
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		setLoadPercent(FLAG_SYLLABUS);
	}

	private void getGradeList()	{
		GetGradeLstParam param = new GetGradeLstParam();
		param.checksum = mDbMgr.getGradeCheckSum();

		WebApiInstance.getInstance().executeAPI(Type.GET_GRADES, param, this);
	}

	private void getGradeListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetAllGradeLstResult resultBean = (GetAllGradeLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				mDbMgr.resetGradeTable();

				for (GradeResult item : resultBean.grades)
				{
					mDbMgr.addGradeItem(item);
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		setLoadPercent(FLAG_GRADE);
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

	private void getUserList(int start) {

		GetUserLstParam param = new GetUserLstParam();
		param.checksum = mDbMgr.getUserCheckSum();
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_USERS, param, this);
	}

	private void getUserListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetAllUserLstResult resultBean = (GetAllUserLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (userCnt == 0)
					mDbMgr.resetUserTable();

				for (UserResult item : resultBean.users)
				{
					mDbMgr.addUserItem(item);
					userCnt++;
				}

				if (resultBean.eof == false) {
					getUserList (userCnt);
					return;
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		setLoadPercent(FLAG_USERS);
	}

	private void getProfileTask(final String token) {

		GetProfileParam param = new GetProfileParam();
		param.token = mPrefs.getToken();

		WebApiInstance.getInstance().executeAPI(Type.GET_PROFILE, param, this);
	}


	private void onGetProfileResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
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

				mPrefs.setUserId(userInfo._id);

			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		setCurrentUserInfo();
		loadContents();
		setLoadPercent(FLAG_PROFILE);
	}

	private void setCurrentUserInfo()
	{
		UserInfo savedUser = mDbMgr.getUser(mPrefs.getUserId());
		if (savedUser == null) {
			startGradeActivity();
			return;
		}

		UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();
		userInfo._id= savedUser._id;
		userInfo.name = savedUser.name;
		userInfo.mail = savedUser.mail;
		userInfo.schoolName = savedUser.schoolName;
		userInfo.address = savedUser.address;
		userInfo.city = savedUser.city;
		userInfo.country = savedUser.country;
		userInfo.postcode = savedUser.postcode;
		userInfo.syllabus_id = savedUser.syllabus_id;
		userInfo.syllabus = savedUser.syllabus;
		userInfo.grade_id = savedUser.grade_id;
		userInfo.grade = savedUser.grade;
		userInfo.section = savedUser.section;
		userInfo.photo_url = savedUser.photo_url;

		userInfo.isValid = true;

		setCurrentSyllabus(userInfo._id);
		setCurrentGrade(userInfo._id);
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
			{
				GetLinkLstParam param = (GetLinkLstParam)parameter;
				
				if (param.kind.equalsIgnoreCase("grade"))
					getGradeLinkListResult(result);
				if (param.kind.equalsIgnoreCase("concept"))
					getConceptLinkListResult(result);
			}
		break;
		case GET_SYLLABUSES:
			getSyllabusListResult(result);
			break;
		case GET_GRADES:
			getGradeListResult(result);
			break;
		case GET_CHAPTERS:
			getChapterListResult(result);			
			break;
		case GET_CONCEPTS:
			getConceptListResult(result);			
			break;

		case GET_USERS:
			getUserListResult(result);
			break;
		default:
			break;
		}
	}
}
