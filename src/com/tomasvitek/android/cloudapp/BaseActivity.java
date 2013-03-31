/**
 * BaseActivity
 * 
 * Implements basic features for all activities
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package com.tomasvitek.android.cloudapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tomasvitek.android.cloudapp.R;
import com.tomasvitek.android.cloudapp.R.anim;
import com.tomasvitek.android.cloudapp.threads.AddBookmarkThread;
import com.tomasvitek.android.cloudapp.threads.FileUploadAsyncTask;
import com.tomasvitek.android.cloudapp.threads.LoginAsyncTask;
import com.tomasvitek.android.cloudapp.tools.EmailValidator;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class BaseActivity extends SherlockActivity implements OnSharedPreferenceChangeListener {
	
	
	static final int MENU_ITEM_ADD = 1;
	static final int MENU_ITEM_REFRESH = 2;
	static final int MENU_ITEM_LOGOUT = 31;
	static final int MENU_ITEM_ABOUT = 32;
	
	static final int DIALOG_ABOUT = 1;
	static final int DIALOG_LOGOUT = 2;
	
	MenuItem addItem;
	MenuItem refreshItem;
	MenuItem logoutItem;
	MenuItem aboutItem;
	
	
	@Override
	    public boolean onCreateOptionsMenu(Menu menu) {

			addItem = menu.add(0, MENU_ITEM_ADD, 0, "Add");
			addItem.setIcon(R.drawable.ic_action_upload);
			addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			 
			refreshItem = menu.add(0, MENU_ITEM_REFRESH, 0, "Refresh");
			refreshItem.setIcon(R.drawable.ic_action_refresh);
		    refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		        
		    SubMenu sub = menu.addSubMenu("Menu");
		        
		    logoutItem = sub.add(0, MENU_ITEM_LOGOUT, 0, "Log out");
		    aboutItem = sub.add(0, MENU_ITEM_ABOUT, 0, "About");
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
			 ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
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
				            	new Thread(new AddBookmarkThread(getApplicationContext(), BaseActivity.this, input1.getText().toString(), input2.getText().toString() )).start();	
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
        }
        else 
    	if (selected == MENU_ITEM_LOGOUT) {
        	showDialog(DIALOG_LOGOUT);
        }
    	else if (selected == MENU_ITEM_ABOUT) {
    		showDialog(DIALOG_ABOUT);
    	}
        else if (selected == MENU_ITEM_REFRESH) {
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    		String email = prefs.getString("email", "");
    		String password = prefs.getString("password", "");
    		
    		if (!EmailValidator.isValidEmail(email) || password.equals("")) {
    			 	Intent intent = new Intent(this, LoginActivity.class);
    		    	startActivity(intent);						
    		}
    		else {
    			 ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    			 NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			     if (networkInfo != null && networkInfo.isConnected()) {
		    			//Toast.makeText(this, "PRESSED!", Toast.LENGTH_LONG).show();
		    			
		    			//ProgressDialog dialog = ProgressDialog.show(BaseActivity.this, "", "Loading...", true);
		    			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        		ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

		        	    Animation rotation = AnimationUtils.loadAnimation(this, anim.rotate);
		        	    rotation.setRepeatCount(Animation.INFINITE);
		        	    iv.startAnimation(rotation);

		        	    refreshItem.setActionView(iv);
		    			
		    			String[] data = {email, password, "1"};
		    			new LoginAsyncTask(BaseActivity.this).execute(data);
		  	     } else {
		  	    	Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show();
		 	     }   			
    		}
        }
        return false;
    } 
	 
    private static final int SELECT_FILE = 2;
    private static final int SELECT_IMAGE = 3;
    
    protected void chooseImage() {
        	Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select image"), SELECT_IMAGE);
	}
    
    protected void chooseFile() {
    	Intent intent = new Intent();
        intent.setType("file/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select file"), SELECT_FILE);
}
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
   	 
        if (resultCode == RESULT_OK) {
 
            if (requestCode == SELECT_FILE)
            {
            	Uri uri = data.getData();
            	
				@SuppressWarnings("unused")
				final ProgressDialog dialog = ProgressDialog.show(BaseActivity.this, "", "Uploading file...", true);
			
				Toast.makeText(this, uri.getPath().toString(), Toast.LENGTH_SHORT);
				
				//String[] path = {getRealPathFromURI(uri)};
				//new FileUploadAsyncTask(BaseActivity.this, dialog).execute(path);
            
            }
            
            if (requestCode == SELECT_IMAGE)
            {
            	Uri uri = data.getData();
            	
				final ProgressDialog dialog = ProgressDialog.show(BaseActivity.this, "", "Uploading image...", true);
			
			    String[] path = {getRealPathFromURI(uri)};
				
				new FileUploadAsyncTask(BaseActivity.this, dialog).execute(path);
            
            }
        }
    }
    
    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver()
                   .query(contentURI, null, null, null, null); 
        cursor.moveToFirst(); 
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
        return cursor.getString(idx); 
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {        
    	super.onCreate(savedInstanceState); 
         
    	setContentView(R.layout.main); 
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.nav_blue_bg);
        bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        getSupportActionBar().setBackgroundDrawable(bg);
        
        }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		//new Thread(new UpdateSettingsThread(getApplicationContext(), new User(getApplicationContext()))).start();		
	}
	
	
	private void logout() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    
		Editor editor = prefs.edit();
	    editor.putString("email", "");
	    editor.putString("password", "");
	    editor.commit();	
	    
	    Intent intent = new Intent(this, LoginActivity.class);
    	startActivity(intent);
	}
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Dialog dialog;
	    switch(id) {
		    case DIALOG_ABOUT:
		    	builder.setTitle("CloudApp for Android")
		    			.setMessage("Bla bla bla bla and bla.")	    			       
		    			.setCancelable(false)
		    			//.setIcon(R.drawable.icon)
		    	        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int id) {
		    	        	   dialog.dismiss();
		    	           }
		    	       });
		    	dialog = builder.create();
		        break;	
	    	case DIALOG_LOGOUT:
		    	builder.setTitle("Log out?")
		    			.setMessage("Are you sure you want to log out?")	    			       
		    			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			    	           public void onClick(DialogInterface dialog, int id) {
			    	        	   logout();
			    	           }
			    	       })
		    		    .setNegativeButton("No", new DialogInterface.OnClickListener() {
			    	           public void onClick(DialogInterface dialog, int id) {
			    	        	   dialog.dismiss();
			    	           }
			    	       });
		    			//.setIcon(R.drawable.icon)
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
}
