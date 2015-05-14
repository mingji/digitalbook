package com.belwadi.sciencefun.view;


import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.utils.ImageProcessingCallback;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class HyIconView extends RelativeLayout implements ImageProcessingCallback {

//	public ImageView imageView;
	private ProgressBar viewProgress;
	public ResizableImageView imageView;
	public boolean flagShowProg = true;
	public Bitmap failedBp = null;

	public HyIconView(Context context) {
		super(context);
		init();
	}

	public HyIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.hy_iconview, this);
//		imageView = (ImageView) findViewById(R.id.id_imageview_bitmap);
		imageView = (ResizableImageView) findViewById(R.id.id_imageview_bitmap);
		viewProgress = (ProgressBar) findViewById(R.id.id_imageview_progress);

	}

	@Override
	public void onImagePreProcessing() {
		//imageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.bg_default));
		if (flagShowProg == true)
			this.viewProgress.setVisibility(View.VISIBLE);
		else
			this.viewProgress.setVisibility(View.GONE);
		this.invalidate();
	}

	@Override
	public void onImageProcessing(Bitmap bitmap, int w, int h) {
		this.viewProgress.setVisibility(View.GONE);
		imageView.setImageBitmap(bitmap);
		this.invalidate();
	}
	
	@Override
	public Bitmap onImageFailedLoad()
	{
		return failedBp;
	}
}
