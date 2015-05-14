package com.belwadi.sciencefun.ui;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.*;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.view.AlertDialogManager;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.HyIconView;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.WebApiCallback;
import com.belwadi.sciencefun.webservice.WebApiInstance;
import com.belwadi.sciencefun.webservice.WebApiInstance.Type;

public class SearchVideoActivity extends Activity implements OnClickListener, WebApiCallback{

	//	public static final String DEVELOPER_KEY = "AIzaSyCs6Pvw0of1MLEDSOMjzJ3s9vpx4HqF7t4";

	DatabaseManager mDbMgr;
	AppPreferences mPrefs;
	UserInfo mUserInfo;

	AlertDialogManager mAlertDiag = new AlertDialogManager();

	String mConceptId;	
	Concept mConcept;

	TextView mTxtUserName;
	HyIconView mImgUserIcon;
	
	Scientist mScientist;

	PopupMenu mPopMenu;

	RelativeLayout mLayGuest, mLaySearch;

	TextView mTxtGuestMsg;

	EditText mTxtSearch;
	Button mBtnBack, mBtnSearchVideo;

	private ArrayList<Video> mArrayVideo = null;	

	private ArrayList<YoutubeSearchItem> mArraySearchVideos = null;
	private VideoAdapter mAdapterVideo;
	private ListView mListviewVideo;

	boolean isSearching = false;

	ArrayList<UserInfo> mArrayClassmates;

	int videoCnt;

	WaitDialog mWaitDlg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_search_video);

		mDbMgr = new DatabaseManager(this);
		mPrefs = new AppPreferences(this);
		
		mScientist = GlobalValue.getInstance().getScientist(this);

		mUserInfo = GlobalValue.getInstance().getCurrentUser();

		mConceptId = getIntent().getStringExtra("concept_id");
		if (mConceptId == null || mConceptId.isEmpty())
			finish();

		mConcept = mDbMgr.getConcept(mConceptId);
		mArrayClassmates = new ArrayList<UserInfo>();

		videoCnt = 0;

		initView();
		initViewPopupMenu();

		if (mPrefs.isLogIn()) {
			onUpdateUserInfo();
			getClassmateList();
			
			getAddedVideos();
			getYoutubeSearch();
		}
	}


	@Override
	public void onResume(){
		super.onResume();
		// put your code here...		
		if (!mPrefs.isLogIn())
			initViewGuestUser();
	}

	private void initView()
	{
		mTxtUserName = (TextView) findViewById(R.id.txtUserName);
		mImgUserIcon = (HyIconView) findViewById(R.id.imgUserIcon);
		mImgUserIcon.setOnClickListener(this);

		mTxtSearch = (EditText) findViewById(R.id.txtSearchVideo);
		mTxtSearch.setText(mConcept.title);

		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(this);

		mLayGuest = (RelativeLayout) findViewById(R.id.layGuestMsg);
		mLaySearch = (RelativeLayout) findViewById(R.id.laySearch);

		mTxtGuestMsg = (TextView) findViewById(R.id.txtGuestMsg);
		initGuestMsg();

		mBtnSearchVideo = (Button) findViewById(R.id.btnSearchVideo);
		mBtnSearchVideo.setOnClickListener(this);

		mArrayVideo = new ArrayList<Video>();
		mArraySearchVideos = new ArrayList<YoutubeSearchItem>(); 
		mAdapterVideo = new VideoAdapter(this, R.layout.item_search_video, mArraySearchVideos);

		mListviewVideo = (ListView) findViewById(R.id.lstVideo);		
		mListviewVideo.setAdapter(mAdapterVideo);
		mListviewVideo.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				YoutubeSearchItem videoItem = mArraySearchVideos.get(position);
				startContentViewActivity(videoItem.id.videoId, "video");
			}
		});	

		mPopMenu = new PopupMenu(this, mImgUserIcon);
		mPopMenu.getMenuInflater().inflate(R.menu.popup, mPopMenu.getMenu());
		mPopMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {

				onClickPopupMenu(item.getTitle().toString());

				return true;
			}
		});

		if (mPrefs.isLogIn()) {
			mLayGuest.setVisibility(View.GONE);
			mLaySearch.setVisibility(View.VISIBLE);			
		} else {
			mLayGuest.setVisibility(View.VISIBLE);
			mLaySearch.setVisibility(View.GONE);
		}
	}

	private void initViewPopupMenu()
	{
		MenuItem item = mPopMenu.getMenu().getItem(1);

		if (mPrefs.isLogIn())
			item.setTitle("Sign Out");
		else
			item.setTitle("Sign In");
	}
	
	private void initViewGuestUser()
	{
		if (mPrefs.isLogIn())
			return;
		
		if (mScientist.name != null && !mScientist.name.isEmpty())
			mTxtUserName.setText(mScientist.name);
		
		mImgUserIcon.imageView.setImageResource(R.drawable.empty_pic);
		if (mScientist.imageUrl != null && !mScientist.imageUrl.isEmpty())
			ImageLoader.getInstance().displayImage(mScientist.imageUrl, mImgUserIcon, 0, 0);
	}

	private void initGuestMsg()
	{
		String strNote = "You have not enrolled in any class. \nTo add a video, Sign In  or  Sign Up for Free.";
		SpannableString ss = new SpannableString(strNote);

		ClickableSpan signinSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				finishActivity("Sign In");
			}
		};

		ClickableSpan signupSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				finishActivity("Sign Up");
			}
		};

		int start1 = strNote.indexOf("Sign In");
		int start2 = strNote.indexOf("Sign Up");		
		ss.setSpan(signinSpan, start1, start1 + "Sign In".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ss.setSpan(signupSpan, start2, start2 + "Sign Up".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTxtGuestMsg.setText(ss);
		mTxtGuestMsg.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private void onUpdateUserInfo()
	{
		UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();

		if (userInfo.name != null && !userInfo.name.isEmpty())
			mTxtUserName.setText(userInfo.name);

		if (userInfo.photo != null)
			mImgUserIcon.imageView.setImageBitmap(userInfo.photo);
		else
			mImgUserIcon.imageView.setImageResource(R.drawable.empty_pic);
	}

	private void getClassmateList()
	{
		mArrayClassmates.clear();

		ArrayList<UserInfo> friendLst = mDbMgr.getFriends(mUserInfo);

		for (UserInfo friend : friendLst)
		{
			if (friend._id.equalsIgnoreCase(mUserInfo._id))
				continue;

			if (friend.schoolName.equalsIgnoreCase(mUserInfo.schoolName)
					&& friend.grade_id.equalsIgnoreCase(mUserInfo.grade_id)
					&& friend.section.equalsIgnoreCase(mUserInfo.section)) {

				mArrayClassmates.add(friend);
			}
		}
	}

	private void getAddedVideos()
	{
		mArrayVideo.clear();
		ArrayList<Video> tmp = mDbMgr.getVideoByConceptUser(mConceptId, mUserInfo._id, mArrayClassmates);
		for (Video item : tmp) {
			mArrayVideo.add(item);
		}
	}

	private String getVideoFromYoutubeId(String youtubeId)
	{
		String retId = null;
		for (Video item : mArrayVideo) {
			if (item.url.equalsIgnoreCase(youtubeId)) {
				retId = item.id;
				return retId;
			}
		}
		return null;
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

	private void startContentViewActivity(String contentId, String type)
	{		
		Intent intent = new Intent();
		intent.setClass(this, ContentViewActivity.class);
		intent.putExtra("type", type);
		intent.putExtra("content_id", contentId);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}

	private void onClickPopupMenu (String result)
	{
		if (result.equalsIgnoreCase("Profile")) {
			if (mPrefs.isLogIn())
				finishActivity("Profile");
			else
				openGuestProfile();
		} else if (result.equalsIgnoreCase("Sign In")) {
			finishActivity("Sign In");			
		} else if (result.equalsIgnoreCase("Sign Out")) {
			finishActivity("Sign Out");
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();

		switch (id)
		{
		case R.id.btnBack:
			finishActivity("");
			break;
		case R.id.btnSearchVideo:
			getYoutubeSearch();
			break;
		case R.id.imgUserIcon:
			mPopMenu.show();
			//			if (mPrefs.isLogIn())
			//				finishActivity("profile");
			//			else
			//				finishActivity("sign");
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
			finishActivity("");
			bool = true;
		}
		return bool;
	}

	private void finishActivity(String goPage)
	{
		Intent prevIntent = getIntent();

		prevIntent.putExtra("go_page", goPage);

		setResult(RESULT_OK, prevIntent);

		finish();
		overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
	}

	private void re_checkSearchVideos() {

		for (YoutubeSearchItem item : mArraySearchVideos) {
			if (checkAddedVideo(item.id.videoId))
				item.isAdded = true;
			else
				item.isAdded = false;
		}

		mAdapterVideo.notifyDataSetChanged();
	}

	private void updateSearchVideos(ArrayList<YoutubeSearchItem> items) {

		mArraySearchVideos.clear();

		if (items == null) {
			mAdapterVideo.notifyDataSetChanged();;
			return;
		}

		for (YoutubeSearchItem item : items) {
			if (checkAddedVideo(item.id.videoId))
				item.isAdded = true;

			mArraySearchVideos.add(item);
		}

		mAdapterVideo.notifyDataSetChanged();
	}
	
	private void viewYoutubeInfo(YoutubeInfo youtubeInfo)
	{
		String videoId = youtubeInfo.id;
		YoutubeSearchItem searchItem = null;
		for (YoutubeSearchItem tmp : mArraySearchVideos) {
			if (tmp.id.videoId.equalsIgnoreCase(videoId)) {
				searchItem = tmp;
				break;
			}
		}
		
		if (searchItem == null || searchItem.info != null)
			return;
		
		searchItem.info = youtubeInfo;
		searchItem.isLoading = false;
		
		mAdapterVideo.notifyDataSetChanged();
	}

	private boolean checkAddedVideo(String video_url) {

		for (Video video : mArrayVideo ){
			if (video.url.equalsIgnoreCase(video_url))
				return true;
		}

		return false;
	}

	private void alertSignInDiag()
	{
		AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(this)
		.setTitle("Alert")
		.setIcon(android.R.drawable.stat_sys_warning)
		.setMessage("Guest user can't change the content. Do you want to sign in?")
		.setCancelable(false)
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finishActivity("sign");
			}
		});

		AlertDialog alertDlg = alertDlgBuilder.create();
		alertDlg.setCanceledOnTouchOutside(false);
		alertDlg.show();
	}

	private void getVideoListByConcept(int start, String conceptId){

		GetVideoLstParam param = new GetVideoLstParam();
		param.checksum = mDbMgr.getVideoCheckSumByConcept(conceptId);
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		param.conceptId = conceptId;

		WebApiInstance.getInstance().executeAPI(Type.GET_VIDEOS_CONCEPT, param, this);

	}

	private void getVideoListResult(GetVideoLstParam param, Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetVideoLstResult resultBean = (GetVideoLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (videoCnt == 0) {					
					mDbMgr.delVideoByConcept(param.conceptId);
				}

				for (VideoResult item : resultBean.videos)
				{
					mDbMgr.addVideoItem(item);
					videoCnt++;
				}

				if (resultBean.eof == false) {
					getVideoListByConcept (videoCnt, param.conceptId);					
					return;
				} else {
					videoCnt = 0;
					getAddedVideos();
					re_checkSearchVideos();
				}

			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		videoCnt = 0;

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void addVideo(String videoUrl){

		mWaitDlg = new WaitDialog(this);
		mWaitDlg.setMessage("Please wait ...");
		mWaitDlg.show();

		AddVideoParam param = new AddVideoParam();

		param.token = mPrefs.getToken();
		param.concept_id = mConceptId;
		param.video = videoUrl;
		param.isPrivate = false;

		WebApiInstance.getInstance().executeAPI(Type.ADD_VIDEO, param, this);
	}

	private void onAddVideoResult(AddVideoParam param, Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			return;
		}

		if (obj != null){
			AddVideoResult resultBean = (AddVideoResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				getVideoListByConcept(videoCnt, mConceptId);
				return;
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}
	private void deleteContent(String type, String contentId){

		mWaitDlg = new WaitDialog(this);
		mWaitDlg.setMessage("Please wait ...");
		mWaitDlg.show();

		DelContentParam param = new DelContentParam();

		param.token = mPrefs.getToken();
		param.type = type;
		param.content_id = contentId;

		WebApiInstance.getInstance().executeAPI(Type.DELETE_CONTENT, param, this);
	}


	private void onDelContentResult(DelContentParam param, Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			return;
		}

		if (obj != null){
			DelContentResult resultBean = (DelContentResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				getVideoListByConcept(videoCnt, mConceptId);
				return;
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void getYoutubeSearch(){

		String search_key = mTxtSearch.getText().toString().trim();
		if (search_key == null || search_key.isEmpty() || isSearching == true)
			return;

		isSearching = true;
		mTxtSearch.setEnabled(false);

		search_key = search_key.replace(" ", "%20");

		GetYoutubeSearchParam param = new GetYoutubeSearchParam();

		param.search_key = search_key;

		WebApiInstance.getInstance().executeAPI(Type.GET_YOUTUBE_SEARCH, param, this);

	}

	private void getYoutubeSearchResult(GetYoutubeSearchParam param, Object obj) {

		if (obj == null) {
			//			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetYoutubeSearchResult resultBean = (GetYoutubeSearchResult)obj;

			if (resultBean.items != null) {
				updateSearchVideos(resultBean.items);
			} else if (resultBean.error != null) {
				CustomToast.makeCustomToastShort(this, resultBean.error.message);
			} else {
				//				CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			}
		}

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}

		isSearching = false;
		mTxtSearch.setEnabled(true);
	}
	
	private void getYoutubeInfo(String youtubeId, Object exParam){

		GetYoutubeInfoParam param = new GetYoutubeInfoParam();
		param.exParam = exParam;
		param.youtubeId = youtubeId;

		WebApiInstance.getInstance().executeAPI(Type.GET_YOUTUBE_INFO, param, this);

	}

	private void getYoutubeInfoResult(GetYoutubeInfoParam param, Object obj) {

		if (obj == null) {
			//			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetYoutubeInfoResult resultBean = (GetYoutubeInfoResult)obj;

			if (resultBean.items != null && resultBean.items.size() > 0) {

				viewYoutubeInfo (resultBean.items.get(0));


			} else if (resultBean.error != null) {
				CustomToast.makeCustomToastShort(this, resultBean.error.message);
			} else {
				//				CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			}
		}

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
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
		case GET_VIDEOS_CONCEPT:
			getVideoListResult((GetVideoLstParam)parameter, result);			
			break;
		case ADD_VIDEO:
			onAddVideoResult((AddVideoParam) parameter, result);
			break;
		case DELETE_CONTENT:
			onDelContentResult((DelContentParam)parameter, result);
			break;
		case GET_YOUTUBE_SEARCH:
			getYoutubeSearchResult((GetYoutubeSearchParam)parameter, result);
			break;
		case GET_YOUTUBE_INFO:
			getYoutubeInfoResult((GetYoutubeInfoParam)parameter, result);
			break;
		default:
			break;
		}
	}

	public class VideoAdapter extends ArrayAdapter<YoutubeSearchItem> {
		Activity activity;
		int layoutResourceId;
		ArrayList<YoutubeSearchItem> item = new ArrayList<YoutubeSearchItem>();

		// constuctor
		public VideoAdapter(Activity activity, int layoutId, ArrayList<YoutubeSearchItem> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public int getCount() {
			return item.size();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			VideoHolder holder = null;	

			YoutubeSearchItem videoItem = getItem(position);

			// inflate the view
//			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_search_video, null);

				holder = new VideoHolder(position);

				holder.imgAdd = (ImageView) convertView.findViewById(R.id.imgAdd);
				holder.imgDel = (ImageView) convertView.findViewById(R.id.imgDelete);
				holder.imgTick = (ImageView) convertView.findViewById(R.id.imgTick);

				holder.imgAdd.setOnClickListener(holder);
				holder.imgDel.setOnClickListener(holder);

				holder.imgThumb = (HyIconView) convertView.findViewById(R.id.imgVideoThumb);
				holder.imgThumb.failedBp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_youtube);

				holder.txtTitle = (TextView) convertView.findViewById(R.id.txtVideoTitle);
				holder.txtUploader = (TextView) convertView.findViewById(R.id.txtVideoUploader);
				holder.txtViews = (TextView) convertView.findViewById(R.id.txtViews);				

				convertView.setTag(holder);
//			} 
//			else
//				holder = (VideoHolder) convertView.getTag();			

			holder.txtTitle.setText(videoItem.snippet.title);
			holder.txtUploader.setText("By " + videoItem.snippet.channelTitle);
			if (videoItem.info == null) {
//				if (!videoItem.isLoading) {
					videoItem.isLoading = true;
					getYoutubeInfo(videoItem.id.videoId, null);
//				}
			} else {
				holder.txtUploader.setText("By " + videoItem.info.snippet.channelTitle);
				holder.txtViews.setText(videoItem.info.statistics.viewCount + " Views");
			}

			if (videoItem.isAdded == true) {
				holder.imgAdd.setVisibility(View.GONE);
				holder.imgDel.setVisibility(View.VISIBLE);

				holder.imgTick.setVisibility(View.VISIBLE);
			} else {
				holder.imgAdd.setVisibility(View.VISIBLE);
				holder.imgDel.setVisibility(View.GONE);

				holder.imgTick.setVisibility(View.GONE);
			}				

			if (holder.imgThumb != null)
				ImageLoader.getInstance().displayImage(Constants.HTTP_YOUTUBE_IMG + videoItem.id.videoId +"/default.jpg", holder.imgThumb, 0, 0);

			return convertView;
		}

		public class VideoHolder implements OnClickListener{
			HyIconView imgThumb;

			ImageView imgAdd;
			ImageView imgDel;
			ImageView imgTick;

			TextView txtTitle, txtUploader, txtViews;

			int itemIndex;

			public VideoHolder(int index) {
				super();
				itemIndex = index;
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int viewId = v.getId();
				String contentId;

				YoutubeSearchItem youtubeItem = mArraySearchVideos.get(itemIndex);
				if (youtubeItem == null)
					return;			

				switch (viewId)
				{
				case R.id.imgVideoThumb:					
					startContentViewActivity(youtubeItem.id.videoId, "video");
					break;
				case R.id.imgAdd:
					addVideo("http://youtu.be/" + youtubeItem.id.videoId);
					break;
				case R.id.imgDelete:
					contentId = getVideoFromYoutubeId(youtubeItem.id.videoId);

					if (contentId != null)
						deleteContent("video", contentId);
					break;
				default:
					break;
				}
			}
		}
	}
}
