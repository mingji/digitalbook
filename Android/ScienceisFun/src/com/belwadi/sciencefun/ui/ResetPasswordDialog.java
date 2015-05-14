package com.belwadi.sciencefun.ui;


import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.entity.Beans.ResetPwdResult;
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

public class ResetPasswordDialog extends Dialog implements android.view.View.OnClickListener{

	Activity activity;
	WaitDialog mWaitDlg;

	Button mBtnCancel, mBtnSubmit;
	EditText mTxtEmail;

	public ResetPasswordDialog(Context context, Activity activity) {
		super(context);
		// TODO Auto-generated constructor stub
		this.activity = activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_reset_password);

		mTxtEmail = (EditText) findViewById(R.id.txtEmail);
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
			if (!mTxtEmail.getText().toString().isEmpty())
				resetPasswordTask();
			break;			
		default:
			break;
		}
	}

	private void resetPasswordTask(){

		final String mail = mTxtEmail.getText().toString();

		BaseTask.run(new TaskListener() {
			@Override
			public Object onTaskRunning(int taskId, Object data) {
				return Server.ResetPassword(mail);
			}

			@Override
			public void onTaskResult(int taskId, Object result) {
				if (mWaitDlg != null) {
					mWaitDlg.dismiss();
					mWaitDlg = null;
				}
				onResetPasswordResult(result);
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

	private void onResetPasswordResult(Object obj) {		

		if (obj == null) {
			CustomToast.makeCustomToastShort(this.activity, Constants.NETWORK_ERR);
			return;
		}

		if (obj != null){
			ResetPwdResult resultBean = (ResetPwdResult)obj;

			if (resultBean.status_code == Constants.STATUS_CODE_SUCCESS) {
				CustomToast.makeCustomToastShort(this.activity, resultBean.message);
				
				dismiss();
			} else {
				CustomToast.makeCustomToastShort(this.activity, resultBean.message);
			}
		}		
	}
}
