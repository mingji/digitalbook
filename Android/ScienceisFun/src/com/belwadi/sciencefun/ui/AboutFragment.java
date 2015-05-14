package com.belwadi.sciencefun.ui;

import com.belwadi.sciencefun.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment implements OnClickListener
{
	TextView mTxtUrl;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_about, null);

		initView (view);

		return view;
	}

	private void initView (View view)
	{
		mTxtUrl = (TextView) view.findViewById(R.id.txtUrl);
		
		mTxtUrl.setText(
				Html.fromHtml(
						"<a href=\"http://www.scienceisfun.in\">www.scienceisfun.in</a> "));
		mTxtUrl.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
}
