package io.invaders.android.cloudit.models;

import io.invaders.android.cloudit.ListActivity;
import io.invaders.android.cloudit.R;
import io.invaders.android.cloudit.R.anim;
import io.invaders.android.cloudit.threads.LoginAsyncTask;
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

public class EndlessScrollListener implements OnScrollListener {

	private int currentPage = 1;
	private ListActivity activity;
	private boolean reachedEnd = false;

	public EndlessScrollListener(ListActivity activity, int currentPage, boolean reachedEnd) {
		this.activity = activity;
		this.currentPage = currentPage;
		this.reachedEnd = reachedEnd;
	}

	public void setCurrentPage(int page) {
		this.currentPage = page;
	}

	public void setReachedEnd() {
		this.setReachedEnd(true);
	}

	public void setReachedEnd(boolean reachedEnd) {
		this.reachedEnd = reachedEnd;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		final int lastItem = firstVisibleItem + visibleItemCount;
		if (!reachedEnd && !activity.loading && lastItem == totalItemCount && totalItemCount > visibleItemCount) {

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

				currentPage++;

				Integer page = currentPage;
				String[] data = { email, password, page.toString() };
				new LoginAsyncTask(activity).execute(data);

				activity.loading = true;

				Log.i("RELOADING", "now");
			} else {
				Toast.makeText(activity, "Sorry, can't load more items. It appears there is no connection!",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}