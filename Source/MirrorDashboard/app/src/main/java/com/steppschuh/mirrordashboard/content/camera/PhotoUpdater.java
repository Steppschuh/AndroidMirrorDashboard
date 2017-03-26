package com.steppschuh.mirrordashboard.content.camera;

import com.steppschuh.mirrordashboard.content.ContentUpdateListener;
import com.steppschuh.mirrordashboard.content.ContentUpdater;

/**
 * Created by Stephan on 3/26/2017.
 */

public class PhotoUpdater extends ContentUpdater {

    public PhotoUpdater(DeviceCamera contentProvider) {
        super(contentProvider);
    }

    @Override
    public void startUpdating(ContentUpdateListener contentUpdateListener) {
        ((DeviceCamera) contentProvider).openCamera();
        super.startUpdating(contentUpdateListener);
    }

    @Override
    public void stopUpdating() {
        super.stopUpdating();
        ((DeviceCamera) contentProvider).closeCamera();
    }

}
