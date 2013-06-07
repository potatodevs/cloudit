/**
 * WebViewActivity
 * 
 * Loads and displays files in WebView
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp;

import com.crashlytics.android.Crashlytics;
import com.tomasvitek.android.cloudapp.models.CustomWebViewClient;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

public class WebViewActivity extends BaseActivity {
	private WebView webView;
	 
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
        Crashlytics.start(this);
		setContentView(R.layout.webview);
 
		CloudAppApplication app = (CloudAppApplication) getApplication();
		
		ProgressDialog dialog = ProgressDialog.show(WebViewActivity.this, "", "Loading...", true);
		
		webView = (WebView) findViewById(R.id.webView1);
		
		CustomWebViewClient client = new CustomWebViewClient();
		client.setProgressDialog(dialog);
		
		webView.setWebViewClient(client);
		webView.getSettings().setSupportZoom(true);
		
		//webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(app.getURLToOpen());
 
	}
}
