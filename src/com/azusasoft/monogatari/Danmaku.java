package com.azusasoft.monogatari;

import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Danmaku extends TextView implements AnimationListener {
	// Modified to implement AnimationListener
	// and disappear when animation ends.
	private FrameLayout mRoot;
	
	public Danmaku(FrameLayout root, String text) {
		super(root.getContext());
		
		mRoot = root;
		LayoutParams lParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		setLayoutParams(lParams);
		setText(text);
		// Hard coded text size.
		// TextSize will be retrieved from the server
		setTextSize(20);
		// same as TextSize
		setTextColor(Color.WHITE);
		setSingleLine(true);
		
		mRoot.addView(this);
		// Load animation from XML and set listener
		// in order to remove it from mRoot
		Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.danmaku);
		animation.setAnimationListener(this);
		startAnimation(animation);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		mRoot.removeView(this);
		CameraTestActivity.animationEnded = true;
		Log.d("Danmaku", "Remove view");
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public void onAnimationStart(Animation animation) {
		CameraTestActivity.animationEnded = false;
	}
}
