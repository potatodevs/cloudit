/**
 * FileUploadAsyncTask
 *
 * Asynchronous Task for calling CloudApp's API to upload a file
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package io.invaders.android.cloudit.threads;

import io.invaders.android.cloudit.BaseActivity;
import io.invaders.android.cloudit.CloudAppApplication;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.cloudapp.api.model.CloudAppProgressListener;

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

			// Get length of file in bytes
			long fileSize = (file.length() / 1024) / 1024;
			if (!isSubscribed && fileSize > 25) {
				message = "Sorry, the file is too big. You have 25MB limit for your uploads.\nGo to my.cl.ly to buy Pro plan and you'll be able to upload upto 250MB files!";
				Log.e("Error", "not subscribed, bigger than 25mb");
				normalError = false;
				return null;
			}
			if (isSubscribed && fileSize > 250) {
				message = "Sorry, the file is too big. You can upload files upto 250MB.";
				Log.e("Error", "subscribed, bigger than 250mb");
				return null;
			}
				

			this.item = api.upload(file, new CloudAppProgressListener() {
				@Override
				public void transferred(long trans, long total) {
					publishProgress((int) (((float) trans * 100f) / (float) total));
				}
			});
			
			Log.e("File uploaded", file.getAbsolutePath().toString());
		} catch (CloudAppException e) {
			if (!isSubscribed && e.getCode() == 200) {
				message = "Sorry, you've reached the daily upload limit allowed by the Free plan.\nGo to my.cl.ly to buy Pro plan and never have this problem!";
				Log.e("Error", "used all uploads");
				normalError = false;
			}
			else {
				message = "Connection error when uploading file. Sorry, try again.";
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

	@SuppressWarnings("deprecation")
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