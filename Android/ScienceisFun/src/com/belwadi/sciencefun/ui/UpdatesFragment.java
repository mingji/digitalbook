package com.belwadi.sciencefun.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Note;
import com.belwadi.sciencefun.entity.Reference;
import com.belwadi.sciencefun.entity.Update;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Video;
import com.belwadi.sciencefun.entity.Beans.*;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.utils.Utility;
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class UpdatesFragment extends Fragment implements OnClickListener, WebApiCallback
{
	private final int FRIEND_PROFILE_ACTIVITY = 0;
	
	AppPreferences mPrefs;
	DatabaseManager mDbMgr;

	UserInfo mCurrentUser;

	private static String TYPE_JOIN = "joined";
	private static String TYPE_ADD_NOTE = "added note";
	private static String TYPE_ADD_REF = "added reference";
	private static String TYPE_ADD_VIDEO = "added video";
	private static String TYPE_SHARE_NOTE = "shared note";
	private static String TYPE_SHARE_REF = "shared reference";
	private static String TYPE_SHARE_VIDEO = "shared video";

	private ListView mListviewUpdate;
	private ArrayList<Update> mUpdateArray = null;
	private UpdateAdapter mAdapterUpdate;	

	private Button mBtnSort;
	private boolean flagSort = true; // true : sort by new, false : sort by old

	WaitDialog mWaitDlg;
	
	int updateCnt, videoCnt, noteCnt, refCnt;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_updates, null);

		mPrefs = new AppPreferences(getActivity());
		mDbMgr = new DatabaseManager(getActivity());

		mCurrentUser = GlobalValue.getInstance().getCurrentUser();

		mUpdateArray = new ArrayList<Update>();

		updateCnt = videoCnt = noteCnt = refCnt = 0;
		
		initView(view);	

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		
//		updateCnt = 0;
//		getUpdateList(updateCnt);
	}

	private void initView(View view)
	{
		mAdapterUpdate = new UpdateAdapter(getActivity(), R.layout.item_update, mUpdateArray);
		mListviewUpdate = (ListView) view.findViewById(R.id.lstUpdates);
		mListviewUpdate.setAdapter(mAdapterUpdate);

		mBtnSort = (Button) view.findViewById(R.id.btnSort);
		mBtnSort.setOnClickListener(this);
	}

	private void refreshUpdates()
	{
		mUpdateArray.clear();

		ArrayList<Update> tempList = mDbMgr.getUpdate();
		for (Update item : tempList)
		{
			UserInfo owner = mDbMgr.getUser(item.owner);
			if (owner == null)
				continue;
			
			mUpdateArray.add(item);
		}

		if (flagSort == true) {
			Collections.sort(mUpdateArray, updateComparator);
		} else {
			Collections.sort(mUpdateArray, updateComparator);
			Collections.reverse(mUpdateArray);
		}

		mAdapterUpdate.notifyDataSetChanged();
	}

	private void onClickSort()
	{
		if (flagSort == true) {
			Collections.sort(mUpdateArray, updateComparator);
		} else {
			Collections.sort(mUpdateArray, updateComparator);
			Collections.reverse(mUpdateArray);
		}

		mAdapterUpdate.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		int id = v.getId();

		switch (id)
		{
		case R.id.btnSort:
			if (flagSort == true) {
				flagSort = false;
				mBtnSort.setBackgroundResource(R.drawable.icon_up);
			} else {
				flagSort = true;
				mBtnSort.setBackgroundResource(R.drawable.icon_down);
			}

			onClickSort();
			break;
		default :
			break;

		}
	}

	private void startFriendProfileActivity(String userId)
	{		
		Intent intent = new Intent();
		intent.setClass(getActivity(), FriendProfileActivity.class);
		intent.putExtra("user_id", userId);
		startActivityForResult(intent, FRIEND_PROFILE_ACTIVITY);
		getActivity().overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}

	private void startContentViewActivity(String contentId, String type)
	{		
		Intent intent = new Intent();
		intent.setClass(getActivity(), ContentViewActivity.class);
		intent.putExtra("type", type);
		intent.putExtra("content_id", contentId);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode != Activity.RESULT_OK)
			return;

		if (requestCode == FRIEND_PROFILE_ACTIVITY) {
			String goPage = data.getStringExtra("go_page");

			if (goPage != null) {
				MainActivity parentActivity = (MainActivity)getActivity();
				
				if (goPage.equalsIgnoreCase("sign"))
					parentActivity.setState(MainActivity.State.SIGNINOUT);
				else if (goPage.equalsIgnoreCase("profile"))
					parentActivity.setState(MainActivity.State.PROFILE);
			}
		}
	}

	private String getViewText (String updateType)
	{
		if (updateType == null)
			return "View Profile";		
		else if (updateType.equalsIgnoreCase(TYPE_JOIN))
			return "View Profile";
		else if (updateType.equalsIgnoreCase(TYPE_ADD_NOTE))
			return "Read Note";
		else if (updateType.equalsIgnoreCase(TYPE_ADD_REF))
			return "Read Reference";
		else if (updateType.equalsIgnoreCase(TYPE_ADD_VIDEO))
			return "Watch Video";
		else if (updateType.equalsIgnoreCase(TYPE_SHARE_NOTE))
			return "Read Note";
		else if (updateType.equalsIgnoreCase(TYPE_SHARE_REF))
			return "Read Reference";
		else if (updateType.equalsIgnoreCase(TYPE_SHARE_VIDEO))
			return "Watch Video";
		else 
			return "View Profile";
	}

	private boolean isAddedContent (String updateType, String contentId)
	{
		if (updateType == null)
			return true;		
		else if (updateType.equalsIgnoreCase(TYPE_JOIN))
			return true;
		else if (updateType.equalsIgnoreCase(TYPE_ADD_NOTE)
				|| updateType.equalsIgnoreCase(TYPE_SHARE_NOTE)) {
			Note note = mDbMgr.getNote(contentId);
			if (note == null)
				return true;

			if (note.shared_user.contains(mCurrentUser._id))
				return true;
			else
				return false;
		}
		else if (updateType.equalsIgnoreCase(TYPE_ADD_REF)
				|| updateType.equalsIgnoreCase(TYPE_SHARE_REF)) {
			Reference ref = mDbMgr.getReference(contentId);
			if (ref == null)
				return true;

			if (ref.shared_user.contains(mCurrentUser._id))
				return true;
			else
				return false;
		}
		else if (updateType.equalsIgnoreCase(TYPE_ADD_VIDEO)
				|| updateType.equalsIgnoreCase(TYPE_SHARE_VIDEO)) {
			Video video = mDbMgr.getVideo(contentId);
			if (video == null)
				return true;

			if (video.shared_user.contains(mCurrentUser._id))
				return true;
			else
				return false;
		}

		return false;
	}
	
	private String getConceptIdFromContent (String type, String contentId)
	{
		if (type.equalsIgnoreCase("video")) {
			Video video = mDbMgr.getVideo(contentId);
			if (video == null)
				return null;
			else
				return video.conceptId;
		}
		else if (type.equalsIgnoreCase("note")) {
			Note note = mDbMgr.getNote(contentId);
			if (note == null)
				return null;
			else
				return note.conceptId;
		}
		else if (type.equalsIgnoreCase("reference")) {
			Reference ref = mDbMgr.getReference(contentId);
			if (ref == null)
				return null;
			else
				return ref.conceptId;
		}
		
		return null;
	}

	public void getUpdateList(int start) {

		if (!mPrefs.isLogIn())
			return;
		
		// if already get the updates, then skip.
		if (mUpdateArray.size() > 0)
			return;
		
		GetUpdateLstParam param = new GetUpdateLstParam();
		param.token = mPrefs.getToken();
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_UPDATES, param, this);		
	}

	private void getUpdateListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
		}

		if (obj != null){
			GetUpdateLstResult resultBean = (GetUpdateLstResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				
				if (updateCnt == 0)
					mDbMgr.resetUpdateTable();

				for (UpdateResult item : resultBean.updates)
				{
					mDbMgr.addUpdateItem(item);
					updateCnt ++;
				}
				
				if (resultBean.eof == false) {
					getUpdateList (updateCnt);
					return;
				} else {
					updateCnt = 0;
					refreshUpdates();
				}
				
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}		
	}

	private void setShareContentTask(final String token, final String friendId, final String contentType, final String contentId){
		
		final String conceptId = getConceptIdFromContent(contentType, contentId);

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SetShareContent(token, friendId, contentType, contentId);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				onSetShareResult(result, contentType, conceptId);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(getActivity());
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}


	private void onSetShareResult(Object obj, String contentType, String conceptId) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
			if (mWaitDlg != null) {
				mWaitDlg.dismiss();
				mWaitDlg = null;
			}
			return;
		}

		if (obj != null){
			SetShareContentResult resultBean = (SetShareContentResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				
				if (contentType.equalsIgnoreCase("video"))
					getVideoListByConcept (videoCnt, conceptId);
				
				else if (contentType.equalsIgnoreCase("reference"))
					getReferenceListByConcept (refCnt, conceptId);
				
				else if (contentType.equalsIgnoreCase("note"))
					getNoteListByConcept (noteCnt, conceptId);

				return;
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}

		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void getVideoListByConcept(int start, String conceptId) {

		GetVideoLstParam param = new GetVideoLstParam();
		param.checksum = mDbMgr.getVideoCheckSumByConcept(conceptId);
		param.start = start;
		param.count = Constants.COUNT_UNIT;
		param.conceptId = conceptId;

		WebApiInstance.getInstance().executeAPI(Type.GET_VIDEOS_CONCEPT, param, this);
	}

	private void getVideoListByConceptResult(GetVideoLstParam param, Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
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
					refreshUpdates();
				}

			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}
		
		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void getReferenceListByConcept(int start, String conceptId) {

		GetRefLstParam param = new GetRefLstParam();
		param.checksum = mDbMgr.getRefCheckSumByConcept(conceptId);
		param.start = start;
		param.count = Constants.COUNT_UNIT;
		
		param.conceptId = conceptId;

		WebApiInstance.getInstance().executeAPI(Type.GET_REFS_CONCEPT, param, this);
	}

	private void getReferenceListByConceptResult(GetRefLstParam param, Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
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
					getReferenceListByConcept (refCnt, param.conceptId);
					return;
				} else {
					refCnt = 0;
					refreshUpdates();
				}
				
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}
		
		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	private void getNoteListByConcept (int start, String conceptId) {

		GetNoteLstParam param = new GetNoteLstParam();
		param.checksum = mDbMgr.getNoteCheckSumByConcept(conceptId);
		param.start = start;
		param.count = Constants.COUNT_UNIT;
		
		param.conceptId = conceptId;

		WebApiInstance.getInstance().executeAPI(Type.GET_NOTES_CONCEPT, param, this);
	}

	private void getNoteListByConceptResult(GetNoteLstParam param, Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
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
					getNoteListByConcept (noteCnt, param.conceptId);
					return;
				} else {
					refCnt = 0;
					refreshUpdates();
				}
				
			} else if (resultBean.status_code == Constants.STATUS_CODE_NOUPDATE) {
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}
		
		if (mWaitDlg != null) {
			mWaitDlg.dismiss();
			mWaitDlg = null;
		}
	}

	public class UpdateAdapter extends ArrayAdapter<Update> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Update> item = new ArrayList<Update>();

		public UpdateAdapter(Activity activity, int layoutId, ArrayList<Update> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			UpdateHolder holder = null;	

			final Update updateItem = getItem(position);
			UserInfo owner = mDbMgr.getUser(updateItem.owner);

			//			// inflate the view
			//			if (convertView == null) {
			LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
			convertView = inflater.inflate(R.layout.item_update, null);

			holder = new UpdateHolder();

			holder.imgUserPic = (HyIconView) convertView.findViewById(R.id.imgUserPic);
			holder.txtText = (TextView) convertView.findViewById(R.id.txtText);
			holder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
			holder.txtViewDetail = (TextView) convertView.findViewById(R.id.txtViewDetail);
			holder.txtViewDetail.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					if (updateItem.type == null
							|| updateItem.type.equalsIgnoreCase(TYPE_JOIN)) {
						UserInfo friend = mDbMgr.getUser(updateItem.owner);
						startFriendProfileActivity (friend._id);
					}						
					else if (updateItem.type.equalsIgnoreCase(TYPE_ADD_NOTE)
							|| updateItem.type.equalsIgnoreCase(TYPE_SHARE_NOTE)) {
						startContentViewActivity (updateItem.content_id, "note");
					}
					else if (updateItem.type.equalsIgnoreCase(TYPE_ADD_REF)
							|| updateItem.type.equalsIgnoreCase(TYPE_SHARE_REF)) {
						startContentViewActivity (updateItem.content_id, "reference");
					}
					else if (updateItem.type.equalsIgnoreCase(TYPE_ADD_VIDEO)
							|| updateItem.type.equalsIgnoreCase(TYPE_SHARE_VIDEO)) {
						startContentViewActivity (updateItem.content_id, "video");
					}						
				}
			});
			holder.btnAddContent = (Button) convertView.findViewById(R.id.btnAddContent);
			if (isAddedContent(updateItem.type, updateItem.content_id))
				holder.btnAddContent.setVisibility(View.INVISIBLE);
			else
				holder.btnAddContent.setVisibility(View.VISIBLE);
			holder.btnAddContent.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {

					if (updateItem.type.equalsIgnoreCase(TYPE_ADD_NOTE)
							|| updateItem.type.equalsIgnoreCase(TYPE_SHARE_NOTE)) {
						setShareContentTask (mPrefs.getToken(), updateItem.owner, "note", updateItem.content_id);
					}
					else if (updateItem.type.equalsIgnoreCase(TYPE_ADD_REF)
							|| updateItem.type.equalsIgnoreCase(TYPE_SHARE_REF)) {
						setShareContentTask (mPrefs.getToken(), updateItem.owner, "reference", updateItem.content_id);
					}
					else if (updateItem.type.equalsIgnoreCase(TYPE_ADD_VIDEO)
							|| updateItem.type.equalsIgnoreCase(TYPE_SHARE_VIDEO)) {
						setShareContentTask (mPrefs.getToken(), updateItem.owner, "video", updateItem.content_id);
					}						
				}
			});

			//				convertView.setTag(holder);
			//			} 
			//			else
			//				holder = (UpdateHolder) convertView.getTag();

			if (owner.photo_url != null && !owner.photo_url.isEmpty())
				ImageLoader.getInstance().displayImage(owner.photo_url, holder.imgUserPic, 0, 0);
			else
				holder.imgUserPic.imageView.setImageResource(R.drawable.empty_pic);

			holder.txtText.setText(updateItem.text);
			holder.txtDate.setText(Utility.getDateStringFromTimestamp(updateItem.create_date));
			holder.txtViewDetail.setText(getViewText(updateItem.type));

			return convertView;
		}

		public class UpdateHolder {
			HyIconView imgUserPic;
			TextView txtText;
			TextView txtDate;
			TextView txtViewDetail;
			Button btnAddContent;
		}
	}

	private final static Comparator<Update> updateComparator= new Comparator<Update>() {
		private final Collator   collator = Collator.getInstance();
		@Override
		public int compare(Update object1, Update object2) {
			return collator.compare(object2.create_date, object1.create_date);
		}
	};

	@Override
	public void onPreProcessing(Type type, Object parameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResultProcessing(Type type, Object parameter, Object result) {
		// TODO Auto-generated method stub
		switch (type)
		{
		case GET_UPDATES:
			getUpdateListResult(result);			
			break;
		case GET_VIDEOS_CONCEPT:
			getVideoListByConceptResult((GetVideoLstParam)parameter, result);			
			break;
		case GET_REFS_CONCEPT:
			getReferenceListByConceptResult((GetRefLstParam)parameter, result);			
			break;
		case GET_NOTES_CONCEPT:
			getNoteListByConceptResult((GetNoteLstParam)parameter, result);			
			break;
		default:
			break;
		}
	}

}
