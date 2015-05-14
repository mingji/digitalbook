package com.belwadi.sciencefun.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GalleryNavigator extends View {
	private static final int SPACING = 20;
	private static final int RADIUS = 15;
	private int mSize = 5;
	private int mPosition = 0;
	private static final Paint mOnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static final Paint mOffPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public GalleryNavigator(Context context) {
		super(context);
		mOnPaint.setColor(0xffc7e5ef);
		mOffPaint.setColor(0xff90cbe0);
	}

	public GalleryNavigator(Context c, int size) {
		this(c);
		mSize = size;
	}

	public GalleryNavigator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mOnPaint.setColor(0xffc7e5ef);
		mOffPaint.setColor(0xff90cbe0);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < mSize; ++i) {
			if (i == mPosition) {
				canvas.drawCircle(i * (2 * RADIUS + SPACING) + RADIUS, RADIUS,
						RADIUS, mOnPaint);
			} else {
				canvas.drawCircle(i * (2 * RADIUS + SPACING) + RADIUS, RADIUS,
						RADIUS, mOffPaint);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mSize * (2 * RADIUS + SPACING) - SPACING,
				2 * RADIUS);
	}

	public void setPosition(int id) {
		mPosition = id;
	}

	public void setSize(int size) {
		mSize = size;
	}
}
