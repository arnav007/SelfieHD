package com.crudelogics.selfiehd.cam;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CamPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;

	public CamPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			Parameters parameters = mCamera.getParameters();
			Size selectedPreviewSize = getBestSupprotedSize(getWidth(),
					getHeight(), parameters, true);
			parameters.setPreviewSize(selectedPreviewSize.width,
					selectedPreviewSize.height);
			Size selectedPictureSize = getBestSupprotedSize(getWidth(),
					getHeight(), parameters, false);
			parameters.setPictureSize(selectedPictureSize.width,
					selectedPictureSize.height);
			parameters.setRotation(90);
			mCamera.setParameters(parameters);

			mCamera.setDisplayOrientation(90);
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();

		} catch (IOException e) {
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
			// System.out.println("preview stopped");
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
		}
	}

	private Camera.Size getBestSupprotedSize(int width, int height,
			Camera.Parameters parameters, boolean isPreviewSize) {
		List<Size> supportedSizes;
		if (isPreviewSize) {
			supportedSizes = parameters.getSupportedPreviewSizes();
		} else {
			supportedSizes = parameters.getSupportedPictureSizes();
		}

		if (supportedSizes == null) {
			return null;
		}

		Size optimize = null;
		for (int i = 0; i < supportedSizes.size(); i++) {
			Size size = supportedSizes.get(i);
			if (optimize == null) {
				optimize = size;
				continue;
			}

			if (size.width > optimize.width && size.height > optimize.height) {
				optimize = size;
			}
		}
		return optimize;
	}
}
