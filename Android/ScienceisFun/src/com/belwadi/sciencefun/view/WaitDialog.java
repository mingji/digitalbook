package com.belwadi.sciencefun.view;



import com.belwadi.sciencefun.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class WaitDialog extends Dialog {

	private TextView mTextMessage;

	public WaitDialog(Context context) {
		super(context);
		initUI();
	}

	private void initUI() {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		this.setContentView(R.layout.wait_dialog);
		this.setCanceledOnTouchOutside(false);
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL , WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		mTextMessage = (TextView) this.findViewById(R.id.txtMessage);
	}

	public void setMessage(int messageId) {
		mTextMessage.setText(messageId);
	}

	public void setMessage(String strMessage) {
		mTextMessage.setText(strMessage);
	}

	@Override
	public void show() {
		try {
			super.show();
		} catch (Exception e) {
		}
	}

	@Override
	public void dismiss() {
		try {
			super.dismiss();
		} catch (Exception e) {
		}
	}
}
