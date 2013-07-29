/*
 * Basic no frills app which integrates the ZBar barcode scanner with
 * the camera.
 * 
 * Created by lisah0 on 2012-02-24
 */
package com.azusasoft.monogatari;

import java.util.ArrayList;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.azusasoft.monogatari.controller.DanmakuController;
import com.azusasoft.monogatari.controller.MessageController;
import com.azusasoft.monogatari.data.HistoryDbHelper;

public class CameraTestActivity extends FragmentActivity {
	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	private String mFocusMode;
	static private CameraTestActivity mMainActivity;

	private FrameLayout mFrameLayout;
	private ImageScanner scanner;
	private LinearLayout mControls;
	
	private ViewGroup mDanmakuDisplayer;
	private DanmakuController mDanmakuController;

	private boolean barcodeScanned = false;
	private boolean previewing = false;

	private MessageController mMessageController;
	
	private HistoryDbHelper mDbHelper;

	static {
		System.loadLibrary("iconv");
	}
	
	static public CameraTestActivity getMainActivity() {
		return mMainActivity;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMainActivity = this;
		Log.d("danmaku lifecycle", "on create");
		
		setContentView(R.layout.main);
		
		mFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
		mControls = (LinearLayout) findViewById(R.id.controls);
		mDanmakuDisplayer = (ViewGroup) findViewById(R.id.danmaku_displayer);
		
		mDanmakuController = DanmakuController.init(this, mDanmakuDisplayer);
		mDbHelper = new HistoryDbHelper(this);

		mMessageController = MessageController.init(new MessageController.danmakuEventListener() {
			
			@Override
			public void pushFirstDanmaku(String mono) {
				NewDanmakuDialog dialog = new NewDanmakuDialog();
				Bundle args = new Bundle();
				args.putString(NewDanmakuDialog.SCANNED_BARCODE_KEY, mono);
				dialog.setArguments(args);
				dialog.show(getSupportFragmentManager(), "NewDanmakuDialogFragment");
			}

			@Override
			public void loadDanmaku(ArrayList<String> danmakuList) {
				mDanmakuController.loadDanmaku(danmakuList);
				runOnUiThread(mDanmakuController.spwanAllDanmaku);
			}
		});
		
		mDanmakuController.setNewDanmakuListener(mMessageController.newDanmakuWillBePosted);

		mCamera = getCameraInstance();
		autoFocusHandler = new Handler();

		/* Instance barcode scanner */
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 5);
		scanner.setConfig(0, Config.Y_DENSITY, 5);
		scanner.setConfig(0, Config.UNCERTAINTY, 1);

//		if (mFocusMode != null)
//			mPreview = new CameraPreview(this, mCamera, previewCb, null, mFocusMode);
//		else 
//			mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB, null);
        mPreview = getCameraPreview(this, mCamera, getPreviewCallback(), getAutofocusCallback(), null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("danmaku lifecycle", "on resume");
		
		reset(null);
		
		mFrameLayout.addView(mPreview);
		mFrameLayout.bringChildToFront(mControls);
		mFrameLayout.bringChildToFront(mDanmakuDisplayer);
		mDanmakuDisplayer.setBackgroundColor(Color.TRANSPARENT);
		
		if (mDbHelper == null)
			mDbHelper = new HistoryDbHelper(this);
	}
	
	@SuppressLint("InlinedApi")
	protected CameraPreview getCameraPreview(Context context, Camera camera,
			PreviewCallback previewCb, AutoFocusCallback autoFocusCb, String focusMode) {
    	mFocusMode = null;
    	String cameraFeature = Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
    	if (Build.VERSION.SDK_INT >= 14) {
    		cameraFeature = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
    	}
    	Parameters parameters = mCamera.getParameters();
    	for (String f : parameters.getSupportedFocusModes())
			if (f == cameraFeature)
				mFocusMode = f;

		return new CameraPreview(context, camera, previewCb, autoFocusCb, focusMode);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d("danmaku lifecycle", "on pause");
		mFrameLayout.removeView(mPreview);
		releaseCamera();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mDbHelper.close();
		mDbHelper = null;
	}

	public void newDanmaku(View v) {
		if (mMessageController.getTargetBarcode() == null) {
			Toast.makeText(this, R.string.not_scanned, Toast.LENGTH_SHORT).show();
			return;
		}
		NewDanmakuDialog dialog = new NewDanmakuDialog();
		Bundle args = new Bundle();
		args.putString(NewDanmakuDialog.SCANNED_BARCODE_KEY, mMessageController.getTargetBarcode());
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "NewDanmakuDialogFragment");
	}
	
	public void history(View v) {
		Intent intent = new Intent(this, HistoryActivity.class);
		startActivity(intent);
	}
	
	public void reset(View v) {
		if (barcodeScanned) {
			barcodeScanned = false;
//			if (mFocusMode != null) {
//				Parameters params = mCamera.getParameters();
//				params.setFocusMode(mFocusMode);
//				mCamera.setParameters(params);
//			} else
//				mCamera.autoFocus(autoFocusCB);
		}
		if (mCamera == null) {
			mCamera = getCameraInstance();
	        mPreview = getCameraPreview(this, mCamera, getPreviewCallback(), getAutofocusCallback(), null);
		}
		mCamera.setPreviewCallback(getPreviewCallback());
		mCamera.autoFocus(getAutofocusCallback());
		mCamera.startPreview();
		previewing = true;
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
			if (previewing) {
				try {
					mCamera.autoFocus(getAutofocusCallback());
				} catch (RuntimeException e) {
					Log.e("monogatari autofocus", e.getMessage());
					autoFocusHandler.postDelayed(doAutoFocus, 2000);
				}
			}
		}
	};
	
	/** This will be called immediately after a
	 * bar-code has been scanned.
	 */
	
	PreviewCallback previewCb;
	private PreviewCallback getPreviewCallback() {
		if (previewCb == null) {
			previewCb = new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera) {
					Camera.Parameters parameters = camera.getParameters();
					Size size = parameters.getPreviewSize();

					Image barcode = new Image(size.width, size.height, "Y800");
					barcode.setData(data);
					
					int result = scanner.scanImage(barcode);

					if (result != 0) {
						previewing = false;
						camera.setPreviewCallback(null);
//						camera.stopPreview();

						SymbolSet syms = scanner.getResults();
						StringBuilder results = new StringBuilder();
						for (Symbol sym : syms) {
							if (!TextUtils.isEmpty(sym.getData())) {
								results.append(sym.getData());
								barcodeScanned = true;
								break;
							}
						}
						Toast.makeText(CameraTestActivity.this, results.toString(), Toast.LENGTH_SHORT).show();
						camera.takePicture(null, null, null, new SaveToDbCallback(results.toString()));
						mMessageController.searchDanmakuFor(results.toString());
					}
				}
			};
		}
		return previewCb;
	}
	
	private class SaveToDbCallback implements Camera.PictureCallback {
		
		String mBarcode;
		
		public SaveToDbCallback(String barcode) {
			mBarcode = barcode;
		}

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			mDbHelper.insertBarcodeAndJpeg(mBarcode, data);
		}
		
	}

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB;
	private AutoFocusCallback getAutofocusCallback() {
		if (autoFocusCB == null)
			autoFocusCB = new AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				autoFocusHandler.postDelayed(doAutoFocus, 5000);
			}
		};
		return autoFocusCB;
	}
	
	public void startPreview() {
		previewing = true;
	}
	
}
