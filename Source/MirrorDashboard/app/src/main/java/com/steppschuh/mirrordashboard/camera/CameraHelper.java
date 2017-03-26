package com.steppschuh.mirrordashboard.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stephan on 3/25/2017.
 */

public final class CameraHelper {

    private static final String TAG = CameraHelper.class.getSimpleName();

    private static boolean useFrontFacingCamera = true;

    private static CameraHelper instance;
    private List<PictureTakenListener> pictureTakenListeners = new ArrayList<>();

    // deprecated camera related
    private Camera deprecatedCamera;
    private SurfaceTexture previewSurface;

    // new camera related
    private CameraDevice newCamera;
    private boolean canTakePictures = false;

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
            if (useDeprecatedCamera()) {
                getInstance().openDeprecatedCamera();
            } else {
                getInstance().openNewCamera(context);
            }
        } catch (CameraException e) {
            Log.w(TAG, "Unable to open deprecatedCamera", e);
        }
    }

    /**
     * Attempts to take a picture using the previously setup camera.
     *
     * @throws CameraException
     */
    public static void takePicture(PictureTakenListener pictureTakenListener) throws CameraException {
        Log.d(TAG, "Taking picture ...");
        CameraHelper instance = getInstance();
        if (!instance.canTakePictures) {
            throw new CameraException("Unable to take pictures");
        }
        instance.registerPictureTakenListener(pictureTakenListener);
        if (useDeprecatedCamera()) {
            instance.takeDeprecatedPicture();
        } else {
            instance.takeNewPicture();
        }
    }

    /**
     * Releases the camera instance.
     */
    public static void closeCamera() {
        if (useDeprecatedCamera()) {
            getInstance().closeDeprecatedCamera();
        } else {
            getInstance().closeNewCamera();
        }
    }

    /**
     * Callback for {@link #takePicture(PictureTakenListener)}.
     *
     * @param data the compressed picture as JPG
     */
    protected void onPictureDataReceived(byte[] data) {
        Log.d(TAG, "Picture taken");
        for (PictureTakenListener pictureTakenListener : pictureTakenListeners) {
            try {
                pictureTakenListener.onPictureTaken(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerPictureTakenListener(PictureTakenListener pictureTakenListener) {
        if (!pictureTakenListeners.contains(pictureTakenListener)) {
            pictureTakenListeners.add(pictureTakenListener);
        }
    }

    public void unregisterPictureTakenListener(PictureTakenListener pictureTakenListener) {
        if (pictureTakenListeners.contains(pictureTakenListener)) {
            pictureTakenListeners.remove(pictureTakenListener);
        }
    }

    /*
        Methods using the new android.hardware.camera2 API
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void openNewCamera(Context context) throws CameraException {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        int direction = useFrontFacingCamera ? CameraMetadata.LENS_FACING_FRONT : CameraMetadata.LENS_FACING_BACK;
        String cameraId = getNewCameraId(direction, manager);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw new CameraException("Camera permission not granted");
        }
        try {
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    newCamera = camera;
                    canTakePictures = true;
                    Log.d(TAG, "Camera opened");
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    newCamera = null;
                    canTakePictures = false;
                    Log.d(TAG, "Camera disconnected");
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    newCamera = null;
                    canTakePictures = false;
                    Log.d(TAG, "Camera error: " + error);
                }
            }, null);
        } catch (CameraAccessException e) {
            throw new CameraException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void takeNewPicture() throws CameraException {
        try {
            final CaptureRequest captureRequest = newCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).build();

            // TODO: query optimal size
            int width = 1000;
            int height = 1000;
            final ImageReader imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new LinkedList<>();
            outputSurfaces.add(imageReader.getSurface());

            newCamera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureRequest, new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                Image image = imageReader.acquireLatestImage();
                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                onPictureDataReceived(bytes);
                                image.close();
                            }

                            @Override
                            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                Log.w(TAG, "Capture failed: " + failure);
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.w(TAG, "Unable to configure capture session");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void closeNewCamera() {
        try {
            canTakePictures = false;
            if (newCamera == null) {
                return;
            }
            newCamera.close();
            newCamera = null;
            Log.d(TAG, "Camera closed");
        } catch (Exception e) {
            Log.w(TAG, "Unable to close camera", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getNewCameraId(int direction, CameraManager cameraManager) throws CameraException {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == direction) {
                    return cameraId;
                }
            }
            throw new CameraException("No deprecatedCamera available with direction: " + direction);
        } catch (CameraAccessException e) {
            throw new CameraException(e);
        }
    }

    /*
        Methods using the old android.hardware.camera API
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
            if (deprecatedCamera == null) {
                return;
            }
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
     * Checks if the current device has a camera.
     *
     * @param context
     * @return
     */
    public static boolean deviceHasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * Checks if the the current device should use the new camera API or the deprecated one.
     * Because the deprecated API works more solid (even on current devices),
     * it will be used even though the newer API is available.
     *
     * @return
     */
    private static boolean useDeprecatedCamera() {
        return true || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

}
