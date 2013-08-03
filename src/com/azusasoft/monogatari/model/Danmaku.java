package com.azusasoft.monogatari.model;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.azusasoft.monogatari.R;

public class Danmaku extends TextView implements AnimationListener {
	// Modified to implement AnimationListener
	// and disappear when animation ends.
	
	private Animation mAnimation;
	private int mIndex;
	
	private AnimationListener mListener;
	
	public boolean inGumi = false;
	
	public Danmaku(Context context, String text, int index) {
		super(context);
		mIndex = index;
//		Hard coded text size and color
//		TextSize will be retrieved from the server
		setTextSize(20);
		setTextColor(Color.WHITE);
		setSingleLine(true);
		setText(text);
		setGravity(Gravity.LEFT);
		int width = (int) getPaint().measureText(text);
		int height = LayoutParams.WRAP_CONTENT;
		setLayoutParams(new LayoutParams(width, height));
		
		setDanmakuAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right));
		mAnimation.setAnimationListener(this);
		// Do not show until start() is called.
		Log.d("danmaku init", "init succeeded");
		setVisibility(INVISIBLE);
	}
	
	public Danmaku setIndex(int index) {
		mIndex = index;
		return this;
	}
	
	public Danmaku setDanmakuAnimation(Animation animation) {
		mAnimation = animation;
		mAnimation.setAnimationListener(this);
		return this;
	}
	
	public Danmaku setAnimationListener(AnimationListener listener) {
		mListener = listener;
		return this;
	}
	
	public Danmaku setAnimationStartOffset(long startOffset) {
		mAnimation.setStartOffset(startOffset);
		return this;
	}
	
	public Danmaku setPaddingTop(int padding) {
		setPadding(0, padding, 0, 0);
		return this;
	}
	
	public Danmaku setText(String text) {
		super.setText(text);
		int width = (int) getPaint().measureText(text);
		int height = LayoutParams.WRAP_CONTENT;
		setLayoutParams(new LayoutParams(width, height));
		return this;
	}
	
	public void addInterval(long interval) {
		long offset = mAnimation.getStartOffset();
		offset += interval * mIndex;
		mAnimation.setStartOffset(offset);
	}
	
	@Override
	public void onAnimationStart(Animation animation) {
		setVisibility(VISIBLE);
		mListener.onAnimationStart(animation);
	}
	
	@Override
	public void onAnimationRepeat(Animation animation) {
		mListener.onAnimationRepeat(animation);
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
    	setVisibility(GONE);
    	mListener.onAnimationEnd(animation);
	}
	
	public Danmaku start() {
		startAnimation(mAnimation);
		return this;
	}

}
