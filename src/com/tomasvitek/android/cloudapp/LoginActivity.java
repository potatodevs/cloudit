/**
 * LoginActivity
 * 
 * Login form
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.tomasvitek.android.cloudapp.threads.LoginAsyncTask;
import com.tomasvitek.android.cloudapp.tools.EmailValidator;

public class LoginActivity extends BaseActivity {
    
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getSupportActionBar().hide();
        Button log = (Button) findViewById(R.id.loginButton);
        log.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

   			 ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
   			 NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			     if (networkInfo != null && networkInfo.isConnected()) {
						TextView utext = (TextView) findViewById(R.id.emailField);
						TextView ptext = (TextView) findViewById(R.id.passwordField);
						
						String email = utext.getText().toString();
						String password = ptext.getText().toString();
						
						if (EmailValidator.validate(email)) {				
							if (!password.equals("")) {
								
								ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "", "Loggin' in...", true);
								
								String[] data = {email, password, "1"};
								new LoginAsyncTask(LoginActivity.this, dialog).execute(data);						
							}
							else {
								Toast.makeText(LoginActivity.this, "Password cannot be empty!", Toast.LENGTH_LONG).show();
							}							
						}
						else {
							Toast.makeText(LoginActivity.this, "Your email is not valid!", Toast.LENGTH_LONG).show();
						}
		  	     } else {
		  	    	Toast.makeText(LoginActivity.this, "No internet connection!", Toast.LENGTH_LONG).show();
		 	     }
				
			}
		});
    }
    
    
    @Override
    public void onResume() {
    	super.onResume();
    	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String email = prefs.getString("email", "");
		
		if (!email.equals("")) {
	        finish();
		}
    }    
    
    @Override
    public void onBackPressed() {
    	return; // TODO fix
    }
    
}
