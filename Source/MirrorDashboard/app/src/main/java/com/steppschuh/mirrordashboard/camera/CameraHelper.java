package com.steppschuh.mirrordashboard.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Stephan on 3/25/2017.
 */

@SuppressWarnings("deprecation")
public final class CameraHelper {

    private static final String TAG = CameraHelper.class.getSimpleName();

    private static boolean useFrontFacingCamera = true;

    private static CameraHelper instance;

    private Camera camera;
    private boolean canTakePictures = false;

    public static CameraHelper getInstance() {
        if (instance == null) {
            instance = new CameraHelper();
        }
        return instance;
    }

    public static void openCamera(Context context) {
        try {
            getInstance().openDeprecatedCamera(context);
        } catch (CameraException e) {
            Log.w(TAG, "Unable to open camera", e);
        }
    }

    public void openDeprecatedCamera(Context context) throws CameraException {
        camera = null;
        try {
            int cameraId = 0;
            if (useFrontFacingCamera) {
                cameraId = getFrontFacingCameraId();
            }
            camera = Camera.open(cameraId);
            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    canTakePictures = true;
                }
            });
            Log.d(TAG, "Camera opened");
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            throw new CameraException("Camera not available", e);
        }
    }

    public void takePicture(Context context) throws CameraException {
        Log.d(TAG, "Taking picture ...");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        } else {
            if (!canTakePictures) {
                throw new CameraException("Unable to take pictures");
            }
            camera.takePicture(null, null, createPictureCallback());
        }
    }

    public static void closeCamera() {
        getInstance().closeDeprecatedCamera();
    }

    public void closeDeprecatedCamera() {
        canTakePictures = false;
        if (camera == null) {
            return;
        }
        camera.stopPreview();
        camera.release();
        camera = null;
        Log.d(TAG, "Camera closed");
    }

    private Camera.PictureCallback createPictureCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.i(TAG, "Picture taken");
                try {
                    writePictureToFile(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void writePictureToFile(byte[] data) throws IOException {
        File externalStorage = Environment.getExternalStorageDirectory();
        File appDirectory = new File(externalStorage, "/Mirror");
        appDirectory.mkdirs();

        String imageFileName = "photo_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(appDirectory, imageFileName);
        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
        fileOutputStream.write(data);
        fileOutputStream.close();
        Log.v(TAG, "Wrote picture to file: " + imageFile.getAbsolutePath());
    }

    public static boolean deviceHasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private static int getFrontFacingCameraId() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

}
