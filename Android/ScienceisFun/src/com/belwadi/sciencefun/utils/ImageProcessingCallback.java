package com.belwadi.sciencefun.utils;

import android.graphics.Bitmap;


public interface ImageProcessingCallback {

	void onImagePreProcessing();
	
	void onImageProcessing(Bitmap bitmap, int w, int h);
	
	Bitmap onImageFailedLoad();
}
