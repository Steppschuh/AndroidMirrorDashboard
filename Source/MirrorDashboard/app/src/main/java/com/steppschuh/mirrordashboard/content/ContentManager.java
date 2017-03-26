package com.steppschuh.mirrordashboard.content;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class ContentManager implements ContentUpdateListener {

    private static final String TAG = ContentManager.class.getSimpleName();

    private List<ContentUpdateListener> contentUpdateListeners = new ArrayList<>();
    private List<ContentUpdater> contentUpdaters = new ArrayList<>();

    public ContentManager() {
    }

    @Override
    public void onContentUpdated(Content content) {
        for (ContentUpdateListener contentUpdateListener : contentUpdateListeners) {
            contentUpdateListener.onContentUpdated(content);
        }
    }

    @Override
    public void onContentUpdateFailed(ContentUpdater contentUpdater, ContentUpdateException exception) {
        for (ContentUpdateListener contentUpdateListener : contentUpdateListeners) {
            contentUpdateListener.onContentUpdateFailed(contentUpdater, exception);
        }
    }

    public void registerContentUpdateListener(ContentUpdateListener contentUpdateListener) {
        if (!contentUpdateListeners.contains(contentUpdateListener)) {
            contentUpdateListeners.add(contentUpdateListener);
        } else {
            Log.w(TAG, "Attempted to register a duplicate ContentUpdateListener: " + contentUpdateListener);
        }
    }

    public void unregisterContentUpdateListener(ContentUpdateListener contentUpdateListener) {
        if (contentUpdateListeners.contains(contentUpdateListener)) {
            contentUpdateListeners.remove(contentUpdateListener);
        } else {
            Log.w(TAG, "Attempted to unregister a ContentUpdateListener that isn't registered: " + contentUpdateListener);
        }
    }

    public void startAllContentUpdaters() {
        for (ContentUpdater contentUpdater : contentUpdaters) {
            contentUpdater.startUpdating(this);
        }
    }

    public void stopAllContentUpdaters() {
        for (ContentUpdater contentUpdater : contentUpdaters) {
            contentUpdater.stopUpdating();
        }
    }

    public void addContentUpdater(ContentProvider contentProvider) {
        ContentUpdater contentUpdater = contentProvider.createDefaultContentUpdater();
        addContentUpdater(contentUpdater);
    }

    public void addContentUpdater(ContentUpdater contentUpdater) {
        if (!contentUpdaters.contains(contentUpdater)) {
            contentUpdaters.add(contentUpdater);
        }
    }

    /**
     * Alters the update interval of all {@link ContentUpdater}s by the
     * specified factor.
     * <p>
     * A factor of 2 would would result in content only updating half
     * as often.
     */
    public void adjustUpdateInterval(float factor) {
        for (ContentUpdater contentUpdater : contentUpdaters) {
            long defaultInterval = ContentUpdater.getDefaultUpdateInterval(contentUpdater.getType());
            long adjustedInterval = Math.round(defaultInterval * factor);
            contentUpdater.setInterval(adjustedInterval);
        }
    }

    /**
     * Returns a recommended update interval based on the current
     * condition
     */
    public static float getRecommendedUpdateIntervalFactor() {
        Date now = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(now);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour <= 6) {
            return 5f;
        } else if (hour >= 23) {
            return 3f;
        } else if (hour >= 7 && hour <= 8) {
            return 0.66f;
        } else {
            return 1f;
        }
    }

}
