/**
 * CustomWebViewClient
 * 
 * 
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp.models;

import android.app.ProgressDialog;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebViewClient extends WebViewClient {

	protected ProgressDialog dialog = null;
	
	public void setProgressDialog(ProgressDialog dialog) {
		this.dialog = dialog;
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		if (dialog != null) dialog.dismiss();
	}
}
