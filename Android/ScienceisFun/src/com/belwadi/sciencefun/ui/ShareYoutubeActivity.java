package com.belwadi.sciencefun.ui;


import java.util.regex.Matcher;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.belwadi.sciencefun.R;

public class ShareYoutubeActivity extends Activity implements OnClickListener{

	Button mBtnCancel, mBtnCopy;

	TextView mTxtUrl;
	
	String value;
	String url;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_share_youtube);

		Bundle extras = getIntent().getExtras();
		value = extras.getString(Intent.EXTRA_TEXT);

		Matcher m = android.util.Patterns.WEB_URL.matcher(value);
		while (m.find()) {
			url = m.group();
	    }

		initView();

	}

	private void initView()
	{
		mTxtUrl = (TextView) findViewById(R.id.txtUrl);
		mTxtUrl.setText("" + url);

		mBtnCopy = (Button) findViewById(R.id.btnCopy);
		mBtnCancel = (Button) findViewById(R.id.btnCancel);

		mBtnCopy.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();


		switch (id)
		{
		case R.id.btnCopy:
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("label", mTxtUrl.getText().toString().trim());
			clipboard.setPrimaryClip(clip);
			finish();
			break;
		case R.id.btnCancel:
			finish();
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
			bool = true;
		}
		return bool;
	}
}
