package com.dynamsoft.barcodereader;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "camera";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mWidth, mHeight;
    private Context mContext;
    private MultiFormatReader mMultiFormatReader;
    private AlertDialog mDialog;
    private int mLeft, mTop, mAreaWidth, mAreaHeight;
    private String source;
    private Camera.PreviewCallback mPreviewCallback = new PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub

            if (mDialog.isShowing())
                return;

//            Log.d(TAG, "data: " + data);
//            Log.d(TAG, "mWidth: " + mWidth + ", mHeight: " + mHeight + ", mLeft: " + mLeft + ", mTop: " + mTop + ", mAreaWidth: " + mAreaWidth + ", mAreaHeight: " + mAreaHeight);
//
//            Log.d(TAG, "Width " + (mLeft + mWidth > mAreaWidth ? "OK" : "FAIL") + " -> : mleft: " + mLeft + " + mWidth: " + mWidth + " >? mAreaWidth: " + mAreaWidth);
//            Log.d(TAG, "Width " + (mTop + mHeight > mAreaHeight ? "OK" : "FAIL") + " -> : mTop: " + mTop + " + mHeight: " + mHeight + " >? mAreaHeight: " + mAreaHeight);

            LuminanceSource source = new PlanarYUVLuminanceSource(data, mWidth, mHeight, mLeft, mTop, mAreaWidth, mAreaHeight, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result;

            try {
                result = mMultiFormatReader.decode(bitmap, null);
                if (result != null) {
                    String code = result.getText();

            Log.d(TAG, "code: " + code);

                    // TODO: ... sum up
//                    Toast.makeText(CameraPreview.this.getContext(), "Adding " + code, Toast.LENGTH_SHORT).show();
                    new HttpsClient().execute("http://boot-alderian.rhcloud.com/item/add?source=" + source + "&code=" + code);

                    mDialog.setTitle("Result:");
                    mDialog.setMessage("Scan: \n" + code);
                    mDialog.show();
                }
            } catch (NotFoundException e) {
                Log.d(TAG, "Error", e);
            }
        }
    };

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mContext = context;
        mHolder = getHolder();
        mHolder.addCallback(this);

        source = getResources().getString(R.string.source);

        Log.i("parameters", mCamera.getParameters().flatten());

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Parameters params = mCamera.getParameters();

        // Auto focus
//        List<String> supportedFocusModes = params.getSupportedFocusModes();
//
//        if (supportedFocusModes.contains(Parameters.FOCUS_MODE_MACRO)) {
//            params.setFocusMode(Parameters.FOCUS_MODE_MACRO);
//        } else if (supportedFocusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
//            params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
//        }

        // 176x144 or 320x240 or 640x480
        mWidth = 640;
        mHeight = 480;

        params.setPreviewSize(mWidth, mHeight);
        mCamera.setParameters(params);

        mMultiFormatReader = new MultiFormatReader();

        mDialog = new AlertDialog.Builder(mContext).create();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();

        } catch (Exception e) {

        }

        try {
            mCamera.setPreviewCallback(mPreviewCallback);
//            mCamera.setAutoFocusMoveCallback(mAutoFocusCallback);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

//    private Camera.AutoFocusMoveCallback mAutoFocusCallback = new Camera.AutoFocusMoveCallback() {
//        @Override
//        public void onAutoFocusMoving(boolean start, Camera camera) {
//
//        }
//    };

    public void onPause() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }

//    public void setArea(int left, int top, int areaWidth, int width) {
//        double ratio = width / mWidth;
//        mLeft = (int) (left / (ratio + 1));
//        if (top < 0) top = -top; // Fix some rare ratio problems
//        mTop = (int) (top / (ratio + 1));
//        mAreaHeight = mHeight - mTop * 2;
//        mAreaWidth = mWidth - mLeft * 2;
//    }

    public void setArea(int left, int top, int areaWidth, int width) {
        double ratio = width / mWidth;
        mLeft = (int) (left / (ratio + 1));
        mTop = (int) (top / (ratio + 1));
        mAreaHeight = mAreaWidth = mWidth - mLeft * 2;
    }

 /*
  * GET EXAMPLE
  */
    class HttpsClient extends AsyncTask<String, Void, String> {
        private Exception exception;

        public String doInBackground(String... urls) {

            try {
                Log.d(TAG, "*******************    Open Connection    *****************************");
                URL url = new URL(urls[0]);
                Log.d(TAG, "Received URL:  " + url);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "Con Status: " + con);

                InputStream in = con.getInputStream();
                Log.d(TAG, "GetInputStream:  " + in);

                Log.d(TAG, "*******************    String Builder     *****************************");
                String line = null;

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                while ((line = br.readLine()) != null) {
                    if (line.contains("event")) {
                        //do nothing since the event tag is of no interest
                        Log.d(TAG, "Failed fetching needed values.");
                        return null;
                    }
                    if (line.contains("data: ")) {
                        //convert to JSON (stripping the beginning "data: "
                        JSONObject jObject = new JSONObject(line.substring(6));
                        String json_data = (String) jObject.get("data");
                        //convert again
                        jObject = new JSONObject(json_data);



                    }
                }

                // Closing the stream
                Log.d(TAG, "*******************  Stream closed, exiting     ******************************");
                br.close();
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to service", e);
                this.exception = e;
                return null;
            }
            return null;
        }

    }

}
