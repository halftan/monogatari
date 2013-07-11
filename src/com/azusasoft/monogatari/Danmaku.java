package com.azusasoft.monogatari;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Danmaku extends TextView implements AnimationListener {
	// Modified to implement AnimationListener
	// and disappear when animation ends.
	private FrameLayout mRoot;
//	private Paint mPaint = null;
	private Animation mSlideIn;
	private Animation mSlideOut;
	private static final int mScrollInterval = 20;
	private int mScrollCount;
	private int mScrollAmountMid;
//	private int mScrollAmountOut;
	private float mVelocity;
	
//	final float densityMultiplier = getContext().getResources().getDisplayMetrics().density;
//	final float scaledPx = 20 * densityMultiplier;
//	private Rect bounds;
	
	public static final int SCROLL_PIXELS = 5;
	private static int mScrollDuration = -1;
		
	@SuppressWarnings("deprecation")
	public Danmaku(FrameLayout root, String text) {
		super(root.getContext());
		
		mRoot = root;
		setText(text);
		// Hard coded text size.
		// TextSize will be retrieved from the server
		setTextSize(20);
		// same as TextSize
		setTextColor(Color.WHITE);
		setHorizontallyScrolling(true);
		mSlideIn = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
		mSlideOut = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right);
		mSlideIn.setAnimationListener(this);
		mSlideOut.setAnimationListener(this);

		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		// TODO: Optimization must!
		mVelocity = (float) display.getWidth() / (float) getResources().getInteger(R.integer.danmaku_duration_half);
		// Calculate scroll interval
		float textW = getPaint().measureText(text);
		float viewW = display.getWidth();
		Log.d("Danmaku info", String.format("TextW: %f\nViewW: %f\nVelocity: %f", textW, viewW, mVelocity));
		if (textW < viewW) {
			// No need to scroll inner text.
			mScrollCount = 0;
		} else {
			mScrollCount = (int) (((textW - viewW)) / (mVelocity * mScrollInterval));
			if (mScrollCount == 0)
				mScrollCount ++;
			mScrollAmountMid = (int) (mVelocity * mScrollInterval * 2);
		}
//		mScrollAmountOut = (int) (textW / (mVelocity * mScrollInterval)) * 18;
//		mSlideOut.setDuration(mScrollAmountOut);
//		Log.d("Danmaku info", String.format("mScrollCount: %d\nmScrollAmountMid: 
//		%d\nmScrollAmountOut: %d", mScrollCount, mScrollAmountMid, mScrollAmountOut));
		// Do not show until start() is called.
		setVisibility(INVISIBLE);
		mRoot.addView(this);
	}
	
	public int getScrollDuration() {
		if (mScrollDuration <= 0)
			mScrollDuration = getContext().getResources()
					.getInteger(R.integer.danmaku_duration);
		return mScrollDuration;
	}
	
	public void start() {
		setVisibility(VISIBLE);
		mState = AnimationState.IN;
		startAnimation(mSlideIn);
	}
	
	public void startScrolling() {
        scrollHandler.sendEmptyMessage(0);
    }
	
	private Handler scrollHandler = new Handler() {
		
		public void handleMessage(Message msg) {
            if (mScrollCount > 0) {
	            scrollBy(mScrollAmountMid, 0);
//	            requestLayout();
	            Log.d("Danmaku", "Scrolled to " + getScrollX() + " px");
	            Log.d("Danmaku", "Scroll Count: " + mScrollCount);
	            mScrollCount --;
	        	sendEmptyMessageDelayed(0, mScrollInterval);
            } else {
            	mState = AnimationState.OUT;
            	startAnimation(mSlideOut);
            }
        }
	};
	
	private enum AnimationState {
		IN, MID, OUT
	}
	
	private AnimationState mState = AnimationState.IN;
	
	@Override
	public void onAnimationEnd(Animation animation) {
		switch (mState) {
		case IN:
			mState = AnimationState.MID;
			startScrolling();
			break;
		case MID:
			mState = AnimationState.OUT;
			break;
		case OUT:
			mRoot.removeView(this);
			CameraTestActivity.animationEnded = true;			
			mScrollCount = 0;
			Log.d("Danmaku", "Remove view");
			break;
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public void onAnimationStart(Animation animation) {
		CameraTestActivity.animationEnded = false;
		Log.d("Danmaku", "Started animation");
	}
}
