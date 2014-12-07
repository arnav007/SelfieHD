package com.crudelogics.selfiehd.cam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class CamFragment extends Fragment {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private boolean isRunning;
	private File pictureFile;

	private Camera cam;
	private CamPreview mPreview;

	private Parameters params;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (checkCameraHardware(getActivity()) == -1) {
			Toast.makeText(getActivity(), "NO camera", Toast.LENGTH_SHORT)
					.show();
			getActivity().finish();
			return null;
		}

		cam = getCameraInstance();

		if (cam == null) {
			Toast.makeText(getActivity(), "Camera could not be opened",
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
			return null;
		}

		isRunning = true;
		// Create our Preview view and set it as the content of our activity.
		mPreview = new CamPreview(getActivity(), cam);

		params = cam.getParameters();

		// autoFocus
		List<String> focusModes = params.getSupportedFocusModes();
		if (focusModes != null
				&& focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			// Autofocus mode is supported

			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			// set Camera parameters
			cam.setParameters(params);
		}

		return mPreview;
	}

	@Override
	public void onStart() {
		super.onStart();

		new Handler().postAtTime(new Runnable() {

			@Override
			public void run() {
				if (isRunning) {
					cam.takePicture(null, null, mPicture);
				}
			}
		}, SystemClock.uptimeMillis() + 3000);
	}

	@Override
	public void onPause() {
		super.onPause();
		isRunning = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseCamera();
	}

	private void showInGallery() {

		getActivity().sendBroadcast(
				new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
						.parse("file://" + pictureFile.getAbsolutePath())));
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri imgUri = Uri.parse("file://" + pictureFile.getPath());
		intent.setDataAndType(imgUri, "image/*");
		startActivity(intent);
	}

	private void releaseCamera() {
		if (cam != null) {
			cam.release(); // release the camera for other applications
			cam = null;
		}
	}

	/** Check if this device has a camera */
	private int checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return Camera.getNumberOfCameras();
		} else {
			// no camera on this device
			return -1;
		}
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			e.printStackTrace();
		}
		return c; // returns null if camera is unavailable
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}

			showInGallery();
			getActivity().onBackPressed();
		}
	};

	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"SelfieHD");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}
		return mediaFile;
	}
}
