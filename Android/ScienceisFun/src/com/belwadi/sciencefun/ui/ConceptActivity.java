package com.belwadi.sciencefun.ui;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.*;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.ui.HomeActivity.State;
import com.belwadi.sciencefun.ui.RepositoryActivity.RepoState;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.utils.Utility;
import com.belwadi.sciencefun.view.AlertDialogManager;
import com.belwadi.sciencefun.view.AutosizeTextView;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.ExpandedListView;
import com.belwadi.sciencefun.view.HyIconView;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.WebApiCallback;
import com.belwadi.sciencefun.webservice.WebApiInstance;
import com.belwadi.sciencefun.webservice.WebApiInstance.Type;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

public class ConceptActivity extends YouTubeFailureRecoveryActivity implements OnClickListener, WebApiCallback{

	//	public static final String DEVELOPER_KEY = "AIzaSyCs6Pvw0of1MLEDSOMjzJ3s9vpx4HqF7t4";
	private final int REPOSITORY_ACTIVITY = 0;
	private final int FRIEND_PROFILE_ACTIVITY = 1;
	private final int SEARCH_VIDEO_ACTIVITY = 2;

	DatabaseManager mDbMgr;
	AppPreferences mPrefs;
	UserInfo mUserInfo;

	AlertDialogManager mAlertDiag = new AlertDialogManager();

	String mConceptId;	
	Concept mConcept;

	Video mDefVideo;
	Reference mDefRef;
	Note mDefNote;

	YouTubePlayerFragment mYouTubeFragment;
	YouTubePlayer mYoutubePlayer;

	TextView mTxtUserName;
	HyIconView mImgUserIcon;
	
	Scientist mScientist;
	
	PopupMenu mPopMenu;

	TextView mMoreRef, mTxtExpand;
	TextView mTxtConceptTitle, mTxtCredit, mTxtRefCaption, mTxtRefTitle, mTxtRefUrl, mTxtRefDscp, mTxtNoteDate, mTxtNoteUserName;
	TextView mTxtDefVideoTitle, mTxtDefVideoUploader, mTxtDefVideoViews;	
	AutosizeTextView mTxtConceptText, mTxtNote;
	EditText mTxtNewNote;
	
	RelativeLayout mLayGuestNote;
	TextView mTxtGuestNote;

	HyIconView mImgConcept, mIconRef, mImgNoteUser;

	RelativeLayout mLayImage, mLayRef, mLayNote;

	Button mBtnBack;
	ImageView mImgAddNote, mImgAddVideo, mImgDelDefNote;

	private ArrayList<Video> mArrayVideo = null;
	private VideoAdapter mAdapterVideo;
	private /*Expanded*/ListView mListviewVideo;

	private ArrayList<Note> mArrayNote = null;
	private NoteAdapter mAdapterNote;
	private /*Expanded*/ListView mListviewNote;

	private ArrayList<UserInfo> mArrayClassmates;

	WaitDialog mWaitDlg;

	int videoCnt, noteCnt, refCnt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_concept);

		mDbMgr = new DatabaseManager(this);
		mPrefs = new AppPreferences(this);
		
		mScientist = GlobalValue.getInstance().getScientist(this);

		mUserInfo = GlobalValue.getInstance().getCurrentUser();

		mConceptId = getIntent().getStringExtra("concept_id");
		if (mConceptId == null || mConceptId.isEmpty())
			finish();

		mConcept = mDbMgr.getConcept(mConceptId);

		mArrayClassmates = new ArrayList<UserInfo>();

		videoCnt = noteCnt = refCnt= 0;

		initView();
		initViewPopupMenu();

		if (mPrefs.isLogIn()) {
			onUpdateUserInfo();
			getClassmateList();
		}

		updateContent();
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
			boolean wasRestored) {
		if (!wasRestored) {
			if (mDefVideo != null)
				player.cueVideo(mDefVideo.url);
			mYoutubePlayer = player;
			//	      player.cueVideo("nCgQDjiotG0");
		}
	}

	@Override
	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		return (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
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
		mYouTubeFragment =
				(YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
		mYouTubeFragment.initialize(Constants.DEVELOPER_KEY, this);

		mTxtUserName = (TextView) findViewById(R.id.txtUserName);
		mImgUserIcon = (HyIconView) findViewById(R.id.imgUserIcon);
		mImgUserIcon.setOnClickListener(this);

		mTxtConceptTitle = (TextView) findViewById(R.id.txtConceptTitle);
		mTxtConceptText = (AutosizeTextView) findViewById(R.id.txtConceptText);
		mTxtConceptText.setOnClickListener(this);

		mImgConcept = (HyIconView) findViewById(R.id.imgConcept);
		mImgConcept.failedBp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_image_not);
		mTxtCredit = (TextView) findViewById(R.id.txtCredit);

		mTxtDefVideoTitle = (TextView) findViewById(R.id.txtDefaultVideoTitle);
		mTxtDefVideoUploader= (TextView) findViewById(R.id.txtDefaultVideoUploader);
		mTxtDefVideoViews = (TextView) findViewById(R.id.txtDefaultVideoViews);

		mLayImage = (RelativeLayout) findViewById(R.id.layImage);
		mLayRef = (RelativeLayout) findViewById(R.id.layReference);
		mLayNote = (RelativeLayout) findViewById(R.id.layNote);		

		mTxtRefCaption = (TextView) findViewById(R.id.txtRefCaption);
		mTxtRefTitle = (TextView) findViewById(R.id.txtRefName);
		mTxtRefUrl = (TextView) findViewById(R.id.txtRefUrl);
		mTxtRefDscp = (TextView) findViewById(R.id.txtRefDescription);		
		mIconRef = (HyIconView) findViewById(R.id.imgRefIcon);
		mIconRef.imageView.setImageResource(R.drawable.icon_ref);
		mIconRef.flagShowProg = false;
		
		mTxtGuestNote = (TextView) findViewById(R.id.txtGuestNote);
		initGuestNote();
		mLayGuestNote = (RelativeLayout) findViewById(R.id.layGuestNote);

		mTxtNoteDate = (TextView) findViewById(R.id.txtNoteDate);
		mTxtNote = (AutosizeTextView) findViewById(R.id.txtNote);
		mTxtNoteUserName = (TextView) findViewById(R.id.txtNoteUserName);
		mImgNoteUser = (HyIconView) findViewById(R.id.imgNoteUser);
		mImgNoteUser.imageView.setImageResource(R.drawable.empty_pic);		

		mTxtNewNote = (EditText) findViewById(R.id.txtNewNote);
		mImgDelDefNote = (ImageView) findViewById(R.id.imgDelDefNote);
		mImgDelDefNote.setOnClickListener(this);

		mImgAddVideo = (ImageView) findViewById(R.id.imgAddVideo);
		mImgAddVideo.setOnClickListener(this);

		mImgAddNote = (ImageView) findViewById(R.id.imgAddNote);
		mImgAddNote.setOnClickListener(this);
		
		mTxtExpand = (TextView) findViewById(R.id.txtExpand);
		mTxtExpand.setOnClickListener(this);

		mMoreRef = (TextView) findViewById(R.id.txtMoreRef);
		mMoreRef.setOnClickListener(this);

		mBtnBack = (Button) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(this);

		mArrayVideo = new ArrayList<Video>();
		mAdapterVideo = new VideoAdapter(this, R.layout.item_concept_video2, mArrayVideo);

		mListviewVideo = (ListView) findViewById(R.id.lstVideo);		
		mListviewVideo.setAdapter(mAdapterVideo);
		mListviewVideo.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				Video videoItem = mArrayVideo.get(position);
				startContentViewActivity(videoItem.id, "video");
			}
		});

		mArrayNote = new ArrayList<Note>();
		mAdapterNote = new NoteAdapter(this, R.layout.item_concept_note2, mArrayNote);

		mListviewNote = (ListView) findViewById(R.id.lstNote);		
		mListviewNote.setAdapter(mAdapterNote);
		mListviewNote.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
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
			mLayGuestNote.setVisibility(View.GONE);
			
			mTxtNewNote.setVisibility(View.VISIBLE);
			mImgAddNote.setVisibility(View.VISIBLE);
		} else {
			mLayGuestNote.setVisibility(View.VISIBLE);
			
			mTxtNewNote.setVisibility(View.GONE);
			mImgAddNote.setVisibility(View.GONE);
		}
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
	
	private void initViewPopupMenu()
	{
		MenuItem item = mPopMenu.getMenu().getItem(1);
		
		if (mPrefs.isLogIn())
			item.setTitle("Sign Out");
		else
			item.setTitle("Sign In");
	}
	
	private void initGuestNote ()
	{
		String strNote = "You have not enrolled in any class. \nSign In  or  Sign Up for Free.";
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
		
		mTxtGuestNote.setText(ss);
		mTxtGuestNote.setMovementMethod(LinkMovementMethod.getInstance());
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

	private void updateContent()
	{
		getDefaultContent();

		mTxtConceptTitle.setText(mConcept.title);
		mTxtConceptText.setText(mConcept.text);

		if (mConcept.image2 != null && !mConcept.image2.isEmpty())
			ImageLoader.getInstance().displayImage(mConcept.image2, mImgConcept, 0, 0);

		mTxtCredit.setClickable(true);
		mTxtCredit.setMovementMethod(LinkMovementMethod.getInstance());
		String creditHtml = "<a href='" + mConcept.image2_source + "'>" +  mConcept.image2_credit + "</a>";
		mTxtCredit.setText(Html.fromHtml(creditHtml));

		setOtherVideos();
		setNotes();
		getRefListByConcept(refCnt);
		getVideoListByConcept(videoCnt, mConceptId);
		getNoteListByConcept(noteCnt);
		
		viewDefaultRef();
		viewDefaultNote();
	}

	private void getDefaultVideo() {
		if (mPrefs.isLogIn()) {
			mDefVideo = mDbMgr.getDefaultVideo(mConceptId, mUserInfo._id);
		}

		if (mDefVideo == null)
			mDefVideo = mDbMgr.getDefaultVideo(mConceptId, "admin");

		if (mDefVideo != null) {
			if (mYoutubePlayer != null)
				mYoutubePlayer.cueVideo(mDefVideo.url);
			getYoutubeInfo(mDefVideo.url, true, null);
		}
	}

	private void getDefaultRef() {
		if (mPrefs.isLogIn()) {
			mDefRef = mDbMgr.getDefaultReference(mConceptId, mUserInfo._id);
		}

		if (mDefRef == null)
			mDefRef = mDbMgr.getDefaultReference(mConceptId, "admin");
	}

	private void getDefaultNote() {
		if (mPrefs.isLogIn()) {
			mDefNote = mDbMgr.getDefaultNote(mConceptId, mUserInfo._id);
		}

		if (mDefNote == null)
			mDefNote = mDbMgr.getDefaultNote(mConceptId, "admin");
	}

	private void viewDefaultRef() {
		if (mDefRef != null) {
//			mLayRef.setVisibility(View.VISIBLE);

			mTxtRefTitle.setText(mDefRef.title);
			mTxtRefUrl.setText(mDefRef.url);
			mTxtRefDscp.setText(mDefRef.description);

			if (mDefRef.image != null && !mDefRef.image.isEmpty())
				ImageLoader.getInstance().displayImage(mDefRef.image, mIconRef, 0, 0);
		} else {
			mLayRef.setVisibility(View.GONE);			
		}		
	}

	private void viewDefaultNote() {
		if (mDefNote != null) {
			mLayNote.setVisibility(View.VISIBLE);

			mTxtNoteDate.setText(Utility.getDateStringFromTimestamp(mDefNote.create_date));
			mTxtNote.setText(mDefNote.text);

			UserInfo noteOwner =  mDbMgr.getUser(mDefNote.ownerId);
			if (noteOwner != null) {
				mTxtNoteUserName.setText(noteOwner.name);
				if (noteOwner.photo_url != null && !noteOwner.photo_url.isEmpty())
					ImageLoader.getInstance().displayImage(noteOwner.photo_url, mImgNoteUser, 0, 0);
				else
					mImgNoteUser.imageView.setImageResource(R.drawable.empty_pic);

				if (noteOwner._id.equalsIgnoreCase(mUserInfo._id))
					mImgDelDefNote.setVisibility(View.VISIBLE);
				else
					mImgDelDefNote.setVisibility(View.GONE);
			}

		} else {
			mLayNote.setVisibility(View.GONE);
		}
	}

	private void getDefaultContent()
	{
		getDefaultVideo();
		getDefaultRef();
		getDefaultNote();
	}

	private void setOtherVideos()
	{
		int indexAdmin = 0;
		mArrayVideo.clear();
		ArrayList<Video> tmp = mDbMgr.getVideoByConceptUser(mConceptId, mUserInfo._id, mArrayClassmates);
		for (int i = 0; i < tmp.size(); i++) {
			Video item = tmp.get(i);
			
			if (mDefVideo != null && item.url.equalsIgnoreCase(mDefVideo.url))
				continue;
			
			if (mDefVideo == null) {
				mDefVideo = item;
				continue;
			}
			
			if (item.ownerId.equalsIgnoreCase("admin")) {
				indexAdmin = i;
				mArrayVideo.add(item);
			} else if (indexAdmin + 1 == i) {
				mArrayVideo.add(item);
			} else {
				mArrayVideo.add(indexAdmin, item);
			}
			
			if (mArrayVideo.size() >= 6)
				break;		
		}
		
		setListViewHeightBasedOnChildren (mListviewVideo, true);
		mAdapterVideo.notifyDataSetChanged();		
	}

	private void setNotes()
	{
		mArrayNote.clear();

		ArrayList<Note> tmp = mDbMgr.getNoteByConceptUser(mConceptId, mUserInfo._id, mArrayClassmates);		
		for (Note item : tmp) {

			if (mDefNote != null && item.id.equalsIgnoreCase(mDefNote.id)) {
				continue;
			}
			
			if (mDefNote == null) {
				mDefNote = item;
				continue;
			}

			mArrayNote.add(item);
		}
		
		setListViewHeightBasedOnChildren (mListviewNote, false);
		mAdapterNote.notifyDataSetChanged();
	}
	
	public void setListViewHeightBasedOnChildren(ListView listView, boolean isVideoList) {

	    ListAdapter listAdapter = listView.getAdapter();
	    if (listAdapter == null) {
	        return;
	    }

	    int totalHeight = 0;
	    for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
	    	View listItem;
	    	if (isVideoList)
	    		listItem = ((VideoAdapter)listAdapter).getViewForSize(i, null, listView);
	    	else
	    		listItem = ((NoteAdapter)listAdapter).getViewForSize(i, null, listView);
	        listItem.measure(0, 0); 
	        totalHeight += listItem.getMeasuredHeight();
	    }

	    ViewGroup.LayoutParams params = listView.getLayoutParams();
	    params.height = totalHeight
	            + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	    listView.setLayoutParams(params);
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();

		switch (id)
		{
		case R.id.txtMoreRef:
			startRepositoryActivity(RepoState.REF.toString());
			break;
		case R.id.btnBack:
			finishActivity("");
			break;
		case R.id.imgAddNote:
			if (!mPrefs.isLogIn()) {
				alertSignInDiag();
				return;
			}

			String strNote = mTxtNewNote.getText().toString();
			if (strNote == null || strNote.isEmpty())
				return;
			addNote (strNote, false);
			break;
		case R.id.imgDelDefNote:
			if (mDefNote == null)
				return;
			deleteContent("note", mDefNote.id);
			break;
		case R.id.imgAddVideo:
			startSearchVideoActivity();
			break;
		case R.id.imgUserIcon:
			mPopMenu.show();
//			if (mPrefs.isLogIn())
//				finishActivity("profile");
//			else
//				startSigninActivity();
			break;
		case R.id.txtExpand:
		case R.id.txtConceptText:
			if (mTxtConceptText.checkTextViewEllipse()) {
				mTxtConceptText.setMaxLines(AutosizeTextView.MAX_LINES_NO_ELLIPSIS);

				mLayImage.setVisibility(View.VISIBLE);
				if (mDefRef != null)
					mLayRef.setVisibility(View.VISIBLE);
				else
					mLayRef.setVisibility(View.GONE);
				
				mTxtRefCaption.setVisibility(View.VISIBLE);
				
				mTxtExpand.setText("Less");
			}
			else {
				mTxtConceptText.setMaxLines(AutosizeTextView.MAX_LINES_ELLIPSIS);

				mLayImage.setVisibility(View.GONE);
				mLayRef.setVisibility(View.GONE);
				mTxtRefCaption.setVisibility(View.GONE);
				
				mTxtExpand.setText("More");
			}
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode != Activity.RESULT_OK)
			return;

		if (requestCode == REPOSITORY_ACTIVITY
				|| requestCode == FRIEND_PROFILE_ACTIVITY
				|| requestCode == SEARCH_VIDEO_ACTIVITY)
		{
			String goPage = data.getStringExtra("go_page");

			if (goPage != null && !goPage.isEmpty()) {
				finishActivity (goPage);
			}
		}

		if (requestCode == SEARCH_VIDEO_ACTIVITY) {
			getDefaultVideo();
			setOtherVideos();
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
	
	private void openGuestProfile()
	{
		if (mScientist == null || mScientist.name == null)
			return;
		
		String searchName = mScientist.name.replace(" ", "%20");
		String url = "http://www.google.com/search?q=" + searchName;
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}
	
	private void startSigninActivity() {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(this, SigninActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
		finish();
	}

	private void startSearchVideoActivity()
	{
		Intent intent = new Intent();
		intent.setClass(this, SearchVideoActivity.class);
		intent.putExtra("concept_id", mConceptId);
		startActivityForResult(intent, SEARCH_VIDEO_ACTIVITY);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}

	private void startFriendProfileActivity(String userId)
	{		
		Intent intent = new Intent();
		intent.setClass(this, FriendProfileActivity.class);
		intent.putExtra("user_id", userId);
		intent.putExtra("report_type", "content");
		startActivityForResult(intent, FRIEND_PROFILE_ACTIVITY);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}

	private void startRepositoryActivity(String extra)
	{
		Intent intent = new Intent();
		intent.setClass(this, RepositoryActivity.class);
		intent.putExtra("concept_id", mConceptId);
		intent.putExtra("content", extra);
		startActivityForResult(intent, REPOSITORY_ACTIVITY);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}

	private void viewDefaultVideoInfo(YoutubeInfo youtubeInfo) {
		mTxtDefVideoTitle.setText(youtubeInfo.snippet.title);
		mTxtDefVideoUploader.setText("By " + youtubeInfo.snippet.channelTitle);
		mTxtDefVideoViews.setText(youtubeInfo.statistics.viewCount + " Views");
	}

	private void viewYoutubeInfo(LinearLayout layVideoInfo, YoutubeInfo youtubeInfo)
	{
		if (layVideoInfo == null)
			return;

		TextView txtTitle, txtUploader, txtViews;

		txtTitle = (TextView) layVideoInfo.findViewById(R.id.txtVideoTitle);
		txtUploader = (TextView) layVideoInfo.findViewById(R.id.txtVideoUploader);
		txtViews = (TextView) layVideoInfo.findViewById(R.id.txtViews);

		txtTitle.setText(youtubeInfo.snippet.title);
		txtUploader.setText("By " + youtubeInfo.snippet.channelTitle);
		txtViews.setText(youtubeInfo.statistics.viewCount + " Views");
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
				finishActivity("Sign In");
			}
		});

		AlertDialog alertDlg = alertDlgBuilder.create();
		alertDlg.setCanceledOnTouchOutside(false);
		alertDlg.show();
	}

	private void addNote(String note, boolean isPrivate){

		mWaitDlg = new WaitDialog(ConceptActivity.this);
		mWaitDlg.setMessage("Add Note ...");
		mWaitDlg.show();

		AddNoteParam param = new AddNoteParam();
		param.token = mPrefs.getToken();
		param.concept_id = mConceptId;
		param.note = note;
		param.isPrivate = isPrivate;

		WebApiInstance.getInstance().executeAPI(Type.ADD_NOTE, param, this);
	}


	private void onAddNoteResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			AddNoteResult resultBean = (AddNoteResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				mTxtNewNote.setText("");
				getNoteListByConcept(noteCnt);
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
					getDefaultVideo();
					setOtherVideos();
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

	private void getRefListByConcept(int start) {

		GetRefLstParam param = new GetRefLstParam();
		param.checksum = mDbMgr.getRefCheckSumByConcept(mConcept.id);
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		param.conceptId = mConcept.id;

		WebApiInstance.getInstance().executeAPI(Type.GET_REFS_CONCEPT, param, this);
	}

	private void getRefListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetRefLstResult resultBean = (GetRefLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (refCnt == 0) {
					mDbMgr.delReferenceByConcept(mConcept.id);
				}

				for (ReferenceResult item : resultBean.references)
				{
					mDbMgr.addReferenceItem(item);
					refCnt++;
				}

				if (resultBean.eof == false) {
					getRefListByConcept(refCnt);
					return;
				} else {
					refCnt = 0;
					getDefaultRef();
					viewDefaultRef();			
				}

			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		refCnt = 0;

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void getNoteListByConcept(int start) {

		GetNoteLstParam param = new GetNoteLstParam();
		param.checksum = mDbMgr.getNoteCheckSumByConcept(mConcept.id);
		param.conceptId = mConcept.id;
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_NOTES_CONCEPT, param, this);
	}

	private void getNoteListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetNoteLstResult resultBean = (GetNoteLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (noteCnt == 0) {
					mDbMgr.delNoteByConcept(mConcept.id);
				}

				for (NoteResult item : resultBean.notes)
				{
					mDbMgr.addNoteItem(item);
					noteCnt++;
				}

				if (resultBean.eof == false) {
					getNoteListByConcept(noteCnt);
					return;
				} else {
					noteCnt = 0;
					getDefaultNote();
					setNotes();
					viewDefaultNote();
				}

			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}

		noteCnt = 0;

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void setDefaultContent(String type, String contentId){

		mWaitDlg = new WaitDialog(this);
		mWaitDlg.setMessage("Please wait ...");
		mWaitDlg.show();

		SetDefaultContentParam param = new SetDefaultContentParam();

		param.token = mPrefs.getToken();
		param.concept_id = mConceptId;
		param.type = type;
		param.content_id = contentId;

		WebApiInstance.getInstance().executeAPI(Type.SET_DEFAULT, param, this);
	}


	private void onSetDefaultResult(SetDefaultContentParam param, Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			return;
		}

		if (obj != null){
			SetDefaultContentResult resultBean = (SetDefaultContentResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				if (param.type.equalsIgnoreCase("video"))
					getVideoListByConcept(videoCnt, mConceptId);
				else if (param.type.equalsIgnoreCase("note"))
					getNoteListByConcept(noteCnt);				
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
				if (param.type.equalsIgnoreCase("video"))
					getVideoListByConcept(videoCnt, mConceptId);
				else if (param.type.equalsIgnoreCase("note"))
					getNoteListByConcept(noteCnt);
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

	private void getYoutubeInfo(String youtubeId, boolean isDefault, Object exParam){

		GetYoutubeInfoParam param = new GetYoutubeInfoParam();
		param.isDefault = isDefault;
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

				if (param.isDefault) {
					viewDefaultVideoInfo(resultBean.items.get(0));
				} else {
					LinearLayout layVideoInfo = (LinearLayout) param.exParam;
					viewYoutubeInfo (layVideoInfo, resultBean.items.get(0));
				}


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
		case GET_REFS_CONCEPT:
			getRefListResult(result);			
			break;
		case GET_NOTES_CONCEPT:
			getNoteListResult(result);			
			break;
		case ADD_NOTE:
			onAddNoteResult(result);
			break;
		case DELETE_CONTENT:
			onDelContentResult((DelContentParam)parameter, result);
			break;
		case SET_DEFAULT:
			onSetDefaultResult((SetDefaultContentParam)parameter, result);
			break;
		case GET_YOUTUBE_INFO:
			getYoutubeInfoResult((GetYoutubeInfoParam)parameter, result);
			break;
		default:
			break;
		}
	}

	public class VideoAdapter extends ArrayAdapter<Video> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Video> item = new ArrayList<Video>();

		// constuctor
		public VideoAdapter(Activity activity, int layoutId, ArrayList<Video> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public int getCount() {
			return item.size();
		}
		
		public View getViewForSize(final int position, View convertView, ViewGroup parent) {
			VideoHolder holder = null;	

			Video videoItem = getItem(position);

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_concept_video2, null);

				holder = new VideoHolder(position);

				holder.layVideoInfo = (LinearLayout) convertView.findViewById(R.id.layVideoInfo);

				holder.txtAddUser = (TextView) convertView.findViewById(R.id.txtAddUser);
				holder.imgAddUser = (HyIconView) convertView.findViewById(R.id.imgAddUser);
				holder.imgAddUser.imageView.setImageResource(R.drawable.empty_pic);

				holder.iconDefault = (ImageView) convertView.findViewById(R.id.imgDefault);
				holder.iconDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
				holder.iconReport = (ImageView) convertView.findViewById(R.id.imgReport);

				holder.imgThumb = (HyIconView) convertView.findViewById(R.id.imgVideoThumb);
			} 

			if (!mPrefs.isLogIn()) {
				holder.iconDefault.setVisibility(View.GONE);
				holder.iconDelete.setVisibility(View.GONE);
				holder.iconReport.setVisibility(View.GONE);
			} else  if (videoItem.ownerId.equalsIgnoreCase(mUserInfo._id)) {
				holder.iconDelete.setVisibility(View.VISIBLE);
				holder.iconReport.setVisibility(View.GONE);
			} else {
				holder.iconDelete.setVisibility(View.GONE);
				holder.iconReport.setVisibility(View.VISIBLE);
			}

			if (videoItem.ownerId.equalsIgnoreCase("admin")) {
				holder.iconDelete.setVisibility(View.GONE);
				holder.iconReport.setVisibility(View.GONE);
			}

			UserInfo videoOwner =  mDbMgr.getUser(videoItem.ownerId);
			if (videoOwner != null) {
				holder.txtAddUser.setVisibility(View.VISIBLE);
				holder.imgAddUser.setVisibility(View.VISIBLE);
				holder.txtAddUser.setText(videoOwner.name);
			} else {
				holder.txtAddUser.setVisibility(View.GONE);
				holder.imgAddUser.setVisibility(View.GONE);
			}

			return convertView;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			VideoHolder holder = null;	

			Video videoItem = getItem(position);

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_concept_video2, null);

				holder = new VideoHolder(position);

				holder.layVideoInfo = (LinearLayout) convertView.findViewById(R.id.layVideoInfo);

				holder.txtAddUser = (TextView) convertView.findViewById(R.id.txtAddUser);
				holder.imgAddUser = (HyIconView) convertView.findViewById(R.id.imgAddUser);
				holder.imgAddUser.imageView.setImageResource(R.drawable.empty_pic);

				holder.iconDefault = (ImageView) convertView.findViewById(R.id.imgDefault);
				holder.iconDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
				holder.iconReport = (ImageView) convertView.findViewById(R.id.imgReport);

				holder.iconDefault.setOnClickListener(holder);
				holder.iconDelete.setOnClickListener(holder);
				holder.iconReport.setOnClickListener(holder);

				holder.imgThumb = (HyIconView) convertView.findViewById(R.id.imgVideoThumb);
				holder.imgThumb.failedBp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_youtube);				 

				convertView.setTag(holder);
			} 
			else
				holder = (VideoHolder) convertView.getTag();			

			if (!mPrefs.isLogIn()) {
				holder.iconDefault.setVisibility(View.GONE);
				holder.iconDelete.setVisibility(View.GONE);
				holder.iconReport.setVisibility(View.GONE);
			} else  if (videoItem.ownerId.equalsIgnoreCase(mUserInfo._id)) {
				holder.iconDelete.setVisibility(View.VISIBLE);
				holder.iconReport.setVisibility(View.GONE);
			} else {
				holder.iconDelete.setVisibility(View.GONE);
				holder.iconReport.setVisibility(View.VISIBLE);
			}

			if (videoItem.ownerId.equalsIgnoreCase("admin")) {
				holder.iconDelete.setVisibility(View.GONE);
				holder.iconReport.setVisibility(View.GONE);
			}

			UserInfo videoOwner =  mDbMgr.getUser(videoItem.ownerId);
			if (videoOwner != null) {
				holder.txtAddUser.setVisibility(View.VISIBLE);
				holder.imgAddUser.setVisibility(View.VISIBLE);

				holder.txtAddUser.setText(videoOwner.name);
				if (videoOwner.photo_url != null && !videoOwner.photo_url.isEmpty())
					ImageLoader.getInstance().displayImage(videoOwner.photo_url, holder.imgAddUser, 0, 0);
				else
					holder.imgAddUser.imageView.setImageResource(R.drawable.empty_pic);
			} else {
				holder.txtAddUser.setVisibility(View.GONE);
				holder.imgAddUser.setVisibility(View.GONE);
			}

			if (holder.imgThumb != null)
				ImageLoader.getInstance().displayImage(Constants.HTTP_YOUTUBE_IMG + videoItem.url +"/default.jpg", holder.imgThumb, 0, 0);

			if (holder.layVideoInfo != null && videoItem.isLoading == false) {
				videoItem.isLoading = true;
				getYoutubeInfo(videoItem.url, false, holder.layVideoInfo);
			}
			return convertView;
		}

		public class VideoHolder implements OnClickListener{
			TextView txtAddUser;
			HyIconView imgAddUser;
			HyIconView imgThumb;

			LinearLayout layVideoInfo;			

			ImageView iconDefault;
			ImageView iconDelete;
			ImageView iconReport;

			int itemIndex;

			public VideoHolder(int index) {
				super();
				itemIndex = index;
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int viewId = v.getId();

				Video video = mArrayVideo.get(itemIndex);
				if (video == null)
					return;			

				switch (viewId)
				{
				case R.id.imgVideoThumb:					
					startContentViewActivity(video.id, "video");
					break;
				case R.id.imgDefault:
					setDefaultContent("video", video.id);
					break;
				case R.id.imgReport:
					UserInfo user = mDbMgr.getUser(video.ownerId);
					if (user != null && user._id != null)
						startFriendProfileActivity (user._id);
					break;
				case R.id.imgDelete:					
					deleteContent("video", video.id);
					break;
				default:
					break;
				}
			}
		}
	}

	public class NoteAdapter extends ArrayAdapter<Note> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Note> item = new ArrayList<Note>();

		public NoteAdapter(Activity activity, int layoutId, ArrayList<Note> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public int getCount() {
			return item.size();
		}
		
		public View getViewForSize(final int position, View convertView, ViewGroup parent) {
			NoteHolder holder = null;	

			Note noteItem = getItem(position);

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();				
				convertView = inflater.inflate(R.layout.item_concept_note2, null);
				holder = new NoteHolder(position);

				holder.txtDate = (TextView) convertView.findViewById(R.id.txtNoteDate);
				holder.txtNote = (TextView) convertView.findViewById(R.id.txtNote);

				holder.txtNoteUser = (TextView) convertView.findViewById(R.id.txtNoteUserName);
				holder.imgNoteUser = (HyIconView) convertView.findViewById(R.id.imgNoteUser);
				holder.imgNoteUser.imageView.setImageResource(R.drawable.empty_pic);

				holder.imgDefault = (ImageView) convertView.findViewById(R.id.imgDefault);
				holder.imgDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
				holder.imgReport = (ImageView) convertView.findViewById(R.id.imgReport);
			} 

			if (noteItem != null) {
				holder.txtDate.setText(Utility.getDateStringFromTimestamp(noteItem.create_date));
				holder.txtNote.setText(noteItem.text);

				if (!mPrefs.isLogIn()) {
					holder.imgDefault.setVisibility(View.GONE);
					holder.imgDelete.setVisibility(View.GONE);
					holder.imgReport.setVisibility(View.GONE);
				} else if (noteItem.ownerId.equalsIgnoreCase(mUserInfo._id)) {
					holder.imgDelete.setVisibility(View.VISIBLE);
					holder.imgReport.setVisibility(View.GONE);
				} else {
					holder.imgDelete.setVisibility(View.GONE);
					holder.imgReport.setVisibility(View.VISIBLE);
				}
			}

			return convertView;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			NoteHolder holder = null;	

			Note noteItem = getItem(position);

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();				
				convertView = inflater.inflate(R.layout.item_concept_note2, null);
				holder = new NoteHolder(position);

				holder.txtDate = (TextView) convertView.findViewById(R.id.txtNoteDate);
				holder.txtNote = (TextView) convertView.findViewById(R.id.txtNote);

				holder.txtNoteUser = (TextView) convertView.findViewById(R.id.txtNoteUserName);
				holder.imgNoteUser = (HyIconView) convertView.findViewById(R.id.imgNoteUser);
				holder.imgNoteUser.imageView.setImageResource(R.drawable.empty_pic);

				holder.imgDefault = (ImageView) convertView.findViewById(R.id.imgDefault);
				holder.imgDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
				holder.imgReport = (ImageView) convertView.findViewById(R.id.imgReport);

				holder.imgDefault.setOnClickListener(holder);
				holder.imgDelete.setOnClickListener(holder);
				holder.imgReport.setOnClickListener(holder);

				convertView.setTag(holder);
			} 
			else
				holder = (NoteHolder) convertView.getTag();

			if (noteItem != null) {
				holder.txtDate.setText(Utility.getDateStringFromTimestamp(noteItem.create_date));
				holder.txtNote.setText(noteItem.text);

				UserInfo noteOwner =  mDbMgr.getUser(noteItem.ownerId);
				if (noteOwner != null) {
					holder.txtNoteUser.setText(noteOwner.name);
					if (noteOwner.photo_url != null && !noteOwner.photo_url.isEmpty())
						ImageLoader.getInstance().displayImage(noteOwner.photo_url, holder.imgNoteUser, 0, 0);
					else
						holder.imgNoteUser.imageView.setImageResource(R.drawable.empty_pic);
				}

				if (!mPrefs.isLogIn()) {
					holder.imgDefault.setVisibility(View.GONE);
					holder.imgDelete.setVisibility(View.GONE);
					holder.imgReport.setVisibility(View.GONE);
				} else if (noteItem.ownerId.equalsIgnoreCase(mUserInfo._id)) {
					holder.imgDelete.setVisibility(View.VISIBLE);
					holder.imgReport.setVisibility(View.GONE);
				} else {
					holder.imgDelete.setVisibility(View.GONE);
					holder.imgReport.setVisibility(View.VISIBLE);
				}
			}

			return convertView;
		}

		public class NoteHolder implements OnClickListener{
			TextView txtNoteUser;
			HyIconView imgNoteUser;			
			TextView txtNote;
			TextView txtDate;

			ImageView imgDefault;
			ImageView imgDelete;
			ImageView imgReport;

			int itemIndex;

			public NoteHolder(int index) {
				super();
				this.itemIndex = index;
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int viewId = v.getId();

				Note note = mArrayNote.get(itemIndex);					
				if (note == null)
					return;

				switch (viewId)
				{
				case R.id.imgDefault:
					setDefaultContent("note", note.id);
					break;
				case R.id.imgReport:
					UserInfo user = mDbMgr.getUser(note.ownerId);
					if (user != null && user._id != null)
						startFriendProfileActivity (user._id);
					break;
				case R.id.imgDelete:					
					deleteContent("note", note.id);
					break;
				default:
					break;
				}
			}
		}
	}
}
