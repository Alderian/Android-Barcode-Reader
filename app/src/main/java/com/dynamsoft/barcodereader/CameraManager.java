package com.dynamsoft.barcodereader;

import android.content.Context;
import android.hardware.Camera;
import android.widget.Toast;

public class CameraManager {
	private Camera mCamera;
	private Context mContext;

	
	public CameraManager(Context context) {
		mContext = context;
		// Create an instance of Camera
        mCamera = getCameraInstance();
	}

	/** A safe way to get an instance of the Camera object. */
	private static Camera getCameraInstance(){
        Camera mCamera = null;
	    try {
			int camFront = 0, camBack = 0;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			int numberOfCameras = Camera.getNumberOfCameras();
			for (int i = 0; i < numberOfCameras; i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					camFront = i;
				}
				else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					camBack = i;
				}

				//Una vez se tenga el ID de la camBack pues se lanza la apertura de la cámara.
				mCamera = Camera.open(camBack);
			}
		}
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return mCamera; // returns null if camera is unavailable
	}

	public Camera getCamera() {
		return mCamera;
	}
	
	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}
	
	public void onPause() {
		releaseCamera();
	}
	
	public void onResume() {
		if (mCamera == null) {
			mCamera = getCameraInstance();
		}

		Toast.makeText(mContext, "preview size = " + mCamera.getParameters().getPreviewSize().width +
				", " + mCamera.getParameters().getPreviewSize().height, Toast.LENGTH_LONG).show();
	}
	
}
