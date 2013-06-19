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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.crashlytics.android.Crashlytics;
import com.tomasvitek.android.cloudapp.threads.LoginAsyncTask;
import com.tomasvitek.android.cloudapp.tools.EmailValidator;
import com.tomasvitek.android.cloudapp.tools.URLSpanNoUnderline;

public class LoginActivity extends BaseActivity {

	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Crashlytics.start(this);
		setContentView(R.layout.login);
		getSupportActionBar().hide();
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		Button log = (Button) findViewById(R.id.loginButton);
		TextView signUp = (TextView) findViewById(R.id.signUp);
		Spannable signUpSpan = Spannable.Factory.getInstance().newSpannable(Html.fromHtml("<a href='http://my.cl.ly/register' style='text-decoration:none !important'>Sign up for CloudApp</a>"));
		Spannable processedSignUpText = removeUnderlines(signUpSpan);
		signUp.setText(processedSignUpText);
		signUp.setMovementMethod(LinkMovementMethod.getInstance());
		signUp.setLinkTextColor(Color.WHITE);
		findViewById(R.id.loginLayout).requestFocus();
		log.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					TextView utext = (TextView) findViewById(R.id.emailField);
					TextView ptext = (TextView) findViewById(R.id.passwordField);

					String email = utext.getText().toString();
					String password = ptext.getText().toString();

					if (EmailValidator.isValidEmail(email)) {
						if (!password.equals("")) {

							ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "",
									"Loggin' in...", true);

							String[] data = { email, password, "1" };
							new LoginAsyncTask(LoginActivity.this, dialog).execute(data);
						} else {
							Toast.makeText(LoginActivity.this, "Password cannot be empty!",
									Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(LoginActivity.this, "Your email is not valid!",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(LoginActivity.this, "No internet connection!", Toast.LENGTH_LONG)
							.show();
				}

			}
		});
	}

	private static Spannable removeUnderlines(Spannable p_Text) {  
        URLSpan[] spans = p_Text.getSpans(0, p_Text.length(), URLSpan.class);  
        for (URLSpan span : spans) {  
             int start = p_Text.getSpanStart(span);  
             int end = p_Text.getSpanEnd(span);  
             p_Text.removeSpan(span);  
             span = new URLSpanNoUnderline(span.getURL());  
             p_Text.setSpan(span, start, end, 0);  
        }  
        return p_Text;  
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        moveTaskToBack(true);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
