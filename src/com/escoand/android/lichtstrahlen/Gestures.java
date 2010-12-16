package com.escoand.android.lichtstrahlen;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * Fling or wipe with the hand to the next or prev Day
 * @author escoand
 */
public class Gestures extends SimpleOnGestureListener {
    private static final int X_MIN_DISTANCE = 120;
    private static final int Y_MAX_DISTANCE = 20;

	private lichtstrahlen context = null;

	public Gestures(lichtstrahlen context) {
		super();
		this.context = context;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,	float velocityY) {
		if(e1.getX() - e2.getX() > X_MIN_DISTANCE && Math.abs(e1.getY() - e2.getY()) < Y_MAX_DISTANCE) {
            context.nextDay();
            return true;
		}
		else if (e2.getX() - e1.getX() > X_MIN_DISTANCE && Math.abs(e1.getY() - e2.getY()) < Y_MAX_DISTANCE) {
            context.prevDay();
            return true;
		}
		return false;
	}

}
