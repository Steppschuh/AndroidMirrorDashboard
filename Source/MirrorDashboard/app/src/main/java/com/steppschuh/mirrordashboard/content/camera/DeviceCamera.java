package com.steppschuh.mirrordashboard.content.camera;

import android.content.Context;

import com.steppschuh.mirrordashboard.camera.CameraException;
import com.steppschuh.mirrordashboard.camera.CameraHelper;
import com.steppschuh.mirrordashboard.camera.PictureTakenListener;
import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentProvider;
import com.steppschuh.mirrordashboard.content.ContentUpdateException;
import com.steppschuh.mirrordashboard.content.ContentUpdater;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Stephan on 3/26/2017.
 */

public class DeviceCamera extends ContentProvider implements PictureTakenListener {

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
    public void onPictureTaken(byte[] data) {
        // TODO: create content from data
        latestPhoto = new Photo(data);

        if (takePictureLatch != null) {
            takePictureLatch.countDown();
        }
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
