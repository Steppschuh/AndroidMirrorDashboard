package com.steppschuh.mirrordashboard.content.location;

import android.content.Context;
import android.util.Log;

import com.steppschuh.mirrordashboard.R;
import com.steppschuh.mirrordashboard.content.Content;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

    public Location(Location location) {
        this();
        this.subject = location.getSubject();
        this.action = location.getAction();
        this.place = location.getPlace();
        this.changeTimestamp = location.getChangeTimestamp();
    }

    public String getReadableString(Context context) {
        return new StringBuilder(subject)
                .append(" ").append(getReadableAction(context))
                .append(" ").append(place)
                .append(" ").append(getReadableTime(context))
                .toString();
    }

    public String getReadableDescription(Context context) {
        return new StringBuilder(subject)
                .append(" ").append(getReadableAction(context))
                .append(" ").append(place)
                .toString();
    }

    public String getReadableTime(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
        String time = dateFormat.format(new Date(changeTimestamp));
        return String.format(context.getResources().getString(R.string.time_since_value), time);
    }

    public String getReadablePassedTime(Context context) {
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - changeTimestamp);
        long seconds = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long minutes = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hours = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
        long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
        long years = (diffInSeconds = (diffInSeconds / 12));

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" ");
            if (days == 1) {
                sb.append(context.getResources().getString(R.string.time_day));
            } else {
                sb.append(context.getResources().getString(R.string.time_days));
            }
            if (days <= 3 && hours > 0) {
                sb.append(" ").append(context.getResources().getString(R.string.time_and)).append(" ");
                sb.append(hours).append(" ");
                if (hours == 1) {
                    sb.append(context.getResources().getString(R.string.time_hour));
                } else {
                    sb.append(context.getResources().getString(R.string.time_hours));
                }
            }
        } else if (hours > 0) {
            sb.append(hours);
            int hourFraction = (int) Math.floor((minutes * 10) / 60);
            if (hours < 3 && hourFraction > 0) {
                sb.append(".").append(hourFraction);
            }
            sb.append(" ");
            if (hours == 1 && hourFraction == 0) {
                sb.append(context.getResources().getString(R.string.time_hour));
            } else {
                sb.append(context.getResources().getString(R.string.time_hours));
            }

        } else if (minutes >= 15) {
            sb.append(minutes).append(" ").append(context.getResources().getString(R.string.time_minutes));
        } else if (minutes >= 2) {
            sb.append(context.getResources().getString(R.string.time_few_minutes));
        } else {
            sb.append(context.getResources().getString(R.string.time_now));
        }

        return String.format(context.getResources().getString(R.string.time_since_value), sb.toString());
    }

    public String getReadableAction(Context context) {
        if (action == null) {
            return "";
        } else if (action.equals(ACTION_ARRIVE)) {
            return context.getResources().getString(R.string.location_action_arrive);
        } else if (action.equals(ACTION_LEAVE)) {
            return context.getResources().getString(R.string.location_action_leave);
        } else {
            Log.w(Location.class.getSimpleName(), "Unknown action: " + action);
            return "";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Location")
                .append(" Subject: ").append(subject)
                .append(", Place: ").append(place);
        if (action != null) {
            sb.append(", Action: ").append(action);
        }
        if (changeTimestamp > 0) {
            sb.append(", Change: ").append(new Date(changeTimestamp));
        }
        return sb.toString();
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
