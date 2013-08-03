package com.azusasoft.monogatari.controller;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import org.springframework.util.StringUtils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.azusasoft.monogatari.R;
import com.azusasoft.monogatari.model.Danmaku;

public class DanmakuController {
	private static final String TAG = "danmaku controller";

	/** 同屏弹幕最大数目 */
	private static final int DANMAKU_MAX_LINE = 10;
	/** 每行弹幕高度 */
	private static final int DANMAKU_PADDING_UNIT = 50;
	/** 最大滚动弹幕数 */
	private static final int MAX_ANIMATING_DANMAKU = 10;
	/** 弹幕显示间隔 */
	private static final int POP_DANMAKU_INTERVAL = 200;
	
	private int mDanmakuCount;
	private int mEndedDanmakuCount;
	private int mAnimatingDanmaku;
	
	private Stack<Danmaku> mDanmakuStack;	
	private ArrayList<Danmaku> mDanmakuList;
	
	private Context mContext;
	private ViewGroup mDisplayer;
	
	private int mDanmakuDuration;
	
	private static DanmakuController mDanmakuController = null;
	
	private boolean mCallbackRemoved = false;
	
	private DanmakuController() {};
	
	public static DanmakuController init(Context context, ViewGroup displayer) {
		mDanmakuController = new DanmakuController();
		
		mDanmakuController.mContext = context;
		mDanmakuController.mDisplayer = displayer;
		mDanmakuController.mDanmakuCount = 0;
		mDanmakuController.mAnimatingDanmaku = 0;
		mDanmakuController.mDanmakuList = new ArrayList<Danmaku>();
		mDanmakuController.mDanmakuStack = new Stack<Danmaku>();
		mDanmakuController.mDanmakuDuration = context.getResources().getInteger(R.integer.danmaku_duration);
		return mDanmakuController;
	}
	
	public static DanmakuController getInstance() throws RuntimeException {
		if (mDanmakuController != null)
			return mDanmakuController;
		else throw new RuntimeException("Danmaku Controller haven't been initialized!");
	}
	
	public void loadDanmaku(List<String> texts) {
		mDanmakuList = new ArrayList<Danmaku>();
		mDanmakuStack = new Stack<Danmaku>();
		mEndedDanmakuCount = 0;
		mDanmakuCount = 0;
		mAnimatingDanmaku = 0;
		for (String text : texts) {
			insertDanmaku(text);
		}
		Log.d(TAG, String.format("current danmaku count is %d.", mDanmakuCount));
	}
	
	private List<Danmaku> insertDanmaku(String text) {
		String[] texts = StringUtils.split(text, "\n");
		List<Danmaku> retDanmaku = new ArrayList<Danmaku>();
		if (texts != null)
			for (String elem : texts) {
				Danmaku d = new Danmaku(mContext, elem, mDanmakuCount);
				d.inGumi = true;
				mDanmakuList.add(d);
				mDanmakuStack.push(d);
				mDanmakuCount++;
				retDanmaku.add(d);
			}
		else {
			Danmaku d = new Danmaku(mContext, text, mDanmakuCount);
			mDanmakuList.add(d);
			mDanmakuStack.push(d);
			mDanmakuCount++;
			retDanmaku.add(d);
		}
		return retDanmaku;
	}
	
	public Runnable spwanAllDanmaku = new Runnable() {
		
		@Override
		public void run() {
			mDisplayer.removeAllViews();
			mDisplayer.bringToFront();
			handler.post(popDanmaku);
		}
	};
	
	public void spawnDanmaku() {
		popNextDanmaku(0);
	}
	
	private Handler handler = new Handler();
	
	private Runnable popDanmaku = new Runnable() {
		
		@Override
		public void run() {
			popNextDanmaku(0);
			mDisplayer.invalidate();
			handler.postDelayed(popDanmaku, POP_DANMAKU_INTERVAL);
		}
	};
	
	synchronized public void popNextDanmaku(long interval) {
		if (mAnimatingDanmaku <= MAX_ANIMATING_DANMAKU && !mDanmakuStack.isEmpty()) {
			try {
				Danmaku d = mDanmakuStack.pop();
				if (d.inGumi) {
					Stack<Danmaku> gumi = new Stack<Danmaku>();
					gumi.push(d);
					while (!mDanmakuStack.isEmpty() && mDanmakuStack.lastElement().inGumi)
						gumi.push(mDanmakuStack.pop());
					// Use FILO to make the order of danmaku right
					int l = gumi.size();
					for (int i = 0; i < l; ++i) {
						gumi.get(i).setPaddingTop(
							DANMAKU_PADDING_UNIT * ((l-i-1) % DANMAKU_MAX_LINE))
							.setAnimationListener(mAnimationListener);
					}
					while (!gumi.isEmpty()) {
						mDisplayer.addView(gumi.pop().start());
					}
				} else {
					d.setPaddingTop(
							DANMAKU_PADDING_UNIT * (mAnimatingDanmaku % DANMAKU_MAX_LINE))
							.setAnimationListener(mAnimationListener)
							.setAnimationStartOffset((mAnimatingDanmaku / DANMAKU_MAX_LINE) 
									* (mDanmakuDuration / 2) + interval);
					mDisplayer.addView(d.start());
				}
			} catch (EmptyStackException e) {
				Log.d(TAG, "All danmaku poped");
			}
		}
	}
	
	synchronized public void popNextDanmaku() {
		popNextDanmaku(0);
	}
	
	synchronized public List<Danmaku> newDanmaku(String text, boolean shouldFireCallback) {
		List<Danmaku> d = insertDanmaku(text);
		if (shouldFireCallback && mNewDanmakuListener != null)
			mNewDanmakuListener.newDanmaku(text);
		if (mCallbackRemoved) {
			mCallbackRemoved = false;
			handler.post(popDanmaku);
		}
		return d;
	}
	
	private NewDanmakuListener mNewDanmakuListener;
	public interface NewDanmakuListener {
		public void newDanmaku(String text);
	}
	
	public void setNewDanmakuListener(NewDanmakuListener listener) {
		mNewDanmakuListener = listener;
	}
	
	public void removeAllDanmaku() {
		mDanmakuList = null;
		mDanmakuStack = null;
		mDisplayer.removeAllViews();
		mEndedDanmakuCount = 0;
		mDanmakuCount = 0;
		mAnimatingDanmaku = 0;
	}
	
	AnimationListener mAnimationListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			Log.d(TAG, "danmaku starts");
			mAnimatingDanmaku++;
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			Log.d(TAG, "danmaku ends");
			mAnimatingDanmaku--;
			mEndedDanmakuCount++;
			if (mDanmakuCount != mEndedDanmakuCount)
				popNextDanmaku(0);
			else {
				handler.removeCallbacks(popDanmaku);
				mCallbackRemoved = true;
				Log.i(TAG, "removed popdanmaku callback");
			}
		}
	};
	
	private synchronized void decreaseAnimatiogDanmaku() {
		mAnimatingDanmaku--;
	}
	
	private synchronized void increaseAnimatingDanmaku() {
		mAnimatingDanmaku++;
	}
	
	private synchronized void increaseEndedDanmaku() {
		mEndedDanmakuCount++;
	}
	
}
