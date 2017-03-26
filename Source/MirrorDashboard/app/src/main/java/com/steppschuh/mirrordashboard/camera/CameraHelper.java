package com.steppschuh.mirrordashboard.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stephan on 3/25/2017.
 */

public final class CameraHelper {

    private static final String TAG = CameraHelper.class.getSimpleName();

    private static boolean useFrontFacingCamera = true;

    private static CameraHelper instance;
    private List<CameraPictureTakenListener> cameraPictureTakenListeners = new ArrayList<>();
    private List<CameraPreviewUpdatedListener> cameraPreviewUpdatedListeners = new ArrayList<>();

    private Camera deprecatedCamera;
    private SurfaceTexture previewSurface;

    private boolean canTakePictures = false;

    private long lastProcessedPreviewTimestamp = 0;
    private long minimumPreviewUpdateDelay = 1000;

    public static CameraHelper getInstance() {
        if (instance == null) {
            instance = new CameraHelper();
        }
        return instance;
    }

    /**
     * Sets up the camera and prepares it to take pictures.
     *
     * @param context
     */
    public static void openCamera(Context context) {
        try {
            getInstance().openDeprecatedCamera();
        } catch (CameraException e) {
            Log.w(TAG, "Unable to open deprecatedCamera", e);
        }
    }

    /**
     * Attempts to take a picture using the previously setup camera.
     *
     * @throws CameraException
     */
    public static void takePicture(CameraPictureTakenListener cameraPictureTakenListener) throws CameraException {
        Log.d(TAG, "Taking picture ...");
        CameraHelper instance = getInstance();
        if (!instance.canTakePictures) {
            throw new CameraException("Unable to take pictures");
        }
        instance.registerPictureTakenListener(cameraPictureTakenListener);
        instance.takeDeprecatedPicture();
    }

    /**
     * Releases the camera instance.
     */
    public static void closeCamera() {
        getInstance().closeDeprecatedCamera();
    }

    /**
     * Callback for {@link #takePicture(CameraPictureTakenListener)}.
     *
     * @param data the compressed picture as JPG
     */
    protected void onPictureDataReceived(byte[] data) {
        Log.d(TAG, "Picture taken");
        for (CameraPictureTakenListener cameraPictureTakenListener : cameraPictureTakenListeners) {
            try {
                cameraPictureTakenListener.onCameraPictureTaken(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void onPreviewDataReceived(byte[] data) {
        for (CameraPreviewUpdatedListener cameraPreviewUpdatedListener : cameraPreviewUpdatedListeners) {
            try {
                cameraPreviewUpdatedListener.onCameraPreviewUpdated(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Methods using the old android.hardware.face_dtection API
     */

    @SuppressWarnings("deprecation")
    protected void openDeprecatedCamera() throws CameraException {
        deprecatedCamera = null;
        canTakePictures = false;
        try {
            int direction = useFrontFacingCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
            int cameraId = getDeprecatedCameraId(direction);
            previewSurface = new SurfaceTexture(0);

            deprecatedCamera = Camera.open(cameraId);
            deprecatedCamera.setPreviewTexture(previewSurface);
            deprecatedCamera.startPreview();
            deprecatedCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] yuvData, Camera camera) {
                    canTakePictures = true;
                    if (cameraPreviewUpdatedListeners.isEmpty()) {
                        return;
                    }
                    if (lastProcessedPreviewTimestamp > System.currentTimeMillis() - minimumPreviewUpdateDelay) {
                        return;
                    }
                    lastProcessedPreviewTimestamp = System.currentTimeMillis();

                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size size = parameters.getPreviewSize();
                    YuvImage yuvImage = new YuvImage(yuvData, parameters.getPreviewFormat(), size.width, size.height, null);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height), 50, out);
                    byte[] bitmapData = out.toByteArray();

                    onPreviewDataReceived(bitmapData);
                }
            });
            Log.d(TAG, "Camera opened");
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            throw new CameraException("Camera not available", e);
        }
    }

    @SuppressWarnings("deprecation")
    protected void takeDeprecatedPicture() throws CameraException {
        deprecatedCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                canTakePictures = true;
                camera.startPreview();
                onPictureDataReceived(data);
            }
        });
        canTakePictures = false;
    }

    @SuppressWarnings("deprecation")
    protected void closeDeprecatedCamera() {
        try {
            canTakePictures = false;
            deprecatedCamera.stopPreview();
            deprecatedCamera.release();
            deprecatedCamera = null;
            previewSurface = null;
            Log.d(TAG, "Camera closed");
        } catch (Exception e) {
            Log.w(TAG, "Unable to close camera", e);
        }
    }

    @SuppressWarnings("deprecation")
    private static int getDeprecatedCameraId(int direction) throws CameraException {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            if (info.facing == direction) {
                return cameraId;
            }
        }
        throw new CameraException("No deprecatedCamera available with direction: " + direction);
    }

    /**
     * Writes the passed bytes to a jpg file in the public external storage directory.
     *
     * @param data
     * @throws IOException
     */
    public static void writePictureToFile(byte[] data) throws IOException {
        File externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appDirectory = new File(externalStorage, "/Mirror");
        appDirectory.mkdirs();

        String imageFileName = "photo_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(appDirectory, imageFileName);
        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
        fileOutputStream.write(data);
        fileOutputStream.close();
        Log.v(TAG, "Wrote picture to file: " + imageFile.getAbsolutePath());
    }

    /**
     * Checks if the current device has a face_dtection.
     *
     * @param context
     * @return
     */
    public static boolean deviceHasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * Checks if the the current device should use the new face_dtection API or the deprecated one.
     * Because the deprecated API works more solid (even on current devices),
     * it will be used even though the newer API is available.
     *
     * @return
     */
    private static boolean useDeprecatedCamera() {
        return true || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public void registerPictureTakenListener(CameraPictureTakenListener cameraPictureTakenListener) {
        if (!cameraPictureTakenListeners.contains(cameraPictureTakenListener)) {
            cameraPictureTakenListeners.add(cameraPictureTakenListener);
        }
    }

    public void unregisterPictureTakenListener(CameraPictureTakenListener cameraPictureTakenListener) {
        if (cameraPictureTakenListeners.contains(cameraPictureTakenListener)) {
            cameraPictureTakenListeners.remove(cameraPictureTakenListener);
        }
    }

    public void registerPreviewUpdatedListener(CameraPreviewUpdatedListener CameraPreviewUpdatedListener) {
        if (!cameraPreviewUpdatedListeners.contains(CameraPreviewUpdatedListener)) {
            cameraPreviewUpdatedListeners.add(CameraPreviewUpdatedListener);
        }
    }

    public void unregisterPreviewUpdatedListener(CameraPreviewUpdatedListener CameraPreviewUpdatedListener) {
        if (cameraPreviewUpdatedListeners.contains(CameraPreviewUpdatedListener)) {
            cameraPreviewUpdatedListeners.remove(CameraPreviewUpdatedListener);
        }
    }

}
