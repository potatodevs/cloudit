/**
 * FileUploadAsyncTask
 *
 * Asynchronous Task for calling CloudApp's API to upload a file
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp.threads;

import java.io.File;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.cloudapp.api.model.CloudAppProgressListener;
import com.cloudapp.impl.model.CloudAppItemImpl;
import com.tomasvitek.android.cloudapp.BaseActivity;
import com.tomasvitek.android.cloudapp.CloudAppApplication;
import com.tomasvitek.android.cloudapp.ListActivity;

public class FileUploadAsyncTask extends AsyncTask<String, Integer, Object> {

	ProgressDialog dialog;
	BaseActivity act;
	
	String message = "File uploaded to CloudApp!";
	CloudAppItem item = null;
	
	boolean normalError = true;

	public FileUploadAsyncTask(BaseActivity act) {
		this.act = act;
	}

	@Override
	protected Object doInBackground(String... path) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
		String email = prefs.getString("email", "");
		String password = prefs.getString("password", "");
		
		CloudAppApplication app = (CloudAppApplication) act.getApplication();
		CloudApp api = app.createCloudAppApi(email, password);

		boolean isSubscribed = false;
		
		if (api == null) {
			Log.e("API", "is null!!");
			message = "Error when uploading file. Try again.";
			return null;
		}
		
		try {
			isSubscribed = api.getAccountDetails().isSubscribed();
			
			File file = new File(path[0]);

			this.item = api.upload(file, new CloudAppProgressListener() {
				@Override
				public void transferred(long trans, long total) {
					publishProgress((int) (((float) trans * 100f) / (float) total));
				}
			});
			
			Log.e("File uploaded", file.getAbsolutePath().toString());
		} catch (CloudAppException e) {
			if (!isSubscribed && e.getCode() == 200) {
				message = "Sorry, it looks like you've used all your uploads for today. You can wait some time or get a subscription for CloudApp.";
				Log.e("Error", "used all uploads");
				normalError = false;
			}
			else {
				message = "Error when uploading file. Try again.";
				Log.e("Error", "when uploading file");
			}
		}
		
		return null;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = new ProgressDialog(act);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMessage("Uploading...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(false);
		dialog.setMax(100);
		dialog.setProgress(0);
		dialog.show();
	}

	@SuppressLint({ "NewApi", "ServiceCast" })
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
		boolean saveClipboard = prefs.getBoolean("save_to_clipboard", false);
		
		if (saveClipboard && item != null) {
			try {
				int sdk = android.os.Build.VERSION.SDK_INT;
				if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
					android.text.ClipboardManager clipboard = (android.text.ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);			
					clipboard.setText(item.getUrl());
				} else {
					android.content.ClipboardManager clipboard = (android.content.ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);
					android.content.ClipData clip = android.content.ClipData.newPlainText(item.getName() + "'s url", item.getUrl());
					clipboard.setPrimaryClip(clip);
				}
				message = message + "\nLink has been copied to the clipboard.";
			} catch (CloudAppException e) {}
		}
		
		if (normalError) 
			Toast.makeText(act, message, Toast.LENGTH_LONG).show();
		else {
			AlertDialog.Builder b = new AlertDialog.Builder(act);
			b.setTitle("Sorry").setMessage(message)
			    .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int id) {
						d.dismiss();
					}
				})
		    	.show();
		}
		
		dialog.dismiss();
		act.refresh();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		Log.e("progress", progress[0].toString());
		dialog.setProgress(progress[0]);
	}

}