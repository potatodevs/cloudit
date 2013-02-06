/**
 * FileDeleteAsyncTask
 * 
 * Asynchronous Task for calling CloudApp's API to delete a file 
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp.threads;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.tomasvitek.android.cloudapp.BaseActivity;
import com.tomasvitek.android.cloudapp.CloudAppApplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class FileDeleteAsyncTask extends AsyncTask<CloudAppItem, Integer, Object> {

	ProgressDialog dialog;
	BaseActivity act;
	
	public FileDeleteAsyncTask(BaseActivity act, ProgressDialog dialog) {
		this.act = act;
		this.dialog = dialog;
		
	}
	
	
	@Override
    protected Object doInBackground(CloudAppItem... items) {
		try {
			CloudApp api = ((CloudAppApplication) act.getApplication()).getCloudAppApi();
			
			CloudAppItem item = items[0];		
			
			api.delete(item);
		} catch (CloudAppException e) {
			// TODO toast?
			Log.e("Error", "when uploading file");
		}
		
		return null;
    }
	
	 @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.show();
    }
	 
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		dialog.dismiss();
	}

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        Log.e("progress", progress.toString());
        dialog.setProgress(progress[0]);
    }
	
}