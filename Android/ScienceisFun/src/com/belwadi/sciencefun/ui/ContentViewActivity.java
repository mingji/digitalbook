package com.belwadi.sciencefun.ui;


import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.*;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.utils.Utility;
import com.belwadi.sciencefun.view.AutosizeTextView;
import com.belwadi.sciencefun.view.HyIconView;
import com.belwadi.sciencefun.webservice.Constants;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

public class ContentViewActivity extends YouTubeFailureRecoveryActivity implements OnClickListener{

	DatabaseManager mDbMgr;
	AppPreferences mPrefs;

	Video mVideo;
	Reference mRef;
	Note mNote;

	Button mBtnClose;

	YouTubePlayerFragment mYouTubeFragment;

	TextView mTxtRefTitle, mTxtRefUrl, mTxtRefDscp, mTxtNoteDate, mTxtNoteUserName;
	AutosizeTextView mTxtNote;	

	HyIconView mIconRef, mImgNoteUser;

	RelativeLayout mLayVideo, mLayRef, mLayNote;

	ContentType mContentType;

	private enum ContentType {
		VIDEO, NOTE, REFERENCE, NONE
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_content_view);

		mDbMgr = new DatabaseManager(this);
		mPrefs = new AppPreferences(this);

		String type = getIntent().getStringExtra("type");
		if (type == null) {
			finish();
			return; 
		} else if (type.equalsIgnoreCase("video"))
			mContentType = ContentType.VIDEO;
		else if (type.equalsIgnoreCase("note"))
			mContentType = ContentType.NOTE;
		else if (type.equalsIgnoreCase("reference"))
			mContentType = ContentType.REFERENCE;

		String contentId = getIntent().getStringExtra("content_id");

		initView();

		showContent(contentId);
	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
			boolean wasRestored) {
		if (!wasRestored) {
			if (mVideo != null)
				player.cueVideo(mVideo.url);
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
	}

	private void initView()
	{
		mYouTubeFragment =
				(YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
		mYouTubeFragment.initialize(Constants.DEVELOPER_KEY, this);

		mLayVideo = (RelativeLayout) findViewById(R.id.layVideo);
		mLayRef = (RelativeLayout) findViewById(R.id.layReference);
		mLayNote = (RelativeLayout) findViewById(R.id.layNote);

		mTxtRefTitle = (TextView) findViewById(R.id.txtRefName);
		mTxtRefUrl = (TextView) findViewById(R.id.txtRefUrl);
		mTxtRefDscp = (TextView) findViewById(R.id.txtRefDescription);		
		mIconRef = (HyIconView) findViewById(R.id.imgRefIcon);
		mIconRef.imageView.setImageResource(R.drawable.icon_ref);
		mIconRef.flagShowProg = false;

		mTxtNoteDate = (TextView) findViewById(R.id.txtNoteDate);
		mTxtNote = (AutosizeTextView) findViewById(R.id.txtNote);
		mTxtNoteUserName = (TextView) findViewById(R.id.txtNoteUserName);
		mImgNoteUser = (HyIconView) findViewById(R.id.imgNoteUser);
		mImgNoteUser.imageView.setImageResource(R.drawable.empty_pic);

		mBtnClose = (Button) findViewById(R.id.btnClose);
		mBtnClose.setOnClickListener(this);

	}

	private void showContent(String contentId)
	{
		mLayVideo.setVisibility(View.GONE);
		mLayNote.setVisibility(View.GONE);
		mLayRef.setVisibility(View.GONE);

		switch (mContentType)
		{
		case VIDEO:
			mVideo = mDbMgr.getVideo(contentId);
			mLayVideo.setVisibility(View.VISIBLE);
			break;
			
		case NOTE:
			mNote = mDbMgr.getNote(contentId);

			if (mNote != null) {
				mLayNote.setVisibility(View.VISIBLE);

				mTxtNoteDate.setText(Utility.getDateStringFromTimestamp(mNote.create_date));
				mTxtNote.setText(mNote.text);

				UserInfo noteOwner =  mDbMgr.getUser(mNote.ownerId);
				if (noteOwner != null) {
					mTxtNoteUserName.setText(noteOwner.name);
					if (noteOwner.photo_url != null && !noteOwner.photo_url.isEmpty())
						ImageLoader.getInstance().displayImage(noteOwner.photo_url, mImgNoteUser, 0, 0);
					else
						mImgNoteUser.imageView.setImageResource(R.drawable.empty_pic);
				}

			}

			mLayNote.setVisibility(View.VISIBLE);
			break;
			
		case REFERENCE:
			mRef = mDbMgr.getReference(contentId);

			if (mRef != null) {
				mLayRef.setVisibility(View.VISIBLE);

				mTxtRefTitle.setText(mRef.title);
				mTxtRefUrl.setText(mRef.url);
				mTxtRefDscp.setText(mRef.description);

				if (mRef.image != null && !mRef.image.isEmpty())
					ImageLoader.getInstance().displayImage(mRef.image, mIconRef, 0, 0);
				else
					mIconRef.imageView.setImageResource(R.drawable.icon_ref);
			}

			mLayRef.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();


		switch (id)
		{
		case R.id.btnClose:
			finish();
			overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
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
			finish();
			overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);			
			bool = true;
		}
		return bool;
	}
}
