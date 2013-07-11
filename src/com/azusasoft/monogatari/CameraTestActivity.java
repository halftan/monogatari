/*
 * Basic no frills app which integrates the ZBar barcode scanner with
 * the camera.
 * 
 * Created by lisah0 on 2012-02-24
 */
package com.azusasoft.monogatari;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CameraTestActivity extends Activity {
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	private String mFocusMode;
	static private Activity mMainActivity;

	FrameLayout frameLayout;

	ImageScanner scanner;

	private boolean barcodeScanned = false;
	private boolean previewing = true;
	
	public static boolean animationEnded = true;

	static {
		System.loadLibrary("iconv");
	}
	
	static public Activity getMainActivity() {
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
		frameLayout = (FrameLayout) findViewById(R.id.cameraPreview);
		if (mFocusMode != null)
			mPreview = new CameraPreview(this, mCamera, previewCb, null, mFocusMode);
		else 
			mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB, null);
		
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
		
		frameLayout.setOnClickListener(new OnClickListener() {
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
				for (Symbol sym : syms) {
					// TODO Do something with the code
					barcodeScanned = true;
				}
				
		    	Danmaku test = new Danmaku(frameLayout, "TestTTTTTTTTTTTADTTTTTTTTTTTTTTdgTTTTTTTTTTTTTvc4TTTTTTTdksf");
		    	test.start();
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
}
