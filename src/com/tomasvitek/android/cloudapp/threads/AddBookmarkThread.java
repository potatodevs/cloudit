/**
 * AddBookmarkThread
 *
 * Thread for calling CloudApp's API to add a new bookmark
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp.threads;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.tomasvitek.android.cloudapp.CloudAppApplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AddBookmarkThread implements Runnable {

protected Context context;
	
protected String title, url;

Activity activity;

	public AddBookmarkThread(Context context, Activity activity, String title, String url) {
		this.context = context;
		this.activity = activity;
		this.title = title;
		this.url = url;
	}	
	
	@Override
	public void run() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		String email = prefs.getString("email", "");
		String password = prefs.getString("password", "");
		
		CloudAppApplication app = (CloudAppApplication) activity.getApplication();
		CloudApp api = app.createCloudAppApi(email, password);

        // Add a new bookmark
        try {
			@SuppressWarnings("unused")
			CloudAppItem bookmark = api.createBookmark(this.title, this.url);
		} catch (CloudAppException e) {
			Log.e("CloudApp", e.toString());
		}
		
	}

}
