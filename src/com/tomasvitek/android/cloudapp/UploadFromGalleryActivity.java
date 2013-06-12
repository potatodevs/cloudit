package com.tomasvitek.android.cloudapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.tomasvitek.android.cloudapp.threads.GalleryFileUploadAsyncTask;

public class UploadFromGalleryActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		 Intent intent = getIntent();
		 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    if (Intent.ACTION_SEND.equals(intent.getAction())) {
		        Bundle extras = intent.getExtras();
		        if (extras.containsKey(Intent.EXTRA_STREAM)) {
		            Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
		            String scheme = uri.getScheme();
		            if (scheme.equals("content")) {
		            	String[] path = {getRealPathFromURI(uri)};
		            	new GalleryFileUploadAsyncTask(UploadFromGalleryActivity.this).execute(path);
		            }
		        }
		    }
	}
	
	private String getRealPathFromURI(Uri contentURI) {
		Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}

}
