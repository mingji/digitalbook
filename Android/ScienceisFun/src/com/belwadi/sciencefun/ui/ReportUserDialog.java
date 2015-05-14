package com.belwadi.sciencefun.ui;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.entity.Beans.SendReportUserResult;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.belwadi.sciencefun.view.CustomToast;
import com.belwadi.sciencefun.view.WaitDialog;
import com.belwadi.sciencefun.webservice.BaseTask;
import com.belwadi.sciencefun.webservice.Constants;
import com.belwadi.sciencefun.webservice.Server;
import com.belwadi.sciencefun.webservice.BaseTask.TaskListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ReportUserDialog extends Dialog implements android.view.View.OnClickListener, RadioGroup.OnCheckedChangeListener{

	AppPreferences mPrefs;
	
	Activity activity;
	WaitDialog mWaitDlg;

	Button mBtnCancel, mBtnSubmit;
	EditText mTxtComment;
	
	RadioGroup mRdGroup;
	RadioButton mRdNotClass, mRdBadContent, mRdOther;
	
	LinearLayout mLayOther;
	
	Flag mFlag;
	
	String mUserId;
	String mReportType;
	
	enum Flag {
		NotClass, BadContent, Other
	};

	public ReportUserDialog(Context context, Activity activity, String userId, String reportType) {
		super(context);
		// TODO Auto-generated constructor stub
		this.activity = activity;
		mPrefs = new AppPreferences(this.activity);
		
		this.setCanceledOnTouchOutside(false);
		
		this.mUserId = userId;
		this.mReportType = reportType;
		
		mFlag = Flag.NotClass;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_report_user);

		mRdGroup = (RadioGroup) findViewById(R.id.radioGroup);
		mRdNotClass = (RadioButton) findViewById(R.id.rbNotinClass);
		mRdBadContent = (RadioButton) findViewById(R.id.rbBadContent);
		mRdOther = (RadioButton) findViewById(R.id.rbOther);
		
		mRdNotClass.setText("Not in Class");
		mRdBadContent.setText("Bad Content");
		mRdOther.setText("Other");
		
		if (mReportType.equalsIgnoreCase("content")) {
			mRdBadContent.setVisibility(View.VISIBLE);
			mRdBadContent.setChecked(true);
			
			mFlag = Flag.BadContent;
		} else {
			mRdNotClass.setChecked(true);
			mRdBadContent.setVisibility(View.GONE);
			
			mFlag = Flag.NotClass;
		}
		
		mRdGroup.setOnCheckedChangeListener(this);		
		
		mTxtComment = (EditText) findViewById(R.id.txtComment);
		mTxtComment.setEnabled(false);
		
		mLayOther = (LinearLayout) findViewById(R.id.layOther);
		mLayOther.setEnabled(false);
		
		mBtnCancel = (Button) findViewById(R.id.btnCancel);
		mBtnSubmit = (Button) findViewById(R.id.btnSubmit);

		mBtnCancel.setOnClickListener(this);
		mBtnSubmit.setOnClickListener(this);		
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnCancel:
			dismiss();
			break;
		case R.id.btnSubmit:
			sendRportUserTask();
			break;			
		default:
			break;
		}
	}
	

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		
		switch (checkedId)
		{
		case R.id.rbNotinClass:
			mFlag = Flag.NotClass;
			mTxtComment.setText("");
			mTxtComment.setEnabled(false);
			mLayOther.setEnabled(false);			
			break;
		case R.id.rbBadContent:
			mFlag = Flag.BadContent;
			mTxtComment.setText("");
			mTxtComment.setEnabled(false);
			mLayOther.setEnabled(false);			
			break;
		case R.id.rbOther:
			mFlag = Flag.Other;
			mTxtComment.setEnabled(true);
			mLayOther.setEnabled(true);
			break;
		default :
			break;
		}
		
	}

	private void sendRportUserTask(){

		final String reason, userId;
		
		userId = mUserId;
		
		if (mFlag == Flag.NotClass)
			reason = "Not in Class";
		else if (mFlag == Flag.Other)
			reason = mTxtComment.getText().toString();
		else if (mFlag == Flag.BadContent)
			reason = "Bad Content";
		else
			reason = "";
		
		if (mFlag == Flag.Other && reason.isEmpty()) {
			CustomToast.makeCustomToastShort(this.activity, "Please provide a reason. This helps us take required steps.");
			return;
		}

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.SendReportUser(mPrefs.getToken(), userId, reason);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onSendReportUserResult(result);
			}

			@Override
			public void onTaskProgress(int taskId, Object... values) {
			}

			@Override
			public void onTaskPrepare(int taskId, Object data) {
				mWaitDlg = new WaitDialog(getContext());
				mWaitDlg.setMessage("Please wait ...");
				mWaitDlg.show();
			}

			@Override
			public void onTaskCancelled(int taskId) {
			}
		});

	}

	private void onSendReportUserResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this.activity, Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			SendReportUserResult resultBean = (SendReportUserResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				CustomToast.makeCustomToastShort(this.activity, resultBean.message);				
				dismiss();
			} else {
				CustomToast.makeCustomToastShort(this.activity, resultBean.message);
			}
		}		
	}

}
