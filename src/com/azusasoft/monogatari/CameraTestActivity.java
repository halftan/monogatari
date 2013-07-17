/*
 * Basic no frills app which integrates the ZBar barcode scanner with
 * the camera.
 * 
 * Created by lisah0 on 2012-02-24
 */
package com.azusasoft.monogatari;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.azusasoft.monogatari.controller.MessageController;
import com.azusasoft.monogatari.model.Danmaku;

public class CameraTestActivity extends FragmentActivity {
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	private String mFocusMode;
	static private CameraTestActivity mMainActivity;

	FrameLayout mFrameLayout;
	ImageScanner scanner;

	private boolean barcodeScanned = false;
	private boolean previewing = true;

	public DanmakuHandler danmakuHandler;
	public MessageController messageController;
	
	public static boolean animationEnded = true;

	static {
		System.loadLibrary("iconv");
	}
	
	static public CameraTestActivity getMainActivity() {
		return mMainActivity;
	}

	@SuppressLint("InlinedApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mMainActivity = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mCamera = getCameraInstance();
		autoFocusHandler = new Handler();

		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);

    	mFocusMode = null;
    	String cameraFeature = Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
    	if (Build.VERSION.SDK_INT >= 14) {
    		cameraFeature = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
    	}
        Parameters parameters = mCamera.getParameters();
        for (String f : parameters.getSupportedFocusModes())
            if (f == cameraFeature)
                mFocusMode = f;
        
		if (mFocusMode != null)
			mPreview = new CameraPreview(this, mCamera, previewCb, null, mFocusMode);
		else 
			mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB, null);

		mFrameLayout = (FrameLayout) findViewById(R.id.cameraPreview);
		danmakuHandler = new DanmakuHandler(mFrameLayout);
		messageController = new MessageController(danmakuHandler,
				new MessageController.onNoDanmakuListener() {
			
			@Override
			public void pushFirstDanmaku(String mono) {
				NewDanmakuDialog dialog = new NewDanmakuDialog();
				Bundle args = new Bundle();
				args.putString(DanmakuHandler.DANMAKU_MONO_KEY, mono);
				dialog.setArguments(args);
				dialog.show(getSupportFragmentManager(), "NewDanmakuDialogFragment");				
			}
		});
		
		init();
	}
	
	private void init() {
		if (mCamera == null) {
			mCamera = getCameraInstance();
			if (mFocusMode != null)
				mPreview = new CameraPreview(this, mCamera, previewCb, null, mFocusMode);
			else 
				mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB, null);
		}
		
		FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
		preview.addView(mPreview);
				
		mFrameLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (barcodeScanned && animationEnded) {
					barcodeScanned = false;
					mCamera.setPreviewCallback(previewCb);
					mCamera.startPreview();
					previewing = true;
					if (mFocusMode != null) {
						Parameters params = mCamera.getParameters();
						params.setFocusMode(mFocusMode);
						mCamera.setParameters(params);
					} else
						mCamera.autoFocus(autoFocusCB);
				} else if (mCamera == null) {
					init();
					previewing = true;
				}
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		releaseCamera();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};
	
	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				SymbolSet syms = scanner.getResults();
				StringBuilder results = new StringBuilder();
				for (Symbol sym : syms) {
					results.append(sym.getData());
					barcodeScanned = true;
				}
				messageController.searchDanmakuFor(results.toString());
			}
		}
	};

	// Mimic continuous auto-focusing
	// Not used when SDK_INT >= 14
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
	
	public void pushDanmaku(String text) {
		messageController.pushDanmaku(text);
	}
	
	public static class DanmakuHandler extends Handler {
		public static final int NEW_DANMAKU = 0xAA;
		public static final int NEW_AND_POST_DANMAKU = 0xAB;
		public static final int PAUSE_DANMAKU = 0xBB;
		public static final int DISPLAY_DANMAKU = 0xCC;
		
		public static final int DanmakuDelay = 400;

		// The content of danmaku
		public static final String DANMAKU_TEXT_KEY = "danmaku text";
		// The target's barcode
		public static final String DANMAKU_MONO_KEY = "danmaku target";
		public static final String DANMAKU_TEXT_LIST_KEY = "danmaku list";
		
		private final WeakReference<FrameLayout> mDisplayLayer;
		
		public DanmakuHandler(FrameLayout displayLayer) {
			mDisplayLayer = new WeakReference<FrameLayout>(displayLayer);
		}
		
		@Override
		public void handleMessage(Message msg) {
			FrameLayout root = mDisplayLayer.get();
			Danmaku danmaku;
			if (root != null) {
				switch (msg.what) {
				case NEW_DANMAKU:
					danmaku = new Danmaku(root, msg.getData().getString(DANMAKU_TEXT_KEY));
					danmaku.start();
					break;
				case NEW_AND_POST_DANMAKU:
					String text = msg.getData().getString(DANMAKU_TEXT_KEY);
					danmaku = new Danmaku(root, text);
					CameraTestActivity.getMainActivity().pushDanmaku(text);
					danmaku.start();
					break;
				case DISPLAY_DANMAKU:
					ArrayList<String> danmakuList = msg.getData().getStringArrayList(DANMAKU_TEXT_LIST_KEY);
					for (String str : danmakuList) {
						Danmaku d = new Danmaku(root, str);
						d.start();
					}
					break;
				}
				
			} else {
				Log.e("Danmaku error", "Cannot find parent reference.");
			}
		}

		public void showDanmaku(String text) {
			Message msg = new Message();
			Bundle data = new Bundle();
			msg.what = DanmakuHandler.NEW_DANMAKU;
			data.putString(DanmakuHandler.DANMAKU_TEXT_KEY, text);
			msg.setData(data);
			sendMessageDelayed(msg, DanmakuDelay);
		}
		
		public void pushDanmaku(String text) {
			Message msg = new Message();
			Bundle data = new Bundle();
			msg.what = DanmakuHandler.NEW_AND_POST_DANMAKU;
			data.putString(DanmakuHandler.DANMAKU_TEXT_KEY, text);
			msg.setData(data);
			sendMessageDelayed(msg, DanmakuDelay);
		}
	}
}
