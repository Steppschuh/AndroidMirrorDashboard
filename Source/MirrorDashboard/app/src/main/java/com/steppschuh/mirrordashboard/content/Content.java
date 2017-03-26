package com.steppschuh.mirrordashboard.content;

import java.util.Date;

public class Content {

    public static final int TYPE_WEATHER = 1;
    public static final int TYPE_CALENDAR = 2;
    public static final int TYPE_TRANSIT = 3;
    public static final int TYPE_NEWS = 4;
    public static final int TYPE_STOCK = 5;
    public static final int TYPE_LOCATION = 6;
    public static final int TYPE_PHOTO = 7;

    private int type;
    private long updateTimestamp;

    public Content(int type) {
        this.type = type;
        updateTimestamp = new Date().getTime();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

}
