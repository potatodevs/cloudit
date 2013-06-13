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
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
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

	public GalleryFileUploadAsyncTask(UploadFromGalleryActivity act) {
		this.act = act;
		this.mContext = act.getBaseContext();
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		error = false;
	}

	@Override
	protected Object doInBackground(String... path) {
		try {
			CloudApp api = ((CloudAppApplication) act.getApplication()).getCloudAppApi();

			if (api == null) {
				Log.e("API", "is null!!");
				error = true;
			}

			File file = new File(path[0]);

			Log.e("File uploaded", file.getAbsolutePath().toString());

			api.upload(file, new CloudAppProgressListener() {

				@Override
				public void transferred(long trans, long total) {
					publishProgress((int) (((float) trans * 100f) / (float) total));
				}
			});
		} catch (CloudAppException e) {
			Toast.makeText(act, "Error when uploading file. Try again.", Toast.LENGTH_LONG).show();
			Log.e("Error", "when uploading file");
			error = true;
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