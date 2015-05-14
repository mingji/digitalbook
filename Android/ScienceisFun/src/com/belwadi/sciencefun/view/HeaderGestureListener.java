package com.belwadi.sciencefun.view;

import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;

public class HeaderGestureListener implements OnGestureListener {    
    private MainControllerGestureEventListener gestureEventListener;
	private static final int SWIPE_THRESHOLD = 150;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
	public HeaderGestureListener(MainControllerGestureEventListener gestureEventListener) {
		this.gestureEventListener = gestureEventListener;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)
                    if (diffX > 0)
                        onSwipeRight();
                    else
                        onSwipeLeft();
            }
            else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD)
                    if (diffY > 0)
                        onSwipeBottom();
                    else
                        onSwipeTop();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }
    @Override
    public void onLongPress(MotionEvent e) {}
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	gestureEventListener.onScroll(e1.getX(), e1.getY(), e2.getX(), e2.getY(), distanceX, distanceY);
    	return true;
    }
    @Override
    public void onShowPress(MotionEvent e) {}    
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
    	gestureEventListener.tapUp();
        return true;
    }
    public void onSwipeRight() {
    	gestureEventListener.onSwipeRight();
    }
    public void onSwipeLeft() {
    	gestureEventListener.onSwipeLeft();
    }
    public void onSwipeTop() {
    	gestureEventListener.onSwipeTop();
    }
    public void onSwipeBottom() {
    	gestureEventListener.onSwipeBottom();
    }
}
