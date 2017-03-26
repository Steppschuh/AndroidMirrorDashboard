package com.steppschuh.mirrordashboard.content;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class ContentUpdater {

    public static final String TAG = ContentUpdater.class.getSimpleName();
    public static final long INTERVAL_DEFAULT = TimeUnit.MINUTES.toMillis(3);

    protected long interval = INTERVAL_DEFAULT;
    protected ContentProvider contentProvider;
    protected Content content;
    protected Handler handler;
    protected HandlerThread handlerThread;
    protected boolean shouldUpdate;

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
                    } catch (ContentUpdateException ex) {
                        contentUpdateListener.onContentUpdateFailed(ContentUpdater.this, ex);
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

    private void updateContent() throws ContentUpdateException {
        content = contentProvider.fetchContent();
    }

    public static long getDefaultUpdateInterval(int type) {
        switch (type) {
            case Content.TYPE_WEATHER: {
                return TimeUnit.MINUTES.toMillis(10);
            }
            case Content.TYPE_TRANSIT: {
                return TimeUnit.MINUTES.toMillis(3);
            }
            case Content.TYPE_LOCATION: {
                return TimeUnit.MINUTES.toMillis(3);
            }
            case Content.TYPE_PHOTO: {
                return TimeUnit.SECONDS.toMillis(10);
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

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

}
