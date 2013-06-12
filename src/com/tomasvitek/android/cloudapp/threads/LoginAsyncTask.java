/**
 * LoginAsyncTask
 *
 * Asynchronous Task for logging in to CloudApp's API
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp.threads;

import java.util.ArrayList;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.cloudapp.impl.CloudAppImpl;
import com.tomasvitek.android.cloudapp.BaseActivity;
import com.tomasvitek.android.cloudapp.CloudAppApplication;
import com.tomasvitek.android.cloudapp.LoginActivity;
import com.tomasvitek.android.cloudapp.models.ListItem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LoginAsyncTask extends AsyncTask<String[], Void, StringBuffer> {

	protected Context context;
	protected BaseActivity activity;
	protected String email, password;
	protected int page;
	protected ProgressDialog dialog;

	protected boolean success;

	protected CloudApp api;

	protected ArrayList<ListItem> items;

	public LoginAsyncTask(BaseActivity activity) {
		this.context = activity.getApplicationContext();
		this.activity = activity;
		this.dialog = null;
	}

	public LoginAsyncTask(BaseActivity activity, ProgressDialog dialog) {
		this.context = activity.getApplicationContext();
		this.activity = activity;
		this.dialog = dialog;
	}

	@Override
	protected StringBuffer doInBackground(String[]... arg0) {

		email = arg0[0][0];
		password = arg0[0][1];
		page = Integer.parseInt(arg0[0][2]);

		api = new CloudAppImpl(email, password);

		CloudAppApplication app = (CloudAppApplication) activity.getApplication();
		app.setCloudAppApi(api);

		this.success = true;

		items = new ArrayList<ListItem>();

		// Add a new bookmark
		try {
			// int count = (int)api.getAccountStats().getItems();
			int count = 10;
			ArrayList<CloudAppItem> its = (ArrayList<CloudAppItem>) api.getItems(1, count, null, false, null);
			for (CloudAppItem i : its) {
				items.add(new ListItem(i));
			}

			app.setList(items);
		} catch (CloudAppException e) {
			// TODO Auto-generated catch block
			Log.e("CloudApp", e.toString());
			this.success = false;
		}

		return null;

	}

	protected void onPostExecute(StringBuffer sb) {
		if (dialog != null) {
			try {
				dialog.dismiss();
				dialog = null;
			} catch (Exception e) {
				//nothing
			}
		}

		if (this.success) {
			activity.taskDone(email, password);
		} else {
			Toast.makeText(context, "Wrong password!", Toast.LENGTH_LONG).show();

			Intent intent = new Intent(activity, LoginActivity.class);
			activity.startActivity(intent);
		}
	}

}
