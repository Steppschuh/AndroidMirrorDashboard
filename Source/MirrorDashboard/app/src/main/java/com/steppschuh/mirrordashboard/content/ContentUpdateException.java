package com.steppschuh.mirrordashboard.content;

/**
 * Created by Stephan on 3/26/2017.
 */

public class ContentUpdateException extends Exception {

    public ContentUpdateException() {
        super();
    }

    public ContentUpdateException(String detailMessage) {
        super(detailMessage);
    }

    public ContentUpdateException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ContentUpdateException(Throwable throwable) {
        super(throwable);
    }

}
