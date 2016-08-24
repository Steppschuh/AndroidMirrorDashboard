package com.steppschuh.mirrordashboard.content;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentManager implements ContentUpdateListener {

    private static final String TAG = ContentManager.class.getSimpleName();

    private List<ContentUpdateListener> contentUpdateListeners = new ArrayList<>();
    private Map<Integer, ContentUpdater> contentUpdaters = new HashMap<>();

    public ContentManager() {
    }

    @Override
    public void onContentUpdated(Content content) {
        for (ContentUpdateListener contentUpdateListener : contentUpdateListeners) {
            contentUpdateListener.onContentUpdated(content);
        }
    }

    @Override
    public void onContentUpdateFailed(ContentUpdater contentUpdater, Exception exception) {
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
        for (Map.Entry<Integer, ContentUpdater> contentUpdaterEntry : contentUpdaters.entrySet()) {
            contentUpdaterEntry.getValue().startUpdating(this);
        }
    }

    public void stopAllContentUpdaters() {
        for (Map.Entry<Integer, ContentUpdater> contentUpdaterEntry : contentUpdaters.entrySet()) {
            contentUpdaterEntry.getValue().stopUpdating();
        }
    }

    public ContentUpdater getContentUpdater(int type) {
        if (contentUpdaters.containsKey(type)) {
            return contentUpdaters.get(type);
        } else {
            return null;
        }
    }

    public void addContentUpdater(ContentProvider contentProvider) {
        ContentUpdater contentUpdater = new ContentUpdater(contentProvider);
        addContentUpdater(contentUpdater);
    }

    public void addContentUpdater(ContentUpdater contentUpdater) {
        if (contentUpdaters.containsKey(contentUpdater.getType())) {
            Log.w(TAG, "Overwriting ContentUpdater for type " + contentUpdater.getType());
        }
        contentUpdaters.put(contentUpdater.getType(), contentUpdater);
    }

}
