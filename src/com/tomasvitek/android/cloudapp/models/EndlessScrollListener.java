package com.tomasvitek.android.cloudapp.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.tomasvitek.android.cloudapp.ListActivity;
import com.tomasvitek.android.cloudapp.R;
import com.tomasvitek.android.cloudapp.R.anim;
import com.tomasvitek.android.cloudapp.threads.LoginAsyncTask;

public class EndlessScrollListener implements OnScrollListener {

	private int currentPage = 1;

	private ListActivity activity;

	public EndlessScrollListener(ListActivity activity, int currentPage) {
		this.activity = activity;
		this.currentPage = currentPage;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		final int lastItem = firstVisibleItem + visibleItemCount;
		if (!activity.loading && lastItem == totalItemCount) {

			ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				// Toast.makeText(this, "PRESSED!", Toast.LENGTH_LONG).show();

				// ProgressDialog dialog =
				// ProgressDialog.show(BaseActivity.this,
				// "", "Loading...", true);
				LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

				Animation rotation = AnimationUtils.loadAnimation(activity, anim.rotate);
				rotation.setRepeatCount(Animation.INFINITE);
				iv.startAnimation(rotation);

				activity.refreshItem.setActionView(iv);

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
				String email = prefs.getString("email", "");
				String password = prefs.getString("password", "");

				Integer page = currentPage + 1;
				String[] data = { email, password, page.toString() };
				new LoginAsyncTask(activity).execute(data);

				activity.loading = true;

				Log.i("RELOADING", "now");
			} else {
				Toast.makeText(activity, "Sorry, can't load anymore items. It appears there is no connection!",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}