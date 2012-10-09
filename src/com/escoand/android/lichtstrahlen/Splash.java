package com.escoand.android.lichtstrahlen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.escoand.android.lichtstrahlen_2013.R;

public class Splash extends Activity {
	private static final int TIMER_SPLASH = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSharedPreferences(getString(R.string.app_name),
				Context.MODE_PRIVATE).getBoolean("splash", true)) {

			/* show splash */
			setContentView(R.layout.splash);

			/* timer */
			Thread timer = new Thread() {
				public void run() {
					try {
						sleep(TIMER_SPLASH);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						exitSplash();
					}
				}
			};
			timer.start();
		}

		/* no splash */
		else
			exitSplash();
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	private void exitSplash() {
		overridePendingTransition(R.anim.in_right, R.anim.out_left);
		startActivity(new Intent("com.escoand.android.lichtstrahlen.Main"));
	}
}
