package com.crudelogics.selfiehd;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.crudelogics.selfiehd.cam.CamFragment;
import com.crudelogics.selfiehd.utils.C;
import com.crudelogics.selfiehd.utils.SpreadUtils;

public class MainActivity extends ActionBarActivity implements C {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		String manufacturer = android.os.Build.MANUFACTURER;

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		if (SpreadUtils.isReviewNeeded(this, 4, true)) {
			SpreadUtils.getReviewDialog(this, null, "Love Selfie HD?",
					R.drawable.ic_launcher, "Please Rate Us on Play Store...").show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.feed:
			SpreadUtils.feedback(getApplicationContext(), MAIL_TO, "Feedback: "
					+ getResources().getString(R.string.app_name) + " "
					+ SpreadUtils.getVersion(this), "");
			return true;

		case R.id.rate:
			SpreadUtils.startReview(this);
			return true;

		case R.id.share:
			SpreadUtils.shareApp(this, "Share via", MSG,
					getResources().getString(R.string.app_name) + " "
							+ SpreadUtils.getVersion(this));
			return true;

		case R.id.more:
			SpreadUtils.getMoreApps(this, PUB);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

	}

	public void snapIt(View v) {
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container, new CamFragment()).addToBackStack(null)
				.commit();
	}

}
