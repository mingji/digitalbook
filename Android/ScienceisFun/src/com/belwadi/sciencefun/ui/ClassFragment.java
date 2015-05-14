package com.belwadi.sciencefun.ui;

import java.util.ArrayList;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.GlobalValue;
import com.belwadi.sciencefun.entity.Grade;
import com.belwadi.sciencefun.entity.Note;
import com.belwadi.sciencefun.entity.Scientist;
import com.belwadi.sciencefun.entity.UserInfo;
import com.belwadi.sciencefun.entity.Beans.AddFriendResult;
import com.belwadi.sciencefun.entity.Beans.DelFriendResult;
import com.belwadi.sciencefun.entity.Beans.GetAllUserLstResult;
import com.belwadi.sciencefun.entity.Beans.GetUserLstParam;
import com.belwadi.sciencefun.entity.Beans.InviteUserResult;
import com.belwadi.sciencefun.entity.Beans.UserResult;
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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ClassFragment extends Fragment implements OnClickListener, WebApiCallback
{
	private final int FRIEND_PROFILE_ACTIVITY = 0;
	
	private DatabaseManager mDbMgr;
	private AppPreferences mPrefs;
	private UserInfo mCurrentUser;	
	
	RelativeLayout mLayGuest, mLayBody;

	TextView mTxtGuestMsg;

	private FriendAdapter mAdapterClass, mAdapterOther;
	private GridView mGridClassMates, mGridOtherMates;

	private ArrayList<UserInfo> mArrayClassmates, mArrayOthermates;

	private EditText mTxtSearchFriend;
	private TextView mTxtMoreClass, mTxtMoreOther;
	private TextView mTxtGroup1School, mTxtGroup1Section;
	
	private ArrayList<UserInfo> mArraySearchedUsers;
	private ListView mListSearchedUser;
	private SearchedUserAdapter mAdapterSearchedUser;

	private TextView mTxtNFoundMail;
	private LinearLayout mLayInvite;

	private Button mBtnSearchFriend;

	ScrollView mViewFriends;
	RelativeLayout mLayFound, mLayNotFound, mLayNotMail, mLayNotResult;

	private ClassState mState;

	private enum ClassState {
		VIEW, FOUND, NOTFOUND, NONE
	};

	WaitDialog mWaitDlg;
	
	int userCnt = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_class, null);

		mDbMgr = new DatabaseManager(getActivity());
		mPrefs = new AppPreferences(getActivity());
		
		mState = ClassState.VIEW;		

		if (mPrefs.isLogIn())
			mCurrentUser = GlobalValue.getInstance().getCurrentUser();
		else
			mCurrentUser = null;

		initView(view);

		resetView();

		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
	}

	private void initView(View view)
	{
		mLayGuest = (RelativeLayout) view.findViewById(R.id.layGuestMsg);
		mLayBody= (RelativeLayout) view.findViewById(R.id.layBody);

		mTxtGuestMsg = (TextView) view.findViewById(R.id.txtGuestMsg);
		initGuestMsg();

		mBtnSearchFriend = (Button)view.findViewById(R.id.btnSearchFriend);

		mTxtSearchFriend = (EditText)view.findViewById(R.id.txtSearchFriend);

		mViewFriends = (ScrollView) view.findViewById(R.id.ScrollviewFriends);
		mLayFound = (RelativeLayout) view.findViewById(R.id.layFoundedFriend);
		mLayNotFound = (RelativeLayout) view.findViewById(R.id.layNotFound);
		mLayNotMail = (RelativeLayout) view.findViewById(R.id.layNotMail);
		mLayNotResult = (RelativeLayout) view.findViewById(R.id.layNotResult);

		mArrayClassmates = new ArrayList<UserInfo>();
		mArrayOthermates = new ArrayList<UserInfo>();

		mAdapterClass = new FriendAdapter(getActivity(), R.layout.item_friend, mArrayClassmates);
		mAdapterOther = new FriendAdapter(getActivity(), R.layout.item_friend, mArrayOthermates);

		mGridClassMates = (GridView)view.findViewById(R.id.gridClassmates);
		mGridOtherMates = (GridView)view.findViewById(R.id.gridOthermates);

		mGridClassMates.setAdapter(mAdapterClass);
		mGridOtherMates.setAdapter(mAdapterOther);

		mTxtMoreClass = (TextView) view.findViewById(R.id.txtMoreClass);
		mTxtMoreOther = (TextView) view.findViewById(R.id.txtMoreOther);

		mTxtGroup1School = (TextView) view.findViewById(R.id.txtGroup1School);
		mTxtGroup1Section = (TextView) view.findViewById(R.id.txtGroup1Grade);

		if (mCurrentUser != null) {
			mTxtGroup1School.setText(mCurrentUser.schoolName);

			Grade grade = mDbMgr.getGrade(mCurrentUser.grade_id);
			if (grade != null)
				mTxtGroup1Section.setText("Grade " + grade.grade_name + " Section " + mCurrentUser.section);
			else
				mTxtGroup1Section.setText("Grade --" + " Section " + mCurrentUser.section);
		}

		mBtnSearchFriend.setOnClickListener(this);

		mTxtMoreClass.setOnClickListener(this);
		mTxtMoreOther.setOnClickListener(this);

		mGridClassMates.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				UserInfo user = mArrayClassmates.get(position);
				if (user != null)
					startFriendProfileActivity(user._id);
			}
		});
		mGridOtherMates.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				UserInfo user = mArrayOthermates.get(position);
				if (user == null)
					return;
				if (user.isScientist)
					openGuestProfile(user.name);
				else
					startFriendProfileActivity(user._id);
			}
		});
		
		mArraySearchedUsers = new ArrayList<UserInfo>();
		mAdapterSearchedUser = new SearchedUserAdapter(getActivity(), R.layout.item_searched_user, mArraySearchedUsers);
		mListSearchedUser = (ListView)view.findViewById(R.id.lstSearchedUser);
		mListSearchedUser.setAdapter(mAdapterSearchedUser);

		mTxtNFoundMail = (TextView) view.findViewById(R.id.txtNotFoundMail);
		mLayInvite = (LinearLayout) view.findViewById(R.id.layInvite);
		mLayInvite.setOnClickListener(this);
	}

	private void updateFriendList()
	{
		mArrayClassmates.clear();
		mArrayOthermates.clear();

		ArrayList<UserInfo> friendLst = mDbMgr.getFriends(mCurrentUser);

		for (UserInfo friend : friendLst)
		{
			if (friend._id.equalsIgnoreCase(mCurrentUser._id))
				continue;

			if (friend.schoolName.equalsIgnoreCase(mCurrentUser.schoolName)
					&& friend.grade_id.equalsIgnoreCase(mCurrentUser.grade_id)) {

				if (friend.section.equalsIgnoreCase(mCurrentUser.section))
					mArrayClassmates.add(friend);

			} else {
				friend.isFriendGroup = true;
				mArrayOthermates.add(friend);
			}
		}
		
		ArrayList <Scientist> scientistLst = Utility.getRandomScientistList(getActivity(), 3);
		for (Scientist scientist: scientistLst) {			
			UserInfo guestInfo = new UserInfo("scientist", scientist.name, scientist.imageUrl);
			
			guestInfo.isFriendGroup = true;
			guestInfo.isScientist = true;
			
			mArrayOthermates.add(guestInfo);
		}

		mAdapterClass.notifyDataSetChanged();
		mAdapterOther.notifyDataSetChanged();
	}
	
	private void openGuestProfile(String name)
	{
		String searchName = name.replace(" ", "%20");
		String url = "http://www.google.com/search?q=" + searchName;
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}
	
	private void initGuestMsg()
	{
		String strNote = "You have not enrolled in any class. \nSign In  or  Sign Up for Free.";
		SpannableString ss = new SpannableString(strNote);

		ClickableSpan signinSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				HomeActivity parent = (HomeActivity)getActivity();
				parent.startSigninActivity();
			}
		};

		ClickableSpan signupSpan = new ClickableSpan() {
			@Override
			public void onClick(View textView) {
				HomeActivity parent = (HomeActivity)getActivity();
				parent.startSignupActivity();
			}
		};

		int start1 = strNote.indexOf("Sign In");
		int start2 = strNote.indexOf("Sign Up");		
		ss.setSpan(signinSpan, start1, start1 + "Sign In".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ss.setSpan(signupSpan, start2, start2 + "Sign Up".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		mTxtGuestMsg.setText(ss);
		mTxtGuestMsg.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub

		int id = view.getId();

		switch (id){
		case R.id.btnSearchFriend:
			onSearchFriend();
			break;
		case R.id.txtMoreClass:
			break;
		case R.id.txtMoreOther:
			break;

//		case R.id.txtViewProfile:
//			startFriendProfileActivity(mFoundUser._id);
//			mTxtSearchFriend.setText("");
//			mState = ClassState.VIEW;
//			showDetailView();
//			break;
//		case R.id.txtAddFriend:
//			onAddFriend();
//			break;
		case R.id.layInvite:
			onInviteFriend();
			break;
		default:
			break;
		}
	}
	
	private void searchUsers(String searchTxt)
	{
		mArraySearchedUsers.clear();

		ArrayList<UserInfo> userLst = mDbMgr.searchUserByNameMail(searchTxt);

		for (UserInfo user : userLst)
		{
			if (user._id.equalsIgnoreCase(mCurrentUser._id))
				continue;

			mArraySearchedUsers.add(user);				
		}

		mAdapterSearchedUser.notifyDataSetChanged();
	}

	private void onSearchFriend()
	{
		String searchTxt = mTxtSearchFriend.getText().toString().trim();

		if (searchTxt.isEmpty()) {
			mState = ClassState.VIEW;
			showDetailView();
			return;
		}

//		if (!Utility.isEmailValid(searchTxt)) {
//			CustomToast.makeCustomToastShort(getActivity(), "Please enter a valid email address.");
//			return;
//		}

		searchUsers(searchTxt);

		if (mArraySearchedUsers.size() > 0) {
			mState = ClassState.FOUND;
		} else {
			mState = ClassState.NOTFOUND;
		}

		showDetailView();			
	}
	
	public void resetView() 
	{
		if (mTxtSearchFriend == null)
			return;
		
		mTxtSearchFriend.setText("");
		mState = ClassState.VIEW;
		showDetailView();
		
		if (mPrefs.isLogIn()) {
			mLayGuest.setVisibility(View.GONE);
			mLayBody.setVisibility(View.VISIBLE);
			
			mCurrentUser = GlobalValue.getInstance().getCurrentUser();
			
			updateFriendList();
			
			mTxtGroup1School.setText(mCurrentUser.schoolName);
			Grade grade = mDbMgr.getGrade(mCurrentUser.grade_id);
			if (grade != null)
				mTxtGroup1Section.setText("Grade " + grade.grade_name + " Section " + mCurrentUser.section);
			else
				mTxtGroup1Section.setText("Grade --" + " Section " + mCurrentUser.section);
		} else {
			mLayGuest.setVisibility(View.VISIBLE);
			mLayBody.setVisibility(View.GONE);
			
			mCurrentUser = null;
		}
		

	}

	private void onAddFriend(String userId)
	{
		addFriend(mPrefs.getToken(), userId);
	}

	private void onInviteFriend()
	{
		inviteUser(mPrefs.getToken(), mTxtNFoundMail.getText().toString().trim());
	}
	
	private boolean checkFriendUser(String userId)
	{	
		ArrayList<UserInfo> friendLst = mDbMgr.getFriends(mCurrentUser);
		for (UserInfo friend : friendLst)
		{
			if (friend._id.equalsIgnoreCase(mCurrentUser._id))
				continue;
			
			if (friend._id.equalsIgnoreCase(userId)) {
				return true;
			}
		}
		
		return false;
	}	

	private void showDetailView()
	{
		mViewFriends.setVisibility(View.GONE);
		mLayFound.setVisibility(View.GONE);
		mLayNotFound.setVisibility(View.GONE);

		if (mState == ClassState.VIEW) {
			mViewFriends.setVisibility(View.VISIBLE);
		} else if (mState == ClassState.FOUND) {
			mLayFound.setVisibility(View.VISIBLE);
		} else if (mState == ClassState.NOTFOUND) {
			mTxtNFoundMail.setText(mTxtSearchFriend.getText().toString().trim());
			mLayNotFound.setVisibility(View.VISIBLE);
			
			String searchTxt = mTxtSearchFriend.getText().toString().trim();
			
			if (!Utility.isEmailValid(searchTxt)){
				mLayNotResult.setVisibility(View.VISIBLE);
				mLayNotMail.setVisibility(View.GONE);
			} else {
				mLayNotResult.setVisibility(View.GONE);
				mLayNotMail.setVisibility(View.VISIBLE);
			}
		} else {			
			mViewFriends.setVisibility(View.VISIBLE);
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
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode != Activity.RESULT_OK)
			return;

		if (requestCode == FRIEND_PROFILE_ACTIVITY) {
			String goPage = data.getStringExtra("go_page");

			if (goPage != null) {
				HomeActivity parentActivity = (HomeActivity)getActivity();
				
				parentActivity.porgressResult(goPage);
			}
		}
	}

	private void addFriend(final String token, final String userId){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.AddFriend(token, userId);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onAddFriendResult(result);
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


	private void onAddFriendResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			AddFriendResult resultBean = (AddFriendResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
//				mTxtSearchFriend.setText("");
//				mState = ClassState.VIEW;
//				showDetailView();
				userCnt = 0;
				getUserList(userCnt);
				
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);				
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}		
	}
	
	private void delFriend(final String token, final String userId){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.DelFriend(token, userId);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onDelFriendResult(result);
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


	private void onDelFriendResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			DelFriendResult resultBean = (DelFriendResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
//				mTxtSearchFriend.setText("");
//				mState = ClassState.VIEW;
//				showDetailView();
				userCnt = 0;
				getUserList(userCnt);
				
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}		
	}

	private void inviteUser(final String token, final String mail){

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.InviteUser(token, mail);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onInviteUserResult(result);
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

	private void onInviteUserResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			InviteUserResult resultBean = (InviteUserResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
//				mTxtSearchFriend.setText("");
//				mState = ClassState.VIEW;
//				showDetailView();
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			} else {
				CustomToast.makeCustomToastShort(getActivity(), resultBean.message);
			}
		}		
	}
	
	private void getUserList(int start) {
		
		mWaitDlg = new WaitDialog(getActivity());
		mWaitDlg.setMessage("Please wait ...");
		mWaitDlg.show();		

		GetUserLstParam param = new GetUserLstParam();
		param.checksum = mDbMgr.getUserCheckSum();
		param.start = start;
		param.count = Constants.COUNT_UNIT;

		WebApiInstance.getInstance().executeAPI(Type.GET_USERS, param, this);
	}

	private void getUserListResult(Object obj) {

		if (obj == null) {
			CustomToast.makeCustomToastShort(getActivity(), Constants.NETWORK_ERR);
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
				} else {
					userCnt = 0;
					updateFriendList();
					onSearchFriend();
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

	public class FriendAdapter extends ArrayAdapter<UserInfo> {
		Activity activity;
		int layoutResourceId;
		ArrayList<UserInfo> item = new ArrayList<UserInfo>();

		public FriendAdapter(Activity activity, int layoutId, ArrayList<UserInfo> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			FriendHolder holder = null;	

			final String token = mPrefs.getToken();
			final UserInfo friendItem = getItem(position);			

			// inflate the view
//			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_friend, null);
				holder = new FriendHolder();
				holder.imgPic = (HyIconView) convertView.findViewById(R.id.imgPic);
				holder.txtName = (TextView) convertView.findViewById(R.id.txtName);				
				holder.imgDel = (ImageView) convertView.findViewById(R.id.imgDelete);	
				holder.imgDel.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						
						delFriend (token, friendItem._id);
					}
				});
				
				convertView.setTag(holder);
//			} 
//			else
//				holder = (FriendHolder) convertView.getTag();

			if (friendItem.photo_url != null && !friendItem.photo_url.isEmpty())
				ImageLoader.getInstance().displayImage(friendItem.photo_url, holder.imgPic, 0, 0);
			else
				holder.imgPic.imageView.setImageResource(R.drawable.empty_pic);
			holder.txtName.setText(friendItem.name);
			
			if (friendItem.isFriendGroup == true && friendItem.isScientist == false)
				holder.imgDel.setVisibility(View.VISIBLE);
			else
				holder.imgDel.setVisibility(View.GONE);

			return convertView;
		}

		public class FriendHolder {
			HyIconView imgPic;
			TextView txtName;
			ImageView imgDel;
		}
	}
	
	public class SearchedUserAdapter extends ArrayAdapter<UserInfo> {
		Activity activity;
		int layoutResourceId;
		ArrayList<UserInfo> item = new ArrayList<UserInfo>();

		public SearchedUserAdapter(Activity activity, int layoutId, ArrayList<UserInfo> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			SearchUserHolder holder = null;	

			final UserInfo userItem = getItem(position);
			Grade grade = mDbMgr.getGrade(userItem.grade_id);

			// inflate the view
//			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_searched_user, null);
				holder = new SearchUserHolder(position);
				
				holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
				holder.txtMail = (TextView) convertView.findViewById(R.id.txtMail);
				holder.txtSchool = (TextView) convertView.findViewById(R.id.txtSchool);
				holder.txtClass = (TextView) convertView.findViewById(R.id.txtClass);
				holder.txtAddFriend = (TextView) convertView.findViewById(R.id.txtAddFriend);
				holder.txtViewProfile = (TextView) convertView.findViewById(R.id.txtViewProfile);
				
				holder.txtAddFriend.setOnClickListener(holder);
				holder.txtViewProfile.setOnClickListener(holder);
				
//				holder.txtAddFriend.setOnClickListener(new View.OnClickListener() {
//					public void onClick(View v) {
//						
//						onAddFriend(userItem._id);
//					}
//				});
				
				holder.imgPic = (HyIconView) convertView.findViewById(R.id.imgPic);

				convertView.setTag(holder);
//			} 
//			else
//				holder = (SearchUserHolder) convertView.getTag();

			if (userItem.photo_url != null && !userItem.photo_url.isEmpty())
				ImageLoader.getInstance().displayImage(userItem.photo_url, holder.imgPic, 0, 0);
			else
				holder.imgPic.imageView.setImageResource(R.drawable.empty_pic);
			
			holder.txtName.setText(userItem.name);
			holder.txtMail.setText(userItem.mail);
			holder.txtSchool.setText(userItem.schoolName);
			if (grade != null)
				holder.txtClass.setText("Grade " + grade.grade_name + ", Section " + userItem.section);
			else
				holder.txtClass.setText("Grade --, Section " + userItem.section);
			
			if (checkFriendUser(userItem._id))
				holder.txtAddFriend.setVisibility(View.INVISIBLE);
			else
				holder.txtAddFriend.setVisibility(View.VISIBLE);

			return convertView;
		}		

		public class SearchUserHolder implements OnClickListener {			
			TextView txtName, txtMail, txtSchool, txtClass, txtViewProfile, txtAddFriend;
			HyIconView imgPic;
			
			int itemIndex;

			public SearchUserHolder(int index) {
				super();
				this.itemIndex = index;
			}

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int viewId = v.getId();

				UserInfo user = mArraySearchedUsers.get(itemIndex);					
				if (user == null)
					return;

				switch (viewId)
				{
				case R.id.txtAddFriend:
					onAddFriend(user._id);
					break;
				case R.id.txtViewProfile:
					startFriendProfileActivity(user._id);
					break;
				default:
					break;
				}
			}
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
		case GET_USERS:
			getUserListResult(result);
			break;
		default:
			break;
		}		
	}

}
