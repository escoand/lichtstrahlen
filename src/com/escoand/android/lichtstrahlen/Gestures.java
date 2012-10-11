package com.escoand.android.lichtstrahlen;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * Fling or wipe with the hand to the next or prev Day
 * 
 * @author escoand
 */

public class Gestures extends SimpleOnGestureListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private MainActivity context = null;

	public Gestures(MainActivity context) {
		super();
		this.context = context;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
			return false;

		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
			context.nextDay();

		else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
				&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
			context.prevDay();

		return super.onFling(e1, e2, velocityX, velocityY);
	}

}