/**
 * Start
 * 
 * Try guessing what this class does... :)
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package io.invaders.android.cloudit;

import io.invaders.android.cloudit.threads.LoginAsyncTask;
import io.invaders.android.cloudit.tools.EmailValidator;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.cloudapp.api.model.CloudAppItem;
import com.crashlytics.android.Crashlytics;


public class Start extends BaseActivity {

	// TODO clean the cache folder for exports
	
	protected ArrayList<CloudAppItem> items;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        
		setContentView(R.layout.activity_start);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String email = prefs.getString("email", "");
		String password = prefs.getString("password", "");
		
		CloudAppApplication app = (CloudAppApplication) getApplication();
        if (app.restoreList()) {
        	Intent intent = new Intent(this, ListActivity.class);
    	    startActivity(intent);
        }
        else {
        	if (!EmailValidator.isValidEmail(email) || password.equals("")) {
			 	Intent intent = new Intent(this, LoginActivity.class);
		    	startActivity(intent);						
			}
			else {
				ProgressDialog dialog = ProgressDialog.show(Start.this, "", "Loading...", true);
				
				String[] data = { email, password, "1" };
				new LoginAsyncTask(Start.this, dialog).execute(data);
			}
        }
    }
}
