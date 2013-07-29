package com.azusasoft.monogatari.controller;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.azusasoft.monogatari.R;
import com.azusasoft.monogatari.model.Danmaku;

public class DanmakuController {

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
		Log.d("danmaku controller new", String.format("current danmaku count is %d.", mDanmakuCount));
	}
	
	private Danmaku insertDanmaku(String text) {
		Danmaku d = new Danmaku(mContext, text, mDanmakuCount);
		mDanmakuList.add(d);
		mDanmakuStack.push(d);
		mDanmakuCount++;
		return d;
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
		if (mAnimatingDanmaku <= MAX_ANIMATING_DANMAKU && mDanmakuStack.size() != 0) {
			try {
				Danmaku d = mDanmakuStack.pop();
				d.setPaddingTop(
						DANMAKU_PADDING_UNIT * (mAnimatingDanmaku % DANMAKU_MAX_LINE))
						.setAnimationListener(mAnimationListener)
						.setAnimationStartOffset((mAnimatingDanmaku / DANMAKU_MAX_LINE) 
								* (mDanmakuDuration / 2) + interval);
				mDisplayer.addView(d);
				d.start();
				Log.d("danmaku controller new", String.format("danmaku layout width is %d.", d.getLayoutParams().width));
				Log.d("danmaku controller new", String.format("danmaku width is %d.", d.getWidth()));
			} catch (EmptyStackException e) {
				Log.d("danmaku controller", "All danmaku poped");
			}
		}
	}
	
	synchronized public void popNextDanmaku() {
		popNextDanmaku(0);
	}
	
	synchronized public Danmaku newDanmaku(String text) {
		Danmaku d = insertDanmaku(text);
		if (mNewDanmakuListener != null)
			mNewDanmakuListener.newDanmaku(text);
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
	}
	
	AnimationListener mAnimationListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			Log.d("danmaku controller", "danmaku starts");
			mAnimatingDanmaku++;
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			Log.d("danmaku controller", "danmaku ends");
			mAnimatingDanmaku--;
			mEndedDanmakuCount++;
			if (mDanmakuCount != mEndedDanmakuCount)
//				removeAllDanmaku();
//			else
				popNextDanmaku();
//			else
//				handler.removeCallbacks(popDanmaku);
		}
	};
	
}
