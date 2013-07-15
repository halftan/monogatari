package com.azusasoft.monogatari;

import android.content.Context;
import android.graphics.Color;
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

public class Danmaku extends TextView {
	// Modified to implement AnimationListener
	// and disappear when animation ends.
	private FrameLayout mRoot;
	private static final int mScrollInterval = 10;
	private int mScrollCount;
	private int mVelocity;
	
	private DanmakuListener mListener;

	public Danmaku(FrameLayout root) {
		this(root, "");
	}
	
	@SuppressWarnings("deprecation")
	public Danmaku(FrameLayout root, String text) {
		super(root.getContext());
		
		mRoot = root;
//		Hard coded text size.
//		TextSize will be retrieved from the server
		setTextSize(20);
//		same as TextSize
		setTextColor(Color.WHITE);
		setHorizontallyScrolling(true);

		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		// Calculate scroll velocity
		float textW = getPaint().measureText(text);
		float viewW = display.getWidth();
		mVelocity = (int) ((viewW + textW) /  getResources().getInteger(R.integer.danmaku_duration_half)) * mScrollInterval;
		// Add spacer before and after text.
		int spacerBefore = (int) ((viewW / getPaint().measureText(" ")) + 1);
		int spacerAfter = (int) ((viewW / textW) + 1);
		
		Log.d("Danmaku info", String.format("TextW: %f\nViewW: %f\nVelocity: %d", textW, viewW, mVelocity));
		
		mScrollCount = (int) ((textW + viewW) / mVelocity + 1);
		setText(strMultiply(spacerBefore) + text + strMultiply(spacerAfter));
		
		// Do not show until start() is called.
		setVisibility(INVISIBLE);
		mRoot.addView(this);
	}
	
	public void setText(String text) {
		setText(text);
	}
	
	private String strMultiply(int c) {
		StringBuilder sb = new StringBuilder(" ");
		for (int i = 0; i < c; ++i)
			sb.append(" ");
		return sb.toString();
	}
	
	public void start() {
		setVisibility(VISIBLE);
	    scrollHandler.sendEmptyMessage(0);
	}
	
	public void setListener(DanmakuListener listener) {
		mListener = listener;
	}
	
	private Handler scrollHandler = new Handler() {
		
		public void handleMessage(Message msg) {
            if (mScrollCount > 0) {
	            scrollBy(mVelocity, 0);
//	            requestLayout();
	            Log.d("Danmaku", "Scrolled to " + getScrollX() + " px");
	            Log.d("Danmaku", "Scroll Count: " + mScrollCount);
	            mScrollCount --;
	        	sendEmptyMessageDelayed(0, mScrollInterval);
            } else {
            	setVisibility(GONE);
            	if (mListener != null)
            		mListener.danmakuEnded();
            }
        }
	};
	
	public interface DanmakuListener {
		void danmakuEnded();
	}
}
