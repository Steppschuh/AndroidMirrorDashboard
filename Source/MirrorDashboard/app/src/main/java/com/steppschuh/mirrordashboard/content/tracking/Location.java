package com.steppschuh.mirrordashboard.content.tracking;

import com.steppschuh.mirrordashboard.content.Content;

import java.util.Date;

public class Location extends Content {

    public static final String ACTION_ARRIVE = "arrive";
    public static final String ACTION_LEAVE = "leave";

    private String subject;
    private String action;
    private String place;
    private long changeTimestamp;

    public Location() {
        super(Content.TYPE_LOCATION);
    }

    @Override
    public String toString() {
        return new StringBuilder(subject)
                .append(" ").append(action)
                .append(" ").append(place)
                .append(" ").append(new Date(changeTimestamp))
                .toString();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public long getChangeTimestamp() {
        return changeTimestamp;
    }

    public void setChangeTimestamp(long changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

}
