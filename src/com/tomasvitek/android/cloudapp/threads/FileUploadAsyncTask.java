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

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppProgressListener;
import com.tomasvitek.android.cloudapp.BaseActivity;
import com.tomasvitek.android.cloudapp.CloudAppApplication;

public class FileUploadAsyncTask extends AsyncTask<String, Integer, Object> {

	ProgressDialog dialog;
	BaseActivity act;

	public FileUploadAsyncTask(BaseActivity act) {
		this.act = act;
	}

	@Override
	protected Object doInBackground(String... path) {
		try {
			CloudApp api = ((CloudAppApplication) act.getApplication()).getCloudAppApi();

			if (api == null)
				Log.e("API", "is null!!");

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
		Toast.makeText(act, "File uploaded!", Toast.LENGTH_LONG).show();
		act.refresh();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		Log.e("progress", progress[0].toString());
		dialog.setProgress(progress[0]);
	}

}