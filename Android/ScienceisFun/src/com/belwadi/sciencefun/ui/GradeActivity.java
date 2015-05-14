package com.belwadi.sciencefun.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Grade;
import com.belwadi.sciencefun.entity.Link;
import com.belwadi.sciencefun.entity.Scientist;
import com.belwadi.sciencefun.entity.Syllabus;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.ui.HomeActivity.State;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.HyIconView;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.WebApiCallback;
import com.belwadi.sciencefun.webservice.WebApiInstance;
import com.belwadi.sciencefun.webservice.WebApiInstance.Type;

public class GradeActivity extends Activity implements OnClickListener, WebApiCallback{

	private ArrayList<Grade> mGradeArray = null;
	private GridView mGridGrade;
	private GradeAdapter mAdapterGrade;
	
	private TextView mTxtUser;
	private HyIconView mImgUser;
	
	PopupMenu mPopMenu;
	
	private Spinner mSpinSyllabus;
	private ArrayList<String> mArrayStrSyllabus;
	private ArrayAdapter<String> mAdapterSyllabus;
	private ArrayList<Syllabus> mArraySyllabus;

	private DatabaseManager mDbMgr;
	private AppPreferences mPrefs;
	
	private Scientist mScientist;

	WaitDialog mWaitDlg;

	int linkCnt, chapterCnt, conceptCnt;
	int mProgHit = 0;

	int FLAG_LINK		= 0x01;
	int FLAG_SYLLABUS	= 0x02;
	int FLAG_GRADE	 	= 0x04;
	int FLAG_CHAPTER	= 0x08;	
	int FLAG_USERS		= 0x10;
	int FLAG_PROFILE	= 0x20;
	int FLAG_CONCEPTS	= 0x40;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_grade);

		mDbMgr = new DatabaseManager(this);
		mPrefs = new AppPreferences(this);
		
		mScientist = GlobalValue.getInstance().getScientist(this);

		linkCnt = chapterCnt = conceptCnt = 0;

		initView();
		initViewPopupMenu();
		
		updateSyllabus();
	}

	private void initView()
	{
		mTxtUser = (TextView)findViewById(R.id.txtUserName);
		if (mScientist.name != null && !mScientist.name.isEmpty())
			mTxtUser.setText(mScientist.name);
		
		mImgUser = (HyIconView) findViewById(R.id.imgUserIcon);
		mImgUser.imageView.setImageResource(R.drawable.empty_pic);
		if (mScientist.imageUrl != null && !mScientist.imageUrl.isEmpty())
			ImageLoader.getInstance().displayImage(mScientist.imageUrl, mImgUser, 0, 0);
		mImgUser.setOnClickListener(this);

		mGridGrade = (GridView)findViewById(R.id.gridGrade);
		mGradeArray = new ArrayList<Grade>();
		mAdapterGrade = new GradeAdapter(GradeActivity.this, R.layout.item_grade, mGradeArray);
		mGridGrade.setAdapter(mAdapterGrade);

		mGridGrade.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				Grade item = mGradeArray.get(position);				
				GlobalValue.getInstance().setCurrentGrade(item);
				
				int syllabus_index = mSpinSyllabus.getSelectedItemPosition();
				Syllabus syllabus = mArraySyllabus.get(syllabus_index);
				GlobalValue.getInstance().setCurrentSyllabus(syllabus);

				UserInfo userInfo = new UserInfo();
				userInfo._id = "admin";
				userInfo.isValid = true;
				GlobalValue.getInstance().setCurrentUser(userInfo);
				
				loadContents();
			}
		});
		
		mSpinSyllabus = (Spinner) findViewById(R.id.spinSyllabus);
		mSpinSyllabus.setPrompt("Select Syllabus");
		
		mArrayStrSyllabus = new ArrayList<String>();
		mArraySyllabus = new ArrayList<Syllabus>();
		mAdapterSyllabus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mArrayStrSyllabus);
		mAdapterSyllabus.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mSpinSyllabus.setAdapter(mAdapterSyllabus);
		mSpinSyllabus.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
            	Syllabus syllabus = mArraySyllabus.get(pos);
            	updateGrade(syllabus.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
		
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

	private void updateSyllabus()
	{
		mArraySyllabus = mDbMgr.getSyllabus();
		mArrayStrSyllabus.clear();
		for (Syllabus syllabus : mArraySyllabus)
		{
			mArrayStrSyllabus.add(syllabus.title);
		}
		
		mAdapterSyllabus.notifyDataSetChanged();
	}
	
	private void updateGrade(String syllabus_id)
	{
		mGradeArray.clear();
		
		ArrayList<Link> gradeLinks = mDbMgr.getGradeLinks(syllabus_id);
		for (Link link : gradeLinks)
		{
			Grade grade = mDbMgr.getGrade(link.grade);
			
			if (grade != null)
				mGradeArray.add(grade);
		}
		
		mAdapterGrade.notifyDataSetChanged();
	}
	
	private void onClickPopupMenu(String title)
	{
		if (title.equalsIgnoreCase("Profile")) {
			openGuestProfile();			
		} else if (title.equalsIgnoreCase("Sign In")) {
			
			Syllabus syllabus = mArraySyllabus.get(0);
			GlobalValue.getInstance().setCurrentSyllabus(syllabus);
			
			Grade item = mGradeArray.get(0);
			GlobalValue.getInstance().setCurrentGrade(item);

			UserInfo userInfo = new UserInfo();
			userInfo._id = "admin";
			userInfo.isValid = true;
			GlobalValue.getInstance().setCurrentUser(userInfo);
			
			startSigninActivity();			
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		int id = v.getId();

		switch(id)
		{
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
	
	private void openGuestProfile()
	{
		if (mScientist == null || mScientist.name == null)
			return;
		String searchName = mScientist.name.replace(" ", "%20");
		String url = "http://www.google.com/search?q=" + searchName;
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}

	private void startHomeActivity(int menuIndex) {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(this, HomeActivity.class);
		intent.putExtra("menu", menuIndex);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
		finish();
	}
	
	private void startSigninActivity() {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(this, SigninActivity.class);
		intent.putExtra("from", "grade_activity");
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
		finish();
	}

	private void loadContents()
	{
		mWaitDlg = new WaitDialog(GradeActivity.this);
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
			startHomeActivity(1); // lesson fragment
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

		setLoadPercent(Type.GET_CONCEPTS);
	}

	public static class GradeAdapter extends ArrayAdapter<Grade> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Grade> item = new ArrayList<Grade>();

		public GradeAdapter(Activity activity, int layoutId, ArrayList<Grade> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			GradeHolder holder = null;	

			Grade gradeItem = getItem(position);

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_grade, null);
				holder = new GradeHolder();
				holder.txtGradeName = (TextView) convertView.findViewById(R.id.txtGradeName);

				convertView.setTag(holder);
			} 
			else
				holder = (GradeHolder) convertView.getTag();

			holder.txtGradeName.setText(gradeItem.grade_name + "th\nGrade");
			if (gradeItem.enable == 0)
				convertView.setBackgroundResource(R.color.grade_gray);

			return convertView;
		}

		@Override
		public boolean areAllItemsEnabled()
		{
			return false;
		}

		@Override
		public boolean isEnabled(int position)
		{
			Grade gradeItem = getItem(position);

			if (gradeItem.enable == 0)
				return false;
			else
				return true;
		}

		public static class GradeHolder {
			TextView txtGradeName;
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
