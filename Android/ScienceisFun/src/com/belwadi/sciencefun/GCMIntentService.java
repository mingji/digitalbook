package com.belwadi.sciencefun;
/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.belwadi.sciencefun.ui.MainActivity;
import com.belwadi.sciencefun.utils.AppPreferences;
import com.google.android.gcm.GCMBaseIntentService;

import static com.belwadi.sciencefun.CommonUtilities.SENDER_ID;
import static com.belwadi.sciencefun.CommonUtilities.displayMessage;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);        
		displayMessage(context, "From GCM: device successfully Registered!");
		AppPreferences prefs = new AppPreferences(context);
		prefs.setGcmRegId(registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		displayMessage(context, "From GCM: device successfully Unregistered!");

	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message");
		String message = intent.getStringExtra("text");
		displayMessage(context, message);
		// notifies user
		generateNotification(context, message);
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
		String message = "Received deleted messages notification";
		displayMessage(context, message);
		// notifies user
		generateNotification(context, message);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
		//        displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		//        displayMessage(context, getString(R.string.gcm_recoverable_error,
		//                errorId));
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		String title = context.getString(R.string.app_name);
		
		if (true)
			return;

//		if (CommonMethods.getRunningState()) {
//			return; // nothing.
//			
////			Intent notificationIntent = new Intent(context, MainActivity.class);
////			notificationIntent.putExtra("menu", 7/*pub talk*/);
////			notificationIntent.putExtra("message", message);
////			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////			context.startActivity(notificationIntent);
//		} else {

			NotificationManager notificationManager = (NotificationManager)
					context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(icon, message, when);

			Intent notificationIntent = new Intent(context, MainActivity.class);
			notificationIntent.putExtra("menu", 7/*pub talk*/);
			notificationIntent.putExtra("message", message);
			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
					Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent intent =
					PendingIntent.getActivity(context, 0, notificationIntent, 0);

			notification.setLatestEventInfo(context, title, message, /*intent*/null);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			// Play default notification sound
			notification.defaults |= Notification.DEFAULT_SOUND;
			// Vibrate if vibrate is enabled
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			
			notificationManager.notify(0, notification);
//		}
	}

}
