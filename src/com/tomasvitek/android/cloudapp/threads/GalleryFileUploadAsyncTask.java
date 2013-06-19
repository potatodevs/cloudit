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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.cloudapp.api.model.CloudAppProgressListener;
import com.tomasvitek.android.cloudapp.CloudAppApplication;
import com.tomasvitek.android.cloudapp.R;
import com.tomasvitek.android.cloudapp.Start;
import com.tomasvitek.android.cloudapp.UploadFromGalleryActivity;

public class GalleryFileUploadAsyncTask extends AsyncTask<String, Integer, Object> {

	ProgressDialog dialog;
	UploadFromGalleryActivity act;
	private Context mContext;
	private int NOTIFICATION_ID = 1;
	private Notification mNotification;
	private NotificationManager mNotificationManager;
	private Boolean error;
	String message = "File uploaded to CloudApp!";
	boolean normalError = true;
	CloudAppItem item = null;



	public GalleryFileUploadAsyncTask(UploadFromGalleryActivity act) {
		this.act = act;
		this.mContext = act.getBaseContext();
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		error = false;
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

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		dialog.dismiss();
		if (!error) {
			Toast.makeText(act, "File uploaded to CloudApp!", Toast.LENGTH_LONG).show();
			notification();
		}
		act.finish();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		Log.e("progress", progress[0].toString());
		dialog.setProgress(progress[0]);
	}

	@SuppressWarnings("deprecation")
	private void notification() {
		Intent notificationIntent = new Intent(mContext, Start.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
				.setSmallIcon(R.drawable.login_logo).setAutoCancel(true).setContentTitle("CloudApp For Android")
				.setContentText("File successfully uploaded.").setContentIntent(pIntent);
		// Get current notification
		mNotification = builder.getNotification();

		// Show the notification
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
	}
}