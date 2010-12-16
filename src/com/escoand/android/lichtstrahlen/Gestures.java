package com.escoand.android.lichtstrahlen;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * Fling or wipe with the hand to the next or prev Day
 * @author escoand
 */
public class Gestures extends SimpleOnGestureListener {
    private static final int SWIPE_MIN_DISTANCE = 120;

	private lichtstrahlen context = null;

	public Gestures(lichtstrahlen context) {
		super();
		this.context = context;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,	float velocityY) {
		if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
            context.nextDay();
            return true;
		}
		else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
            context.prevDay();
            return true;
		}
		return false;
	}

}
