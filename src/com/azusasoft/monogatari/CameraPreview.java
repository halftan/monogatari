/*
 * Barebones implementation of displaying camera preview.
 * 
 * Created by lisah0 on 2012-02-24
 */
package com.azusasoft.monogatari;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/** A basic Camera preview class */
@SuppressLint("InlinedApi")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private PreviewCallback previewCallback;
	private AutoFocusCallback autoFocusCallback;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera,
						 PreviewCallback previewCb,
						 AutoFocusCallback autoFocusCb,
						 String focusMode) {
		super(context);
		mCamera = camera;
		previewCallback = previewCb;
		autoFocusCallback = autoFocusCb;

		/* 
		 * Set camera to continuous focus if supported, otherwise use
		 * software auto-focus. Only works for API level >=9.
		 */

		Parameters params = mCamera.getParameters();
		if (autoFocusCb == null) {
			if (focusMode == null) {
				String cameraFeature = Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
				if (Build.VERSION.SDK_INT >= 14) {
					cameraFeature = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
				}
				for (String f : params.getSupportedFocusModes()) {
					if (f == cameraFeature) {
						params.setFocusMode(f);
						break;
					}
				}
			} else {
				params.setFocusMode(focusMode);
			}
		}
		List<Size> sizes = params.getSupportedPreviewSizes();
		params.setPreviewSize(sizes.get(0).width, sizes.get(0).height);
		mCamera.setParameters(params);
		
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);

		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
		try {
			mCamera.setPreviewDisplay(holder);
			ScanningActivity.getMainActivity().startPreview();
		} catch (IOException e) {
			Log.d("DBG", "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Camera preview released in activity
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		/*
		 * If your preview can change or rotate, take care of those events here.
		 * Make sure to stop the preview before resizing or reformatting it.
		 */
		if (mHolder.getSurface() == null){
		  // preview surface does not exist
		  return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e){
		  // ignore: tried to stop a non-existent preview
		}

		try {
			int orientation = 0;
			WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
			Log.d("camera", String.valueOf(windowManager.getDefaultDisplay().getRotation()));
			switch(windowManager.getDefaultDisplay().getRotation()) {
			
			case Surface.ROTATION_0:
				orientation = 0;
				break;
			case Surface.ROTATION_180:
				orientation = 180;
				break;
			}
			mCamera.setDisplayOrientation(orientation); // Landscape only.

			mCamera.setPreviewDisplay(mHolder);
			mCamera.setPreviewCallback(previewCallback);
			mCamera.startPreview();
			mCamera.autoFocus(autoFocusCallback);
		} catch (Exception e){
			Log.d("DBG", "Error starting camera preview: " + e.getMessage());
		}
	}
	
}
