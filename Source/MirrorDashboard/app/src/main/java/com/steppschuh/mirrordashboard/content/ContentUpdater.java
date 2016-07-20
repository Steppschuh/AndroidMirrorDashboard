package com.steppschuh.mirrordashboard.content;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class ContentUpdater {

    public static final String TAG = ContentUpdater.class.getSimpleName();
    public static final long INTERVAL_DEFAULT = TimeUnit.MINUTES.toMillis(3);

    private long interval = INTERVAL_DEFAULT;
    private ContentProvider contentProvider;
    private Content content;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean shouldUpdate;

    public ContentUpdater(ContentProvider contentProvider) {
        this.contentProvider = contentProvider;
        this.interval = getDefaultUpdateInterval(contentProvider.getType());
    }

    public int getType() {
        return contentProvider.getType();
    }

    public void startUpdating(final ContentUpdateListener contentUpdateListener) {
        try {
            shouldUpdate = true;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        updateContent();
                        contentUpdateListener.onContentUpdated(content);
                    } catch (Exception ex) {
                        contentUpdateListener.onContentUpdateFailed(ex);
                    } finally {
                        if (shouldUpdate) {
                            handler.postDelayed(this, interval);
                        } else {
                            handler.removeCallbacks(this);
                        }
                    }
                }
            };

            handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            handler.postDelayed(runnable, 50);
            Log.d(TAG, "Started updating " + contentProvider);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to start updating " + contentProvider);
            ex.printStackTrace();
        }
    }

    public void stopUpdating() {
        try {
            shouldUpdate = false;
            if (handlerThread != null && handlerThread.isAlive()) {
                handlerThread.quit();
            }
            Log.d(TAG, "Stopped updating " + contentProvider);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to stop updating " + contentProvider);
            ex.printStackTrace();
        }
    }

    private void updateContent() throws Exception {
        content = contentProvider.fetchContent();
    }

    public static long getDefaultUpdateInterval(int type) {
        switch (type) {
            case Content.TYPE_WEATHER: {
                return TimeUnit.MINUTES.toMillis(5);
            }
            default: {
                return INTERVAL_DEFAULT;
            }
        }
    }

    @Override
    public String toString() {
        return contentProvider + " updater";
    }
}
