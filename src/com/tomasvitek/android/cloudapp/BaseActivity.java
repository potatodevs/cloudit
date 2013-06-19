/**
 * BaseActivity
 * 
 * Implements basic features for all activities
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp;

import java.io.File;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.beanie.imagechooser.api.ChooserType;
import com.beanie.imagechooser.api.ChosenImage;
import com.beanie.imagechooser.api.ImageChooserListener;
import com.beanie.imagechooser.api.ImageChooserManager;
import com.crashlytics.android.Crashlytics;
import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.tomasvitek.android.cloudapp.R.anim;
import com.tomasvitek.android.cloudapp.threads.AddBookmarkThread;
import com.tomasvitek.android.cloudapp.threads.FileUploadAsyncTask;
import com.tomasvitek.android.cloudapp.threads.LoginAsyncTask;
import com.tomasvitek.android.cloudapp.tools.EmailValidator;

public class BaseActivity extends SherlockActivity implements OnSharedPreferenceChangeListener, ImageChooserListener {

	static final int MENU_ITEM_ADD = 1;
	static final int MENU_ITEM_REFRESH = 2;
	static final int MENU_ITEM_LOGOUT = 31;
	static final int MENU_ITEM_ABOUT = 32;
	static final int MENU_ITEM_SETTINGS = 33;
	

	static final int DIALOG_ABOUT = 1;
	static final int DIALOG_LOGOUT = 2;
	static final int DIALOG_SETTINGS = 3;

	private MenuItem addItem;
	public MenuItem refreshItem; // needs to be public, to that the animation can be started from outside
	private MenuItem logoutItem;
	private MenuItem settingsItem;
	private MenuItem aboutItem;
	private ShareActionProvider mShareActionProvider;
	private ImageChooserManager icm;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		addItem = menu.add(0, MENU_ITEM_ADD, 0, "Add");
		addItem.setIcon(R.drawable.ic_action_upload);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		refreshItem = menu.add(0, MENU_ITEM_REFRESH, 0, "Refresh");
		refreshItem.setIcon(R.drawable.ic_action_refresh);
		refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		SubMenu sub = menu.addSubMenu("Menu");

		aboutItem = sub.add(0, MENU_ITEM_ABOUT, 0, "About");
		settingsItem = sub.add(0, MENU_ITEM_SETTINGS, 1, "Settings");
		logoutItem = sub.add(0, MENU_ITEM_LOGOUT, 2, "Log out");
		
		sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		sub.getItem().setIcon(R.drawable.ic_action_overflow);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
			return false;
		}
		int selected = item.getItemId();

		if (selected == MENU_ITEM_ADD) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				builder.setTitle("What to add?");
				builder.setMessage("Do you want to upload a file or just add a bookmark?");

				builder.setPositiveButton("Bookmark", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						LayoutInflater factory = LayoutInflater.from(BaseActivity.this);
						final View textEntryView = factory.inflate(R.layout.add_dialog, null);

						AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);

						alert.setTitle("Add a bookmark");
						alert.setMessage("Enter name and URL.");
						// Set an EditText view to get user input
						alert.setView(textEntryView);

						final EditText input1 = (EditText) textEntryView.findViewById(R.id.title);
						final EditText input2 = (EditText) textEntryView.findViewById(R.id.url);
						alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String urlInput;
								if (input2.getText().toString().toLowerCase(Locale.getDefault())
										.contains("http://".toLowerCase(Locale.getDefault())) == false) {
									urlInput = "http://" + input2.getText().toString().toLowerCase(Locale.getDefault());
								} else {
									urlInput = input2.getText().toString().toLowerCase(Locale.getDefault());
								}
								Thread bookmarkThread = new Thread(new AddBookmarkThread(getApplicationContext(),
										BaseActivity.this, input1.getText().toString(), urlInput));
								bookmarkThread.start();
								try {
									bookmarkThread.join();
								} catch (InterruptedException e) {
									e.printStackTrace();
								} finally {
									refresh();
								}
							}
						});

						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Canceled.
							}
						});

						alert.show();
					}

				});

				builder.setNegativeButton("Upload an image", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						BaseActivity.this.chooseImage();
					}
				});

				builder.setNeutralButton("Upload a file", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						BaseActivity.this.chooseFile();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();

			} else {
				Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show();
			}
		} else if (selected == MENU_ITEM_LOGOUT) {
			showDialog(DIALOG_LOGOUT);
		} else if (selected == MENU_ITEM_ABOUT) {
			showDialog(DIALOG_ABOUT);
		} else if (selected == MENU_ITEM_SETTINGS) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		} else if (selected == MENU_ITEM_REFRESH) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String email = prefs.getString("email", "");
			String password = prefs.getString("password", "");

			if (!EmailValidator.isValidEmail(email) || password.equals("")) {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
			} else {
				refresh();
			}
		}
		return false;
	}

	private static final int SELECT_FILE = 2;
	//private static final int SELECT_IMAGE = 3;

	protected void chooseImage() {
		icm.setImageChooserListener(this);
		icm.choose();
	}

	protected void chooseFile() {
		Intent intent = new Intent(BaseActivity.this, FileChooserActivity.class);
		intent.setType("*/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, SELECT_FILE);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {

			if (requestCode == SELECT_FILE) {
				Uri uri = data.getData();
				//Toast.makeText(this, uri.getPath().toString(), Toast.LENGTH_SHORT).show();
				File file = FileUtils.getFile(uri);
				String path = file.getAbsolutePath();
				new FileUploadAsyncTask(BaseActivity.this).execute(path);

			}

			if (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE) {
				icm.submit(requestCode, data);

			}
		}
	}

	/*private String getRealPathFromURI(Uri contentURI) {
		Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

		setContentView(R.layout.main);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.nav_blue_bg);
		bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		getSupportActionBar().setBackgroundDrawable(bg);
		icm = new ImageChooserManager(this, ChooserType.REQUEST_PICK_PICTURE);

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// new Thread(new UpdateSettingsThread(getApplicationContext(), new
		// User(getApplicationContext()))).start();
	}

	private void logout() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Editor editor = prefs.edit();
		editor.putString("email", "");
		editor.putString("password", "");
		editor.commit();
		
		CloudAppApplication app = (CloudAppApplication) getApplication();
		app.clearCachedList();

		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog dialog;
		switch (id) {
		case DIALOG_ABOUT:
			builder.setTitle("CloudApp for Android")
					.setMessage(
							"Manage and share your CloudApp drops right from your Android device. \nMade by Invaders.")
					.setCancelable(false)
					// .setIcon(R.drawable.icon)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			dialog = builder.create();
			dialog.show();
			dialog.getWindow().getAttributes();
			TextView textView = (TextView) dialog.findViewById(android.R.id.message);
			textView.setTextSize(15);
			break;
		case DIALOG_LOGOUT:
			builder.setTitle("Log out?").setMessage("Are you sure you want to log out?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							logout();
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			// .setIcon(R.drawable.icon)
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	public void taskDone(String email, String password) {
		Toast.makeText(this, "Welcome!", Toast.LENGTH_LONG).show();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Editor editor = prefs.edit();
		editor.putString("email", email);
		editor.putString("password", password);
		editor.commit();

		Intent intent = new Intent(this, ListActivity.class);
		startActivity(intent);

	}

	public void refresh() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// Toast.makeText(this, "PRESSED!", Toast.LENGTH_LONG).show();

			// ProgressDialog dialog = ProgressDialog.show(BaseActivity.this,
			// "", "Loading...", true);
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

			Animation rotation = AnimationUtils.loadAnimation(this, anim.rotate);
			rotation.setRepeatCount(Animation.INFINITE);
			iv.startAnimation(rotation);

			refreshItem.setActionView(iv);
			
			CloudAppApplication app = (CloudAppApplication) getApplication();
			
			app.clearList();

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String email = prefs.getString("email", "");
			String password = prefs.getString("password", "");
			
			String[] data = { email, password, "1" };
			new LoginAsyncTask(BaseActivity.this).execute(data);
		} else {
			Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onImageChosen(final ChosenImage image) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (image != null) {
					// Use the image
					// image.getFilePathOriginal();
					// image.getFileThumbnail();
					// image.getFileThumbnailSmall();
					new FileUploadAsyncTask(BaseActivity.this).execute(image.getFilePathOriginal());
				}
			}
		});
	}

	@Override
	public void onError(final String reason) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(BaseActivity.this, "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
			}
		});
	}
}
