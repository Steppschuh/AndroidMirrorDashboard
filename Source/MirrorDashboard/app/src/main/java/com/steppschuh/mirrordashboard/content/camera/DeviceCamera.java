package com.steppschuh.mirrordashboard.content.camera;

import android.content.Context;
import android.graphics.Bitmap;

import com.steppschuh.mirrordashboard.camera.CameraException;
import com.steppschuh.mirrordashboard.camera.CameraHelper;
import com.steppschuh.mirrordashboard.camera.CameraPictureTakenListener;
import com.steppschuh.mirrordashboard.camera.CameraPreviewUpdatedListener;
import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentProvider;
import com.steppschuh.mirrordashboard.content.ContentUpdateException;
import com.steppschuh.mirrordashboard.content.ContentUpdater;
import com.steppschuh.mirrordashboard.util.BitmapUtil;
import com.steppschuh.mirrordashboard.util.ScreenUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Stephan on 3/26/2017.
 */

public class DeviceCamera extends ContentProvider implements CameraPictureTakenListener, CameraPreviewUpdatedListener {

    public static final int POSITION_BACK_FACING = 0;
    public static final int POSITION_FRONT_FACING = 1;
    private static final long TAKE_PICTURE_TIMEOUT = TimeUnit.SECONDS.toMillis(3);

    protected Context context;

    protected int cameraPosition;
    protected CountDownLatch takePictureLatch;
    protected Photo latestPhoto;

    public DeviceCamera(Context context, int cameraPosition) {
        super(Content.TYPE_PHOTO);
        this.context = context;
        this.cameraPosition = cameraPosition;
    }

    @Override
    public Content fetchContent() throws ContentUpdateException {
        try {
            takePictureLatch = new CountDownLatch(1);
            requestPicture();
            if (takePictureLatch.await(TAKE_PICTURE_TIMEOUT, TimeUnit.MILLISECONDS)) {
                return latestPhoto;
            } else {
                throw new InterruptedException();
            }
        } catch (InterruptedException e) {
            throw new ContentUpdateException("Unable to take picture in time", e);
        }
    }

    protected void requestPicture() {
        try {
            CameraHelper.takePicture(this);
        } catch (CameraException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraPictureTaken(byte[] data) {
        float rotationAngle = ScreenUtil.getRotation(context);
        if (rotationAngle != 0) {
            Bitmap bitmap = BitmapUtil.createBitmap(data);
            Bitmap rotatedBitmap = BitmapUtil.rotate(bitmap, rotationAngle);
            byte[] rotatedData = BitmapUtil.createByteArray(rotatedBitmap);

            latestPhoto = new Photo(rotatedData);
            latestPhoto.setBitmap(rotatedBitmap);
        } else {
            latestPhoto = new Photo(data);
        }

        if (takePictureLatch != null) {
            takePictureLatch.countDown();
        }
    }

    @Override
    public void onCameraPreviewUpdated(byte[] data) {

    }

    public void openCamera() {
        CameraHelper.openCamera(context);
    }

    public void closeCamera() {
        CameraHelper.closeCamera();
    }

    @Override
    public ContentUpdater createDefaultContentUpdater() {
        return new PhotoUpdater(this);
    }

}
