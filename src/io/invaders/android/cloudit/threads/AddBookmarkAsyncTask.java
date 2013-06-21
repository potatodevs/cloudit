/**
 * AddBookmarkThread
 *
 * Thread for calling CloudApp's API to add a new bookmark
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package io.invaders.android.cloudit.threads;

import io.invaders.android.cloudit.BaseActivity;
import io.invaders.android.cloudit.CloudAppApplication;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;

@SuppressLint("NewApi")
public class AddBookmarkAsyncTask extends AsyncTask<String[], Integer, Object> {

	protected String title = "", url = "";
	
	Activity activity;

	private String message = "Bookmark added to CloudApp.";
	
	boolean finishParentActivityWhenDone = false;
	
	public AddBookmarkAsyncTask(Activity activity) {
		this.activity = activity;
	}	
	
	public AddBookmarkAsyncTask(Activity activity, boolean finishParentActivityWhenDone) {
		this.activity = activity;
		this.finishParentActivityWhenDone = finishParentActivityWhenDone;
	}	
	
	protected Object doInBackground(String[]... arg0) {
		this.title = arg0[0][0];
		this.url = arg0[0][1];
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		String email = prefs.getString("email", "");
		String password = prefs.getString("password", "");
		
		CloudAppApplication app = (CloudAppApplication) activity.getApplication();
		CloudApp api = app.createCloudAppApi(email, password);

        // Add a new bookmark
        try {
			CloudAppItem item = api.createBookmark(this.title, this.url);
			
			if (item != null) {
				return item;
			}
			else {
				message = "Sorry, there was an error. Try again. Thanks.";
				return null;
			}
		} catch (CloudAppException e) {
			message = "Sorry, there was an error. Try again. Thanks.";
			return null;
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint({ "NewApi", "ServiceCast" })
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		
		CloudAppItem item = (CloudAppItem)result;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean saveClipboard = prefs.getBoolean("save_to_clipboard", false);
	
		try {
			if (saveClipboard && item != null) {
				int sdk = android.os.Build.VERSION.SDK_INT;
				if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
					android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);			
					clipboard.setText(item.getUrl());
				} else {
					android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
					android.content.ClipData clip = android.content.ClipData.newPlainText(item.getName() + "'s url", item.getUrl());
					clipboard.setPrimaryClip(clip);
				}
				message = message + "\nLink has been copied to the clipboard.";
			}
		} catch (CloudAppException e) {}
		
		Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
		
		// from sharing
		if (finishParentActivityWhenDone) { 
			activity.finish();
		}
		// from listview
		else {
			BaseActivity baseAct = (BaseActivity) activity;
			baseAct.refresh();
		}
	}

}
