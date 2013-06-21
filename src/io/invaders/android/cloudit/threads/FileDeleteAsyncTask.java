/**
 * FileDeleteAsyncTask
 * 
 * Asynchronous Task for calling CloudApp's API to delete a file 
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package io.invaders.android.cloudit.threads;

import io.invaders.android.cloudit.BaseActivity;
import io.invaders.android.cloudit.CloudAppApplication;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class FileDeleteAsyncTask extends AsyncTask<CloudAppItem, Integer, Object> {

	ProgressDialog dialog;
	BaseActivity act;
	
	String message = "File was deleted";

	
	public FileDeleteAsyncTask(BaseActivity act, ProgressDialog dialog) {
		this.act = act;
		this.dialog = dialog;
		
	}
	
	
	@Override
    protected Object doInBackground(CloudAppItem... items) {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(act);
			String email = prefs.getString("email", "");
			String password = prefs.getString("password", "");
			
			CloudAppApplication app = (CloudAppApplication) act.getApplication();
			CloudApp api = app.createCloudAppApi(email, password);

			if (api == null) {
				Log.e("API", "is null!!");
				message = "Error when uploading file. Try again.";
				return null;
			}
			
			CloudAppItem item = items[0];		
			
			api.delete(item);
		} catch (CloudAppException e) {
			Log.e("Error", "when deleting file");
			message = "Error when uploading file. Try again.";
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
		Toast.makeText(act, message, Toast.LENGTH_LONG).show();
		act.refresh();
	}

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        Log.e("progress", progress.toString());
        dialog.setProgress(progress[0]);
    }
	
}