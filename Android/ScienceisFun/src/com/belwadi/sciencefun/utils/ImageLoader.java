package com.belwadi.sciencefun.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;


public class ImageLoader {

	private static final int MAX_IMAGE_SIZE = 512/*256*/;

	private ImageLoaderMemoryCache memoryCache = new ImageLoaderMemoryCache();
	private ImageLoaderFileCache fileCache;
	private Map<ImageProcessingCallback, String> callbacks = Collections.synchronizedMap(new WeakHashMap<ImageProcessingCallback, String>());
	private ExecutorService executorService;
	private Handler handler = new Handler();// handler to display images in UI thread

	private ImageLoader() { }
	 
    private static class SingletonHolder { 
            public static final ImageLoader instance = new ImageLoader();
    }

    public static ImageLoader getInstance() {
            return SingletonHolder.instance;
    }

    public void init(Context context) {
		fileCache = new ImageLoaderFileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}
    
    public void init(Context context, int numberOfThreads) {
		fileCache = new ImageLoaderFileCache(context);
		executorService = Executors.newFixedThreadPool(numberOfThreads);
	}
    
	public void init(Context context, String directory) {
		fileCache = new ImageLoaderFileCache(context, directory);
		executorService = Executors.newFixedThreadPool(5);
	}
	
	
	public void init(Context context, String directory, int numberOfThreads) {
		fileCache = new ImageLoaderFileCache(context, directory);
		executorService = Executors.newFixedThreadPool(numberOfThreads);
	}
    
	
	public void displayImage(String url, ImageProcessingCallback imageProcessingCallback, 
			int w, int h) {
		imageProcessingCallback.onImagePreProcessing();
		callbacks.put(imageProcessingCallback, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			imageProcessingCallback.onImageProcessing(bitmap, w, h);
		}else {
			queuePhoto(url, imageProcessingCallback, w, h);
		}
	}

	private void queuePhoto(String url, ImageProcessingCallback imageProcessingCallback, 
			int w, int h) {
		PhotoToLoad p = new PhotoToLoad(url, imageProcessingCallback);
		executorService.submit(new PhotosLoader(p, w, h));
	}

	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// from web
		try {
			/*
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			copyStream(is, os);
			os.close();
			conn.disconnect();
			bitmap = decodeFile(f);
			return bitmap;
			*/
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			
			// [begin] download image from url
			InputStream input = imageUrl.openStream();

			OutputStream output = new FileOutputStream(f);
			try {
				byte[] buffer = new byte[1024];
				int bytesRead = 0;
				while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
					output.write(buffer, 0, bytesRead);
				}
			} catch (Exception e) {
				bitmap = null;
			} finally {
				output.close();
				input.close();
			}
			// [end] download image from url
			//FileInputStream fis = new FileInputStream(f);
			//bitmap = BitmapFactory.decodeStream(fis);
			bitmap = decodeFile(f);
			//fis.close();
			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError)
				memoryCache.clear();
				return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			int scale = 1;
			
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();

			if (o.outHeight > MAX_IMAGE_SIZE || o.outWidth > MAX_IMAGE_SIZE) {
				scale = (int)Math.pow(2, (int)Math.round(Math.log(MAX_IMAGE_SIZE/(double)Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}
			
//			 decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			//o2.inSampleSize = 1;
			o2.inSampleSize = scale; // 1 = 100% if you write 4 means 1/4 = 25%
			o2.inJustDecodeBounds = false;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageProcessingCallback imageProcessingCallback;
		
		public PhotoToLoad(String u, ImageProcessingCallback i) {
			url = u;
			imageProcessingCallback = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;
		int weight;
		int height;

		PhotosLoader(PhotoToLoad photoToLoad, int w, int h) {
			this.photoToLoad = photoToLoad;
			this.weight = w;
			this.height = h;
		}

		@Override
		public void run() {
			try {
				if (viewReused(photoToLoad))
					return;
				Bitmap bmp = getBitmap(photoToLoad.url);
				if (bmp == null) {
					bmp = photoToLoad.imageProcessingCallback.onImageFailedLoad();
					if (bmp == null)
						return;
				}

				Bitmap roundBitmap = ImageUtil
						.getRoundedCornerBitmap(bmp, 6.0f);
				memoryCache.put(photoToLoad.url, roundBitmap);
				if (viewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(roundBitmap, photoToLoad, weight, height);
				handler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	boolean viewReused(PhotoToLoad photoToLoad) {
		String tag = callbacks.get(photoToLoad.imageProcessingCallback);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;
		int weight;
		int height;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p, int w, int h) {
			bitmap = b;
			photoToLoad = p;
			this.weight = w;
			this.height = h;
		}

		public void run() {
			if (viewReused(photoToLoad))
				return;
			if (bitmap != null) {
				photoToLoad.imageProcessingCallback.onImageProcessing(bitmap, weight, height);
			}
		}
	}
	

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
		System.gc();
	}
	

	public void clearMemoryCache() {
		memoryCache.clear();
		System.gc();
	}
	

	public void clearFileCache() {
		fileCache.clear();
	}


	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

}
