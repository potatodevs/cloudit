package com.tomasvitek.android.cloudapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.tomasvitek.android.cloudapp.threads.SharedFileUploadAsyncTask;

public class ShareActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String type = intent.getType();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			if ("text/plain".equals(type)) {
	            handleSendText(intent); // Handle text being sent
	        }
			else {
				Bundle extras = intent.getExtras();
				if (extras.containsKey(Intent.EXTRA_STREAM)) {
					Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
					String scheme = uri.getScheme();
					if (scheme.equals("content")) {
						String[] path = { getRealPathFromURI(uri) };
						new SharedFileUploadAsyncTask(ShareActivity.this).execute(path);
					}
				}
			}
		}
	}
	
	void handleSendText(Intent intent) {
	    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	    if (sharedText != null) {
    		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    		Date today = Calendar.getInstance().getTime();
    		String date = df.format(today);
    		
    		File file = saveToFile(date + "_text.txt", sharedText);
    		
	    	if (file != null) {
				String path = file.getAbsolutePath();
				new SharedFileUploadAsyncTask(ShareActivity.this).execute(path);
	    	}
	    	else {
	    		Toast.makeText(this, "Sorry, it seems that there was an error saving a temporary file. Try again thanks.", Toast.LENGTH_SHORT)
				.show();
	    		finish();
	    	}
	    }
	}

	private String getRealPathFromURI(Uri contentURI) {
		Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}

	public File saveToFile(String filename, String body){
	    try
	    {
	        File root = new File(Environment.getExternalStorageDirectory(), "txts");
	        if (!root.exists()) {
	            root.mkdirs();
	        }
	        File file = new File(root, filename);
	        FileWriter writer = new FileWriter(file);
	        writer.append(body);
	        writer.flush();
	        writer.close();
	        return file;
	    }
	    catch(IOException e) {
	    	return null;
    	}
   } 	
	
}
