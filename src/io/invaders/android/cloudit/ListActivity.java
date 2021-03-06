/**
 * ListActivity
 * 
 * Main activity with a list of all items
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package io.invaders.android.cloudit;

import io.invaders.android.cloudit.models.Adapter;
import io.invaders.android.cloudit.models.EndlessScrollListener;
import io.invaders.android.cloudit.models.ListItem;
import io.invaders.android.cloudit.threads.FileDeleteAsyncTask;
import io.invaders.android.cloudit.threads.FileDownloadAsyncTask;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudapp.api.CloudAppException;
import com.cloudapp.api.model.CloudAppItem;
import com.crashlytics.android.Crashlytics;

public class ListActivity extends BaseActivity {

	private Adapter adapter;

	ArrayList<ListItem> items;
	
	EndlessScrollListener scrollListener;

	BroadcastReceiver logOut;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

		setContentView(R.layout.list);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("ACTION_LOGOUT");
		logOut = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("onReceive", "Logout in progress");
				Intent Loginintent = new Intent(getApplicationContext(), LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY
						| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				startActivity(Loginintent);
				finish();
			}
		};
		registerReceiver(logOut, intentFilter);

		final ListView list = (ListView) findViewById(android.R.id.list);

		registerForContextMenu(list);

		CloudAppApplication app = (CloudAppApplication) getApplication();
		items = app.getList();
		
		TextView empty = (TextView) findViewById(android.R.id.empty);
		if (!items.isEmpty()) {
			empty.setVisibility(View.GONE);
		}
		else {
			empty.setVisibility(View.VISIBLE);
		}

		scrollListener = new EndlessScrollListener(ListActivity.this, 1, app.hasReachedEnd());
		
		list.setOnScrollListener(scrollListener);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// ListActivity.this.openContextMenu(parent);
				// view.showContextMenu();
				CloudAppItem item = (CloudAppItem) list.getItemAtPosition(position);
				try {
					// if (!item.getItemType().equals(CloudAppItem.Type.IMAGE))
					// {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getContentUrl()));
					startActivity(browserIntent);
					/*
					 * } else { CloudAppApplication app = (CloudAppApplication)
					 * getApplication(); app.setURLToOpen(item.getContentUrl());
					 * Intent intent = new Intent(ListActivity.this,
					 * WebViewActivity.class); startActivity(intent); }
					 */
				} catch (CloudAppException e) {
					e.printStackTrace();
				}
			}
		});

		adapter = new Adapter(ListActivity.this, items);

		list.setAdapter(adapter);

		loading = false;
		
		setRefreshAfterStart();
	}
	
	void handleSendImage(Intent intent) {
	    Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
	    if (imageUri != null) {
	        // Update UI to reflect image being shared
	    }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setRefreshAfterStart();
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.item_menu, menu);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final ListItem i = items.get(info.position);
		try {
			menu.setHeaderTitle(i.getName());
		} catch (CloudAppException e) {
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(logOut);
		super.onDestroy();
	}

	@SuppressLint({ "ServiceCast", "NewApi" })
	@SuppressWarnings("deprecation")
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}

		final ListItem i = items.get(info.position);
		final String name = i.name;
		final String url = i.contentUrl;
		final String inDirectURL = i.url;
		final CloudAppItem.Type type = i.getItemType();

		switch (item.getItemId()) {
		case R.id.copylink:
			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(inDirectURL);
			} else {
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData.newPlainText(name + "'s url", inDirectURL);
				clipboard.setPrimaryClip(clip);
			}
			Toast.makeText(this, "Link for '" + name + "' has been copied into your clipboard.", Toast.LENGTH_SHORT)
					.show();
			return true;
		case R.id.share:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle("What to share?");
			builder.setMessage("Do you want to share the file or just a link to it?");

			builder.setPositiveButton("File", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
					if (networkInfo != null && networkInfo.isConnected()) {
						ProgressDialog mProgressDialog;

						// instantiate it within the onCreate method
						mProgressDialog = new ProgressDialog(ListActivity.this);
						mProgressDialog.setMessage("Downloading the file for sharing...");
						mProgressDialog.setIndeterminate(false);
						mProgressDialog.setMax(100);
						mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

						String where = name;

						FileDownloadAsyncTask downloadFile = new FileDownloadAsyncTask(ListActivity.this,
								mProgressDialog, where, type);
						downloadFile.execute(url);
					} else {
						Toast.makeText(ListActivity.this, "No internet connection!", Toast.LENGTH_LONG).show();
					}

					dialog.dismiss();
				}

			});

			builder.setNegativeButton("Link", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					shareIntent.setType("text/plain");
					shareIntent.putExtra(Intent.EXTRA_TEXT, url);
					startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

					dialog.dismiss();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
			return true;
		case R.id.delete:
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("Delete?").setMessage("Are you sure you want to delete this item?")
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface d, int id) {
							d.dismiss();
						}
					}).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface d, int id) {
							final ProgressDialog dialog = ProgressDialog.show(ListActivity.this, "",
									"Deleting file...", true);
							FileDeleteAsyncTask del = new FileDeleteAsyncTask(ListActivity.this, dialog);
							del.execute(i);
						}
					}).show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void fileDownloaded(String where, boolean isImage) {
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		Uri uri = Uri.fromFile(getFileStreamPath(where));
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		if (isImage)
			shareIntent.setType("image/*");
		else
			shareIntent.setType("*/*");
		startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
	}

	public void taskDone(String email, String password) {
		ListView list = (ListView) findViewById(android.R.id.list);

		int index = list.getFirstVisiblePosition();
		View v = list.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();

		CloudAppApplication app = (CloudAppApplication) getApplication();
		items = app.getList();
		adapter = new Adapter(ListActivity.this, items);
		list.setAdapter(adapter);
		
		TextView empty = (TextView) findViewById(android.R.id.empty);
		if (!items.isEmpty()) {
			empty.setVisibility(View.GONE);
		}
		else {
			empty.setVisibility(View.VISIBLE);
		}
		
		scrollListener.setReachedEnd(app.hasReachedEnd());
		list.setOnScrollListener(scrollListener);
		
		registerForContextMenu(list);

		loading = false;

		list.setSelectionFromTop(index, top);

		if (refreshItem.getActionView() != null) {
			if (refreshItem.getActionView().getAnimation() != null) {
				refreshItem.getActionView().clearAnimation();
				refreshItem.setActionView(null);
			}
		}
	}

}
