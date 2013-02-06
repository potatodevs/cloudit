/**
 * FileDownloadAsyncTask
 *
 * Asynchronous Task for calling CloudApp's API to download a file
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp.threads;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.cloudapp.api.model.CloudAppItem;
import com.tomasvitek.android.cloudapp.ListActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class FileDownloadAsyncTask extends AsyncTask<String, Integer, String> {

	ProgressDialog dialog;
	String saveUrl;
	ListActivity act;
	CloudAppItem.Type type;
	
	public FileDownloadAsyncTask(ListActivity act, ProgressDialog dialog, String saveUrl, CloudAppItem.Type type) {
		this.act = act;
		this.dialog = dialog;
		this.saveUrl = saveUrl;
		this.type = type;
	}
	
	
	@SuppressLint("WorldReadableFiles")
	@Override
    protected String doInBackground(String... sUrl) {
        try {
            URL url = new URL(sUrl[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            
            OutputStream output = act.openFileOutput(saveUrl, Context.MODE_WORLD_READABLE);
            
            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                publishProgress((int) (total * 100 / fileLength));
                Log.e("progr", String.valueOf(((int) (total * 100 / fileLength))));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
        	e.getStackTrace();
        	Log.e("Downloading", e.toString());
        }
        return null;
    }
	
	 @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.show();
    }
	 
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		dialog.dismiss();
		if (type.equals(CloudAppItem.Type.IMAGE)) act.fileDownloaded(saveUrl, true);
		else act.fileDownloaded(saveUrl, false);
	}

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        Log.e("progress", progress.toString());
        dialog.setProgress(progress[0]);
    }
	
}