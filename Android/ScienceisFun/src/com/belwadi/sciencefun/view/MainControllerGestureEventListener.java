package com.belwadi.sciencefun.view;

import java.util.EventListener;

public interface MainControllerGestureEventListener extends EventListener {
	public void onSwipeRight();
	public void onSwipeLeft();
	public void onSwipeTop();
	public void onSwipeBottom();
	public void tapUp();
	public void onScroll(float fromX, float fromY, float toX, float toY, float movedInX, float movedInY);
}