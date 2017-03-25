package com.steppschuh.mirrordashboard.camera;

/**
 * Created by Stephan on 3/25/2017.
 */

public class CameraException extends Exception {

    public CameraException() {
        super();
    }

    public CameraException(String detailMessage) {
        super(detailMessage);
    }

    public CameraException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CameraException(Throwable throwable) {
        super(throwable);
    }

}
