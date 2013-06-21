/**
 * SettingsActivity
 * 
 * Settings screen
 *
 * @copyright   Copyright (c) 2013 Tomas Vitek
 * @author      Tomas Vitek
 */

package io.invaders.android.cloudit;

import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.crashlytics.android.Crashlytics;

public class SettingsActivity extends SherlockPreferenceActivity {
 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

		BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.nav_blue_bg);
		bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		getSupportActionBar().setBackgroundDrawable(bg);
		
		addPreferencesFromResource(R.layout.settings);
	}
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}
}
