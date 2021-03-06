/**
 * LoginAsyncTask
 *
 * Asynchronous Task for logging in to CloudApp's API
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package io.invaders.android.cloudit.threads;

import io.invaders.android.cloudit.BaseActivity;
import io.invaders.android.cloudit.CloudAppApplication;
import io.invaders.android.cloudit.LoginActivity;
import io.invaders.android.cloudit.R;
import io.invaders.android.cloudit.models.ListItem;

import java.util.ArrayList;
import java.util.Collections;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudapp.api.CloudApp;
import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;

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

	protected StringBuffer doInBackground(String[]... arg0) {

		email = arg0[0][0];
		password = arg0[0][1];
		page = Integer.parseInt(arg0[0][2]);
		
		CloudAppApplication app = (CloudAppApplication) activity.getApplication();
		api = app.createCloudAppApi(email, password);

		this.success = true;

		items = app.getList();
		
		if (page == 1) {
			app.setReachedEnd(false);
		}

		// Add a new bookmark
		try {
			//long totalItems = (int)api.getAccountStats().getItems();
			// yeah, doesnt work :(
			
			int count = 40;
			ArrayList<CloudAppItem> its = (ArrayList<CloudAppItem>) api.getItems(page, count, null, false, null);
			
			ArrayList<CloudAppItem> reversed = new ArrayList<CloudAppItem>(its);
			Collections.reverse(reversed);
			
			boolean theEnd = true;
			
			if (!items.isEmpty()) {
				int counter = 1;
				for (CloudAppItem i : reversed) {
					if (items.size()-counter >= items.size()) {
						theEnd = false;
						break;
					}
					if (!items.get(items.size()-counter).getUrl().equals(i.getUrl())) {
						theEnd = false;
						break;
					}
					counter++;
				}
			}
			else {
				theEnd = false;
			}
			
			if (!theEnd) {
				for (CloudAppItem i : its) {
					items.add(new ListItem(i));
				}
			}
			else {
				app.setReachedEnd();
			}

			app.setList(items);
		} catch (CloudAppException e) {
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
			TextView utext = (TextView) activity.findViewById(R.id.emailField);
			utext.setText(email);
			activity.startActivity(intent);
		}
	}

}
