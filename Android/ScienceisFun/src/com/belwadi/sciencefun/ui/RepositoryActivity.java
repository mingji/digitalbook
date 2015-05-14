package com.belwadi.sciencefun.ui;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.Chapter;
import com.belwadi.sciencefun.entity.Concept;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Grade;
import com.belwadi.sciencefun.entity.Note;
import com.belwadi.sciencefun.entity.Reference;
import com.belwadi.sciencefun.entity.Syllabus;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Video;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.utils.Utility;
import com.belwadi.sciencefun.view.AlertDialogManager;
import com.belwadi.sciencefun.view.AutosizeTextView;
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

public class RepositoryActivity extends Activity implements OnClickListener, WebApiCallback {

	AppPreferences mPrefs;
	DatabaseManager mDbMgr;

	AlertDialogManager mAlertDiag = new AlertDialogManager();

	UserInfo mUserInfo;

	String mConceptId;	
	Concept mConcept;
	private String mSyllabusId, mGradeId;

	TextView mTxtUserName;
	ImageView mImgUserIcon;

	private Spinner mSpinChapter;
	private ArrayList<Chapter> mArrayChapter;
	private ArrayList<String> mArrayStrChapter;
	private ArrayAdapter<String> mAdapterChapter;	

	private Button mBtnVideo, mBtnNote, mBtnRef, mBtnBack;

	private ArrayList<Concept> mArrayConcept = null;

	private ConceptVideoAdapter mAdapterConceptVideo;
	private ListView mListviewVideo;

	private ConceptNoteAdapter mAdapterConceptNote;
	private ListView mListviewNote;

	private ConceptRefAdapter mAdapterConceptRef;
	private ListView mListviewRef;

	private RepoState mRepoState;

	public enum RepoState {
		VIDEO, NOTE, REF, NONE
	};

	WaitDialog mWaitDlg;
	
	int videoCnt, noteCnt, refCnt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);		

		setContentView(R.layout.activity_repository);

		Syllabus currentSyllabus = GlobalValue.getInstance().getCurrentSyllabus();
		mSyllabusId = currentSyllabus.id;
		
		Grade currentGrade = GlobalValue.getInstance().getCurrentGrade();
		mGradeId = currentGrade.id;

		mUserInfo = GlobalValue.getInstance().getCurrentUser();

		mPrefs = new AppPreferences(this);
		mDbMgr = new DatabaseManager(this);
		
		videoCnt = noteCnt = refCnt = 0;

		mConceptId = getIntent().getStringExtra("concept_id");
		if (mConceptId == null || mConceptId.isEmpty())
			finish();

		mConcept = mDbMgr.getConcept(mConceptId);

		String contentType = getIntent().getStringExtra("content");
		if (contentType == null) {
			mRepoState = RepoState.VIDEO;
		}
		else {
			if(contentType.equalsIgnoreCase(RepoState.VIDEO.toString()))
				mRepoState = RepoState.VIDEO;
			else if(contentType.equalsIgnoreCase(RepoState.NOTE.toString()))
				mRepoState = RepoState.NOTE;
			else if(contentType.equalsIgnoreCase(RepoState.REF.toString()))
				mRepoState = RepoState.REF;
			else
				mRepoState = RepoState.VIDEO;

		}

		initView();

		if (mPrefs.isLogIn())
			onUpdateUserInfo();

		updateChapter();

		updateSection(mRepoState);	

	}

	private void initView()
	{
		mTxtUserName = (TextView) findViewById(R.id.txtUserName);
		mImgUserIcon = (ImageView) findViewById(R.id.imgUserIcon);
		mImgUserIcon.setOnClickListener(this);

		mSpinChapter = (Spinner) findViewById(R.id.spinChapter);
		mSpinChapter.setPrompt("Select Chapter");

		mArrayStrChapter = new ArrayList<String>();
		mArrayChapter = new ArrayList<Chapter>();
		mAdapterChapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mArrayStrChapter);
		mAdapterChapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mSpinChapter.setAdapter(mAdapterChapter);
		mSpinChapter.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
				Chapter chapter = mArrayChapter.get(pos);
				updateConcept(chapter.id, mConceptId);				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		mBtnVideo = (Button) findViewById(R.id.btnVideo);
		mBtnNote = (Button) findViewById(R.id.btnNote);
		mBtnRef = (Button) findViewById(R.id.btnRef);
		mBtnBack = (Button) findViewById(R.id.btnBack);

		mBtnVideo.setOnClickListener(this);
		mBtnNote.setOnClickListener(this);
		mBtnRef.setOnClickListener(this);
		mBtnBack.setOnClickListener(this);

		mArrayConcept = new ArrayList<Concept>();

		mAdapterConceptNote = new ConceptNoteAdapter(this, R.layout.item_chapter_note, mArrayConcept);
		mListviewNote = (ListView) findViewById(R.id.lstChapterNote);		
		mListviewNote.setAdapter(mAdapterConceptNote);

		mAdapterConceptRef = new ConceptRefAdapter(this, R.layout.item_chapter_ref, mArrayConcept);
		mListviewRef = (ListView) findViewById(R.id.lstChapterRef);		
		mListviewRef.setAdapter(mAdapterConceptRef);

		mAdapterConceptVideo = new ConceptVideoAdapter(this, R.layout.item_chapter_ref, mArrayConcept);
		mListviewVideo = (ListView) findViewById(R.id.lstChapterVideo);		
		mListviewVideo.setAdapter(mAdapterConceptVideo);

	}

	private void onUpdateUserInfo()
	{
		UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();

		if (userInfo.name != null && !userInfo.name.isEmpty())
			mTxtUserName.setText(userInfo.name);

		if (userInfo.photo != null)
			mImgUserIcon.setImageBitmap(userInfo.photo);
		else
			mImgUserIcon.setImageResource(R.drawable.empty_pic);
	}

	private void updateChapter()
	{
		int chapterIndex = 0;
		
		mArrayChapter = mDbMgr.getChapterByGrade(mGradeId);
		mArrayStrChapter.clear();
		for (Chapter chapter : mArrayChapter)
		{
			mArrayStrChapter.add(chapter.title);
			if (chapter.id.equalsIgnoreCase(mConcept.chapterId))
				chapterIndex = mArrayChapter.indexOf(chapter);
		}
			mAdapterChapter.notifyDataSetChanged();
			
			
			mSpinChapter.setSelection(chapterIndex);
	}

	private void updateConcept(String chapterId, String conceptId)
	{
		int concept_position = 0;
		
		String currentChapterId = chapterId;
		if (currentChapterId == null)
			currentChapterId = mArrayChapter.get(mSpinChapter.getSelectedItemPosition()).id;

		mArrayConcept.clear();
		ArrayList<Concept> list = mDbMgr.getConceptByParam(mSyllabusId, mGradeId, currentChapterId);
		for (Concept item : list) {
			mArrayConcept.add(item);
			if (item.id.equalsIgnoreCase(conceptId))
				concept_position = list.indexOf(item);				
		}

		mAdapterConceptNote.notifyDataSetChanged();
		mAdapterConceptRef.notifyDataSetChanged();
		mAdapterConceptVideo.notifyDataSetChanged();
		
		mListviewVideo.smoothScrollToPosition(concept_position); 
		mListviewNote.smoothScrollToPosition(concept_position);
		mListviewRef.smoothScrollToPosition(concept_position);		
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		int id = view.getId();

		switch (id)
		{
		case R.id.btnVideo:
		case R.id.btnNote:
		case R.id.btnRef:
			onClickSectionButton (id);
			break;
		case R.id.btnBack:
			finishActivity("");
			break;
		case R.id.imgUserIcon:
			if (mPrefs.isLogIn())
				finishActivity("profile");
			else
				finishActivity("sign");
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

	private void onClickSectionButton (int id)
	{
		RepoState oldState, newState;

		oldState = mRepoState;
		newState = mRepoState;

		switch (id)
		{
		case R.id.btnVideo:
			newState = RepoState.VIDEO;
			break;
		case R.id.btnNote:
			newState = RepoState.NOTE;
			break;
		case R.id.btnRef:
			newState = RepoState.REF;
			break;
		default:

			break;
		}

		if (oldState == newState)
			return;

		mRepoState = newState;
		updateSection(mRepoState);

	}

	private void updateSection(RepoState state)
	{
		mBtnVideo.setBackgroundResource(R.color.repo_unselect);
		mBtnNote.setBackgroundResource(R.color.repo_unselect);
		mBtnRef.setBackgroundResource(R.color.repo_unselect);

		mBtnVideo.setTextColor(getResources().getColor(R.color.darkgray));
		mBtnNote.setTextColor(getResources().getColor(R.color.darkgray));
		mBtnRef.setTextColor(getResources().getColor(R.color.darkgray));

		switch (state)
		{
		case VIDEO:
			mBtnVideo.setBackgroundResource(R.color.repo_select);
			mBtnVideo.setTextColor(getResources().getColor(R.color.white));

			mListviewVideo.setVisibility(View.VISIBLE);
			mListviewNote.setVisibility(View.GONE);
			mListviewRef.setVisibility(View.GONE);
			break;
		case NOTE:
			mBtnNote.setBackgroundResource(R.color.repo_select);
			mBtnNote.setTextColor(getResources().getColor(R.color.white));

			mListviewVideo.setVisibility(View.GONE);
			mListviewNote.setVisibility(View.VISIBLE);
			mListviewRef.setVisibility(View.GONE);
			break;
		case REF:
			mBtnRef.setBackgroundResource(R.color.repo_select);
			mBtnRef.setTextColor(getResources().getColor(R.color.white));

			mListviewVideo.setVisibility(View.GONE);
			mListviewNote.setVisibility(View.GONE);
			mListviewRef.setVisibility(View.VISIBLE);
			break;
		default:
			break;
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
	
	private void startContentViewActivity(String contentId, String type)
	{		
		Intent intent = new Intent();
		intent.setClass(this, ContentViewActivity.class);
		intent.putExtra("type", type);
		intent.putExtra("content_id", contentId);
		startActivity(intent);
		overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}

	private void addVideoTask(final String token, final String conceptId, final String videoUrl, final boolean isPrivate, final String chapterId){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.AddVideo(token, conceptId, videoUrl, isPrivate);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onAddVideoResult(result, chapterId, conceptId);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(RepositoryActivity.this);
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onAddVideoResult(Object obj, String chapterId, String conceptId) {		

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
				getVideoListByConcept(videoCnt, chapterId, conceptId);
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
	
	private void addNoteTask(final String token, final String conceptId, final String note, final boolean isPrivate, final String chapterId){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.AddNote(token, conceptId, note, isPrivate);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onAddNoteResult(result, chapterId, conceptId);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(RepositoryActivity.this);
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onAddNoteResult(Object obj, String chapterId, String conceptId) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			return;
		}

		if (obj != null){
			AddNoteResult resultBean = (AddNoteResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				getNoteList(noteCnt, chapterId, conceptId);
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

	private void addRefTask(final String token, final String conceptId, final String refUrl, final boolean isPrivate, final String chapterId){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.AddReference(token, conceptId, refUrl, isPrivate);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onAddRefResult(result, chapterId, conceptId);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(RepositoryActivity.this);
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onAddRefResult(Object obj, String chapterId, String conceptId) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			return;
		}

		if (obj != null){
			AddRefResult resultBean = (AddRefResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				getReferenceList(refCnt, chapterId, conceptId);
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

	private void setDefaultContentTask(final String token,
			final String conceptId, final String contentType, final String contentId, final String chapterId){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SetDefaultContent(token, conceptId, contentType, contentId);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onSetDefaultResult(result, chapterId, conceptId, contentType);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(RepositoryActivity.this);
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onSetDefaultResult(Object obj, String chapterId, String conceptId, String contentType) {		

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
				if (contentType.equalsIgnoreCase("video"))
					getVideoListByConcept(videoCnt, chapterId, conceptId);
				else if (contentType.equalsIgnoreCase("reference"))
					getReferenceList(refCnt, chapterId, conceptId);
				else if (contentType.equalsIgnoreCase("note"))
					getNoteList(noteCnt, chapterId, conceptId);				
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

	private void setPrivateContentTask(final String token,
			final String conceptId, final String contentType, final String contentId, final String chapterId, final boolean isPrivate){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SetPrivateContent(token, conceptId, contentType, contentId, isPrivate);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onSetPrivateResult(result, chapterId, conceptId, contentType);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(RepositoryActivity.this);
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onSetPrivateResult(Object obj, String chapterId, String conceptId, String contentType) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			return;
		}

		if (obj != null){
			SetPrivateContentResult resultBean = (SetPrivateContentResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				if (contentType.equalsIgnoreCase("video"))
					getVideoListByConcept(videoCnt, chapterId, conceptId);
				else if (contentType.equalsIgnoreCase("reference"))
					getReferenceList(refCnt, chapterId, conceptId);
				else if (contentType.equalsIgnoreCase("note"))
					getNoteList(noteCnt, chapterId, conceptId);				
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

	private void deleteContentTask(final String token,
			final String contentType, final String contentId, final String chapterId, final String conceptId){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.DeleteContent(token, contentType, contentId);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onDelContentResult(result, chapterId, conceptId, contentType);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(RepositoryActivity.this);
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onDelContentResult(Object obj, String chapterId, String conceptId, String contentType) {		

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
				if (contentType.equalsIgnoreCase("video"))
					getVideoListByConcept(videoCnt, chapterId, conceptId);
				else if (contentType.equalsIgnoreCase("reference"))
					getReferenceList(refCnt, chapterId, conceptId);
				else if (contentType.equalsIgnoreCase("note"))
					getNoteList(noteCnt, chapterId, conceptId);				
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

	private void getVideoListByConcept(int start, String chapterId, String conceptId){

		GetVideoLstParam param = new GetVideoLstParam();
		param.checksum = mDbMgr.getVideoCheckSumByConcept(conceptId);
		param.start = start;
		param.count = Constants.COUNT_UNIT;
		
		param.chapterId = chapterId;
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
					getVideoListByConcept (videoCnt, param.chapterId, param.conceptId);
					return;
				} else {
					videoCnt = 0;
					updateConcept(param.chapterId, param.conceptId);
				}

			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}
		
		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void getNoteList(int start, String chapterId, String conceptId){

		GetNoteLstParam param = new GetNoteLstParam();
		param.checksum = mDbMgr.getNoteCheckSumByConcept(conceptId);
		param.start = start;
		param.count = Constants.COUNT_UNIT;
		
		param.chapterId = chapterId;
		param.conceptId = conceptId;

		WebApiInstance.getInstance().executeAPI(Type.GET_NOTES_CONCEPT, param, this);

	}

	private void getNoteListResult(GetNoteLstParam param, Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetNoteLstResult resultBean = (GetNoteLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (noteCnt == 0) {
					mDbMgr.delNoteByConcept(param.conceptId);
				}

				for (NoteResult item : resultBean.notes)
				{
					mDbMgr.addNoteItem(item);
					noteCnt++;
				}

				if (resultBean.eof == false) {
					getNoteList (noteCnt, param.chapterId, param.conceptId);
					return;
				} else {
					noteCnt = 0;
					updateConcept(param.chapterId, param.conceptId);
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}
		
		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void getReferenceList(int start, String chapterId, String conceptId){

		GetRefLstParam param = new GetRefLstParam();
		param.checksum = mDbMgr.getRefCheckSumByConcept(conceptId);
		param.start = start;
		param.count = Constants.COUNT_UNIT;
		
		param.chapterId = chapterId;
		param.conceptId = conceptId;

		WebApiInstance.getInstance().executeAPI(Type.GET_REFS_CONCEPT, param, this);
	}

	private void getReferenceListResult(GetRefLstParam param, Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(this, Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetRefLstResult resultBean = (GetRefLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {

				if (refCnt == 0) {
					mDbMgr.delReferenceByConcept(param.conceptId);
				}

				for (ReferenceResult item : resultBean.references)
				{
					mDbMgr.addReferenceItem(item);
					refCnt++;
				}

				if (resultBean.eof == false) {
					getReferenceList (refCnt, param.chapterId, param.conceptId);
					return;
				} else {
					refCnt = 0;
					updateConcept(param.chapterId, param.conceptId);
				}
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(this, resultBean.message);
			}
		}
		
		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	public class ConceptNoteAdapter extends ArrayAdapter<Concept> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Concept> item = new ArrayList<Concept>();

		// constructor
		public ConceptNoteAdapter(Activity activity, int layoutId, ArrayList<Concept> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final Concept conceptItem = getItem(position);

			Note defaultNote = null;
			ArrayList<Note> arrayNote = null;
			NoteAdapter adapterNote;

			arrayNote = new ArrayList<Note>();
			UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();
			if (userInfo.isValid)
				arrayNote = mDbMgr.getNoteByConceptUser(conceptItem.id, mUserInfo._id, null);
			else
				arrayNote = mDbMgr.getNoteByConceptUser(conceptItem.id, "admin", null);
			
			defaultNote = mDbMgr.getDefaultNote(mConceptId, mUserInfo._id);
			if (defaultNote == null)
				defaultNote = mDbMgr.getDefaultNote(mConceptId, "admin");
			
			adapterNote = new NoteAdapter(this.activity, R.layout.item_concept_note, arrayNote, defaultNote);

			ConceptNoteHolder holder = null;

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_chapter_note, null);
				holder = new ConceptNoteHolder();
				holder.txtConceptName = (TextView) convertView.findViewById(R.id.txtConceptName);
				holder.lstConceptNote = (ListView) convertView.findViewById(R.id.lstConceptNote);
				final EditText txtNote = (EditText) convertView.findViewById(R.id.txtAddNote);
				//				holder.txtAddRef = (EditText) convertView.findViewById(R.id.txtAddRef);
				holder.imgAddNote = (ImageView) convertView.findViewById(R.id.imgAddNote);
				holder.imgAddNote.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						if (!mPrefs.isLogIn()) {
							alertSignInDiag("Guest user can't add the content. Do you want to sign in?");
							return;
						}

						String note = txtNote.getText().toString();
						txtNote.setText("");
						if (note == null || note.isEmpty())
							return;
						String conceptId = new String(conceptItem.id);
						String chapterId = new String(conceptItem.chapterId);
						Log.d("debug", mPrefs.getToken() + ", " + conceptId + ", " + note);
						addNoteTask(mPrefs.getToken(), conceptId, note, !conceptItem.noteFlag, chapterId);
					}
				});
				final ImageView imgAddLock = (ImageView) convertView.findViewById(R.id.imgAddLock);
				if (conceptItem.noteFlag == true)
					imgAddLock.setImageResource(R.drawable.unlock);
				else
					imgAddLock.setImageResource(R.drawable.lock);
				imgAddLock.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (conceptItem.noteFlag == true) {
							conceptItem.noteFlag = false;
							imgAddLock.setImageResource(R.drawable.lock);
						} else {
							conceptItem.noteFlag = true;
							imgAddLock.setImageResource(R.drawable.unlock);
						}
					}
				});
//				if (!mPrefs.isLogIn()) {
//					holder.imgAddNote.setVisibility(View.GONE);
//					txtNote.setVisibility(View.GONE);
//				}

				convertView.setTag(holder);
			} 
			else
				holder = (ConceptNoteHolder) convertView.getTag();

			holder.txtConceptName.setText(conceptItem.title);
			holder.lstConceptNote.setAdapter(adapterNote);
			if (arrayNote.size() < 1)
				holder.lstConceptNote.setVisibility(View.GONE);
			else
				holder.lstConceptNote.setVisibility(View.VISIBLE);
			holder.lstConceptNote.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					mListviewNote.requestDisallowInterceptTouchEvent(true);
					return false;
				}
			});

			return convertView;
		}

		public class ConceptNoteHolder {
			TextView txtConceptName;
			ListView lstConceptNote;
			EditText txtAddNote;
			ImageView imgAddNote;
//			ImageView imgAddLock;
		}
	}

	public class NoteAdapter extends ArrayAdapter<Note> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Note> item = new ArrayList<Note>();
		Note defaultNote;

		// constuctor
		public NoteAdapter(Activity activity, int layoutId, ArrayList<Note> items, Note defaultNote) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
			this.defaultNote = defaultNote;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final Note noteItem = getItem(position);		

			NoteHolder holder = null;

			// inflate the view
			//			if (convertView == null) {
			LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
			convertView = inflater.inflate(R.layout.item_concept_note, null);

			holder = new NoteHolder();
			holder.txtNoteDate = (TextView) convertView.findViewById(R.id.txtNoteDate);
			holder.txtNote = (AutosizeTextView) convertView.findViewById(R.id.txtNote);
			holder.txtNoteUserName = (TextView) convertView.findViewById(R.id.txtNoteUserName);
			holder.imgNoteUser = (HyIconView) convertView.findViewById(R.id.imgNoteUser);
			holder.imgNoteUser.imageView.setImageResource(R.drawable.empty_pic);
			holder.iconDefault = (ImageView) convertView.findViewById(R.id.iconDefault);
			holder.iconDefault.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					if (!mPrefs.isLogIn()) {
						alertSignInDiag("Guest user can't change the content. Do you want to sign in?");
						return;
					}

					if (noteItem.defaulted_user.contains(mUserInfo._id))
						return;

					String contentId = new String(noteItem.id);
					String conceptId = new String(noteItem.conceptId);
					Concept concept = mDbMgr.getConcept(conceptId);
					String chapterId = new String(concept.chapterId);
					String contentType = "note";

					setDefaultContentTask(mPrefs.getToken(),
							conceptId, contentType, contentId, chapterId);
				}
			});

			holder.iconLock = (ImageView) convertView.findViewById(R.id.imgLock);
			holder.iconLock.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					if (!mPrefs.isLogIn()) {
						alertSignInDiag("Guest user can't change the content. Do you want to sign in?");
						return;
					}

					boolean isPrivate;
					if (noteItem.privated_user.contains(mUserInfo._id))
						isPrivate = false;
					else
						isPrivate = true;


					String contentId = new String(noteItem.id);
					String conceptId = new String(noteItem.conceptId);
					Concept concept = mDbMgr.getConcept(conceptId);
					String chapterId = new String(concept.chapterId);
					String contentType = "note";						

					setPrivateContentTask(mPrefs.getToken(),
							conceptId, contentType, contentId, chapterId, isPrivate);
				}
			});

			holder.iconDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
			holder.iconDelete.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					if (!mPrefs.isLogIn()) {
						alertSignInDiag("Guest user can't change the content. Do you want to sign in?");
						return;
					}

					String contentId = new String(noteItem.id);
					String conceptId = new String(noteItem.conceptId);
					Concept concept = mDbMgr.getConcept(conceptId);
					String chapterId = new String(concept.chapterId);
					String contentType = "note";						

					deleteContentTask(mPrefs.getToken(), contentType, contentId, chapterId, conceptId);
				}
			});

			convertView.setTag(holder);
			//			}
			//			else
			//				holder = (NoteHolder) convertView.getTag();

			if (noteItem != null) {
				holder.txtNoteDate.setText(Utility.getDateStringFromTimestamp(noteItem.create_date));
				holder.txtNote.setText(noteItem.text);

				UserInfo noteOwner =  mDbMgr.getUser(noteItem.ownerId);
				if (noteOwner != null) {
					holder.txtNoteUserName.setText(noteOwner.name);
					if (noteOwner.photo_url != null && !noteOwner.photo_url.isEmpty())
						ImageLoader.getInstance().displayImage(noteOwner.photo_url, holder.imgNoteUser, 0, 0);
					else
						holder.imgNoteUser.imageView.setImageResource(R.drawable.empty_pic);
				}

				if (noteItem.defaulted_user.contains(mUserInfo._id))
					holder.iconDefault.setImageResource(R.drawable.icon_star);
				else if (defaultNote != null && noteItem.id.equalsIgnoreCase(defaultNote.id))
					holder.iconDefault.setImageResource(R.drawable.icon_star);
				else
					holder.iconDefault.setImageResource(R.drawable.icon_grey_star);

				if (noteItem.privated_user.contains(mUserInfo._id))
					holder.iconLock.setImageResource(R.drawable.lock);
				else
					holder.iconLock.setImageResource(R.drawable.unlock);
				
				if (mPrefs.isLogIn()
						&& noteItem.ownerId.contains(mUserInfo._id))
					holder.iconLock.setVisibility(View.VISIBLE);
				else
					holder.iconLock.setVisibility(View.GONE);

				if (mPrefs.isLogIn()
						&& noteItem.shared_user.contains(mUserInfo._id)
						&& !noteItem.defaulted_user.contains(mUserInfo._id))
					holder.iconDelete.setVisibility(View.VISIBLE);
				else
					holder.iconDelete.setVisibility(View.GONE);
			}
			return convertView;
		}

		public class NoteHolder {
			TextView txtNoteDate, txtNote, txtNoteUserName;
			HyIconView imgNoteUser;
			ImageView iconDefault;
			ImageView iconLock;
			ImageView iconDelete;
		}
	}

	public class ConceptRefAdapter extends ArrayAdapter<Concept> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Concept> item = new ArrayList<Concept>();

		// constuctor
		public ConceptRefAdapter(Activity activity, int layoutId, ArrayList<Concept> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final Concept conceptItem = getItem(position);

			ArrayList<Reference> arrayRef = null;
			ReferenceAdapter adapterRef;
			Reference defaultRef;

			arrayRef = new ArrayList<Reference>();
			UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();
			arrayRef.clear();
			if (userInfo.isValid)
				arrayRef = mDbMgr.getReferenceByConceptUser(conceptItem.id, mUserInfo._id);
			else
				arrayRef = mDbMgr.getReferenceByConceptUser(conceptItem.id, "admin");
			
			defaultRef = mDbMgr.getDefaultReference(conceptItem.id, mUserInfo._id);
			if (defaultRef == null)
				defaultRef = mDbMgr.getDefaultReference(conceptItem.id, "admin");

			adapterRef = new ReferenceAdapter(this.activity, R.layout.item_concept_ref, arrayRef, defaultRef);

			ConceptRefHolder holder = null;

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_chapter_ref, null);
				holder = new ConceptRefHolder();
				holder.txtConceptName = (TextView) convertView.findViewById(R.id.txtConceptName);
				holder.lstConceptRef = (ListView) convertView.findViewById(R.id.lstConceptRef);
				final EditText txtRef = (EditText) convertView.findViewById(R.id.txtAddRef);
				//				holder.txtAddRef = (EditText) convertView.findViewById(R.id.txtAddRef);
				holder.imgAddRef = (ImageView) convertView.findViewById(R.id.imgAddRef);
				holder.imgAddRef.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						if (!mPrefs.isLogIn()) {
							alertSignInDiag("Guest user can't add the content. Do you want to sign in?");
							return;
						}

						String refUrl = txtRef.getText().toString();
						txtRef.setText("");
						if (refUrl == null || refUrl.isEmpty())
							return;
						String conceptId = new String(conceptItem.id);
						String chapterId = new String(conceptItem.chapterId);
						Log.d("debug", mPrefs.getToken() + ", " + conceptId + ", " + refUrl);
						addRefTask(mPrefs.getToken(), conceptId, refUrl, !conceptItem.refFlag, chapterId);
					}
				});
				final ImageView imgAddLock = (ImageView) convertView.findViewById(R.id.imgAddLock);
				if (conceptItem.refFlag == true)
					imgAddLock.setImageResource(R.drawable.unlock);
				else
					imgAddLock.setImageResource(R.drawable.lock);
				imgAddLock.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (conceptItem.refFlag == true) {
							conceptItem.refFlag = false;
							imgAddLock.setImageResource(R.drawable.lock);
						} else {
							conceptItem.refFlag = true;
							imgAddLock.setImageResource(R.drawable.unlock);
						}
					}
				});
				
//				if (!mPrefs.isLogIn()) {
//					holder.imgAddRef.setVisibility(View.GONE);
//					txtRef.setVisibility(View.GONE);
//				}

				convertView.setTag(holder);
			} 
			else
				holder = (ConceptRefHolder) convertView.getTag();

			holder.txtConceptName.setText(conceptItem.title);
			holder.lstConceptRef.setAdapter(adapterRef);
			if (arrayRef.size() < 1)
				holder.lstConceptRef.setVisibility(View.GONE);
			else
				holder.lstConceptRef.setVisibility(View.VISIBLE);
			holder.lstConceptRef.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					mListviewRef.requestDisallowInterceptTouchEvent(true);
					return false;
				}
			});

			return convertView;
		}

		public class ConceptRefHolder {
			TextView txtConceptName;
			ListView lstConceptRef;
			EditText txtAddRef;
			ImageView imgAddRef;
		}
	}

	public class ReferenceAdapter extends ArrayAdapter<Reference> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Reference> item = new ArrayList<Reference>();
		Reference defaultRef;

		// constuctor
		public ReferenceAdapter(Activity activity, int layoutId, ArrayList<Reference> items, Reference defaultRef) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
			this.defaultRef = defaultRef;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final Reference refItem = getItem(position);		

			ReferenceHolder holder = null;

			// inflate the view
			//			if (convertView == null) {
			LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
			convertView = inflater.inflate(R.layout.item_concept_ref, null);

			holder = new ReferenceHolder();
			holder.txtRefTitle = (TextView) convertView.findViewById(R.id.txtRefName);
			holder.txtRefUrl = (TextView) convertView.findViewById(R.id.txtRefUrl);
			holder.txtRefDscp = (TextView) convertView.findViewById(R.id.txtRefDescription);		
			holder.iconRef = (HyIconView) convertView.findViewById(R.id.imgRefIcon);
			holder.iconRef.flagShowProg = false;
			holder.iconRef.imageView.setImageResource(R.drawable.icon_ref);
			holder.iconDefault = (ImageView) convertView.findViewById(R.id.iconDefault);
			holder.iconDefault.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					if (!mPrefs.isLogIn()) {
						alertSignInDiag("Guest user can't change the content. Do you want to sign in?");
						return;
					}

					String contentId = new String(refItem.id);
					String conceptId = new String(refItem.conceptId);
					Concept concept = mDbMgr.getConcept(conceptId);
					String chapterId = new String(concept.chapterId);
					String contentType = "reference";

					setDefaultContentTask(mPrefs.getToken(),
							conceptId, contentType, contentId, chapterId);
				}
			});
			holder.iconLock = (ImageView) convertView.findViewById(R.id.imgLock);
			holder.iconLock.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					if (!mPrefs.isLogIn()) {
						alertSignInDiag("Guest user can't change the content. Do you want to sign in?");
						return;
					}

					boolean isPrivate;
					if (refItem.privated_user.contains(mUserInfo._id))
						isPrivate = false;
					else
						isPrivate = true;

					String contentId = new String(refItem.id);
					String conceptId = new String(refItem.conceptId);
					Concept concept = mDbMgr.getConcept(conceptId);
					String chapterId = new String(concept.chapterId);
					String contentType = "reference";						

					setPrivateContentTask(mPrefs.getToken(),
							conceptId, contentType, contentId, chapterId, isPrivate);
				}
			});

			holder.iconDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
			holder.iconDelete.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					if (!mPrefs.isLogIn()) {
						alertSignInDiag("Guest user can't change the content. Do you want to sign in?");
						return;						
					}
					
					String contentId = new String(refItem.id);
					String conceptId = new String(refItem.conceptId);
					Concept concept = mDbMgr.getConcept(conceptId);
					String chapterId = new String(concept.chapterId);
					String contentType = "reference";						

					deleteContentTask(mPrefs.getToken(), contentType, contentId, chapterId, conceptId);
				}
			});
			holder.index = position;

			//				convertView.setTag(holder);
			//			}
			//			else
			//				holder = (ReferenceHolder) convertView.getTag();

			if (refItem != null) {
				holder.txtRefTitle.setText(refItem.title);
				holder.txtRefUrl.setText(refItem.url);
				holder.txtRefDscp.setText(refItem.description);

				if (refItem.image != null && !refItem.image.isEmpty())
					ImageLoader.getInstance().displayImage(refItem.image, holder.iconRef, 0, 0);
				else
					holder.iconRef.imageView.setImageResource(R.drawable.icon_ref);

				if (refItem.defaulted_user.contains(mUserInfo._id))
					holder.iconDefault.setImageResource(R.drawable.icon_star);
				else if (defaultRef != null && refItem.id.equalsIgnoreCase(defaultRef.id))
					holder.iconDefault.setImageResource(R.drawable.icon_star);
				else
					holder.iconDefault.setImageResource(R.drawable.icon_grey_star);

				if (refItem.privated_user.contains(mUserInfo._id))
					holder.iconLock.setImageResource(R.drawable.lock);
				else
					holder.iconLock.setImageResource(R.drawable.unlock);
				
				if (mPrefs.isLogIn()
						&& refItem.ownerId.contains(mUserInfo._id))
					holder.iconLock.setVisibility(View.VISIBLE);
				else
					holder.iconLock.setVisibility(View.GONE);

				if (mPrefs.isLogIn()
						&& refItem.shared_user.contains(mUserInfo._id)
						&& !refItem.defaulted_user.contains(mUserInfo._id))
					holder.iconDelete.setVisibility(View.VISIBLE);
				else
					holder.iconDelete.setVisibility(View.GONE);
			}

			return convertView;
		}

		public class ReferenceHolder {
			TextView txtRefTitle, txtRefUrl, txtRefDscp;
			HyIconView iconRef;
			ImageView iconDefault;
			ImageView iconLock;
			ImageView iconDelete;
			int index;
		}
	}

	public class ConceptVideoAdapter extends ArrayAdapter<Concept> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Concept> item = new ArrayList<Concept>();

		// constructor
		public ConceptVideoAdapter(Activity activity, int layoutId, ArrayList<Concept> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final Concept conceptItem = getItem(position);

			ArrayList<Video> arrayVideo = null;
			VideoAdapter adapterVideo;
			Video defaultVideo = null;

			arrayVideo = new ArrayList<Video>();
			UserInfo userInfo = GlobalValue.getInstance().getCurrentUser();
			arrayVideo.clear();
			if (userInfo.isValid)
				arrayVideo = mDbMgr.getVideoByConceptUser(conceptItem.id, mUserInfo._id, null);
			else
				arrayVideo = mDbMgr.getVideoByConceptUser(conceptItem.id, "admin", null);

			Video addVideo = new Video();
			addVideo.id = "add_video";
			addVideo.conceptId = conceptItem.id;
			arrayVideo.add(addVideo);

			defaultVideo = mDbMgr.getDefaultVideo(conceptItem.id, mUserInfo._id);
			if (defaultVideo == null)
				defaultVideo = mDbMgr.getDefaultVideo(conceptItem.id, "admin");
			
			adapterVideo = new VideoAdapter(this.activity, R.layout.item_concept_video, arrayVideo, defaultVideo);

			ConceptRefHolder holder = null;

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_chapter_video, null);
				holder = new ConceptRefHolder();
				holder.txtConceptName = (TextView) convertView.findViewById(R.id.txtConceptName);
				holder.gridConcepVideo = (GridView) convertView.findViewById(R.id.gridConceptVideo);
//				holder.gridConcepVideo.setOnItemClickListener(new OnItemClickListener() {
//					@Override
//					public void onItemClick(AdapterView<?> arg0, View arg1, int index,
//							long arg3) {
//						showGetNumberDiag(conceptItem.id, conceptItem.chapterId);
//					}
//				});

				convertView.setTag(holder);
			} 
			else
				holder = (ConceptRefHolder) convertView.getTag();

			holder.txtConceptName.setText(conceptItem.title);
			holder.gridConcepVideo.setAdapter(adapterVideo);

			return convertView;
		}

		public class ConceptRefHolder {
			TextView txtConceptName;
			GridView gridConcepVideo;
		}
	}

	public class VideoAdapter extends ArrayAdapter<Video> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Video> item = new ArrayList<Video>();
		Video defaultVideo;

		// constructor
		public VideoAdapter(Activity activity, int layoutId, ArrayList<Video> items, Video defaultVideo) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
			this.defaultVideo = defaultVideo;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final Video videoItem = getItem(position);		

			VideoHolder holder = null;

			// inflate the view
			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_concept_video, null);

				holder = new VideoHolder();
				holder.imgThumb = (HyIconView) convertView.findViewById(R.id.imgVideoThumb);
				holder.imgThumb.failedBp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_youtube);
				holder.imgThumb.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						if (videoItem.id.equalsIgnoreCase("add_video")) {
							
							if (!mPrefs.isLogIn()) {
								alertSignInDiag("Guest user can't add the content. Do you want to sign in?");
								return;
							}
							
							String conceptId = new String(videoItem.conceptId);
							Concept concept = mDbMgr.getConcept(conceptId);
							String chapterId = new String(concept.chapterId);
							
							addVideoDiag(conceptId, chapterId);
						} else {
							startContentViewActivity(videoItem.id, "video");
						}
					}
				});
				
				holder.iconDefault = (ImageView) convertView.findViewById(R.id.iconDefault);
				holder.iconDefault.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						if (!mPrefs.isLogIn()) {
							alertSignInDiag("Guest user can't change the content. Do you want to sign in?");
							return;
						}

						if (videoItem.defaulted_user.contains(mUserInfo._id))
							return;

						String contentId = new String(videoItem.id);
						String conceptId = new String(videoItem.conceptId);
						Concept concept = mDbMgr.getConcept(conceptId);
						String chapterId = new String(concept.chapterId);
						String contentType = "video";

						setDefaultContentTask(mPrefs.getToken(),
								conceptId, contentType, contentId, chapterId);
					}
				});

				holder.iconDelete = (ImageView) convertView.findViewById(R.id.imgDelete);
				holder.iconDelete.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {

						if (!mPrefs.isLogIn()) {
							alertSignInDiag("Guest user can't change the content. Do you want to sign in?");
							return;
						}
						
						String contentId = new String(videoItem.id);
						String conceptId = new String(videoItem.conceptId);
						Concept concept = mDbMgr.getConcept(conceptId);
						String chapterId = new String(concept.chapterId);
						String contentType = "video";						

						deleteContentTask(mPrefs.getToken(), contentType, contentId, chapterId, conceptId);
					}
				});
				holder.index = position;

				convertView.setTag(holder);
			}
			else
				holder = (VideoHolder) convertView.getTag();

			if (videoItem != null) {
				if (videoItem.id.equals("add_video")) {
					holder.imgThumb.imageView.setImageResource(R.drawable.img_add_video);
					holder.iconDefault.setVisibility(View.GONE);					
				} else {
					ImageLoader.getInstance().displayImage(Constants.HTTP_YOUTUBE_IMG + videoItem.url +"/default.jpg", holder.imgThumb, 0, 0);

					if (videoItem.defaulted_user.contains(mUserInfo._id))
						holder.iconDefault.setImageResource(R.drawable.icon_star);
					else if (defaultVideo != null && videoItem.id.equalsIgnoreCase(defaultVideo.id))
						holder.iconDefault.setImageResource(R.drawable.icon_star);
					else
						holder.iconDefault.setImageResource(R.drawable.icon_grey_star);

					if (mPrefs.isLogIn()
							&& videoItem.shared_user.contains(mUserInfo._id)
							&& !videoItem.defaulted_user.contains(mUserInfo._id))
						holder.iconDelete.setVisibility(View.VISIBLE);
					else
						holder.iconDelete.setVisibility(View.GONE);
				}
			}

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
			Video videoItem = getItem(position);

			if (videoItem.id.equals("add_video"))
				return true;
			else
				return false;
		}

		public class VideoHolder {
			HyIconView imgThumb;
			ImageView iconDefault;
			ImageView iconDelete;
			int index;
		}
	}

	private void addVideoDiag(final String conceptId, final String chapterId)
	{
		LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.view_input_video, null);
		final EditText txtUrl = (EditText)layout.findViewById(R.id.txtUrl);

		txtUrl.setText("");

		AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(this)
		.setTitle("YouTube URL")
		.setIcon(android.R.drawable.stat_sys_warning)
		.setMessage("Please input YouTube URL")
		.setView(layout)
		.setCancelable(false)
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String url = txtUrl.getText().toString();
				if (url == null || url.isEmpty())
					return;

				addVideoTask(mPrefs.getToken(), conceptId, url, false, chapterId);
			}
		});

		AlertDialog alertDlg = alertDlgBuilder.create();
		alertDlg.setCanceledOnTouchOutside(false);
		alertDlg.show();
	}
	
	private void alertSignInDiag(String msg)
	{
		AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(this)
		.setTitle("Alert")
		.setIcon(android.R.drawable.stat_sys_warning)
		.setMessage("" + msg)
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
			getReferenceListResult((GetRefLstParam)parameter, result);			
			break;
		case GET_NOTES_CONCEPT:
			getNoteListResult((GetNoteLstParam)parameter, result);			
			break;
		default:
			break;
		}
	}

}
