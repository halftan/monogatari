package com.azusasoft.monogatari.model;

import java.lang.ref.WeakReference;

import com.azusasoft.monogatari.R;
import com.azusasoft.monogatari.R.integer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
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
	private final ScrollHandler mScrollHandler;

	public Danmaku(FrameLayout root) {
		this(root, "");
	}
	
	/** use the count to set top padding of each danmaku */
	private static int danmakuCount = 0;

	private static Rect bounds = null;
	
	@SuppressWarnings("deprecation")
	public Danmaku(FrameLayout root, String text) {
		super(root.getContext());
		
		mScrollHandler = new ScrollHandler(this);
		mRoot = root;
		danmakuCount++;
//		Hard coded text size.
//		TextSize will be retrieved from the server
		setTextSize(20);
//		same as TextSize
		setTextColor(Color.WHITE);
		setHorizontallyScrolling(true);
		
		text = strMultiply(danmakuCount * 2) + text;

		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		// Calculate scroll velocity
		if (bounds == null) {
			bounds = new Rect();
			getPaint().getTextBounds("A", 0, 1, bounds);
		}
		float textW = getPaint().measureText(text);
		float viewW = display.getWidth();
		mVelocity = (int) ((viewW + textW) /  getResources().getInteger(R.integer.danmaku_duration_half)) * mScrollInterval + 10;
		// Add spacer before and after text.
		int spacerBefore = (int) ((viewW / getPaint().measureText(" ")) + 1);
		int spacerAfter = (int) ((viewW / textW) + 1);
		
		Log.d("Danmaku info", String.format("TextW: %f\nViewW: %f\nVelocity: %d", textW, viewW, mVelocity));
		
		mScrollCount = (int) ((textW + viewW) / mVelocity + 1);
		/** make danmaku not aligned */
		setText(strMultiply(spacerBefore) + text + strMultiply(spacerAfter));
		/** set danmaku top padding */
		setPadding(0, ( 10 + bounds.height() ) * ( danmakuCount - 1 ), 0, 0);
		// Do not show until start() is called.
		setVisibility(INVISIBLE);
		mRoot.addView(this);
	}
	
	public void setText(String text) {
		super.setText(text);
	}
	
	public void start() {
		setVisibility(VISIBLE);
	    mScrollHandler.sendEmptyMessage(0);
	}
	
	public void setListener(DanmakuListener listener) {
		mListener = listener;
	}
			
	public void scroll(Message msg) {
        if (mScrollCount > 0) {
            scrollBy(mVelocity, 0);
//            Log.d("Danmaku", "Scrolled to " + getScrollX() + " px");
//            Log.d("Danmaku", "Scroll Count: " + mScrollCount);
            mScrollCount --;
        	mScrollHandler.sendEmptyMessageDelayed(0, mScrollInterval);
        } else {
        	setVisibility(GONE);
        	danmakuCount--;
        	if (mListener != null)
        		mListener.danmakuEnded();
        }
    }
	
	static private class ScrollHandler extends Handler {
		private final WeakReference<Danmaku> mDanmaku;
		
		ScrollHandler(Danmaku danmaku) {
			mDanmaku = new WeakReference<Danmaku>(danmaku);
		}
		
		@Override
		public void handleMessage(Message msg) {
			Danmaku danmaku = mDanmaku.get();
			if (danmaku != null) {
				danmaku.scroll(msg);
			}
		}
	}
	
	public interface DanmakuListener {
		public void danmakuEnded();
	}
	
	private String strMultiply(int c) {
		StringBuilder sb = new StringBuilder("  ");
		for (;c >= 0; c -= 2)
			sb.append("  ");
		return sb.toString();
	}
}
