package com.steppschuh.mirrordashboard.pattern.recorder;

import android.util.Log;

import com.steppschuh.mirrordashboard.pattern.Pattern;
import com.steppschuh.mirrordashboard.pattern.PatternRecordedListener;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericPatternRecorder implements PatternRecorder {

    private static final String TAG = GenericPatternRecorder.class.getSimpleName();

    List<PatternRecordedListener> patternRecordedListeners = new ArrayList<>();

    @Override
    public void registerPatternRecordedListener(PatternRecordedListener patternRecordedListener) {
        if (!patternRecordedListeners.contains(patternRecordedListener)) {
            patternRecordedListeners.add(patternRecordedListener);
        } else {
            Log.w(TAG, "Attempted to register a duplicate PatternRecordedListener: " + patternRecordedListener);
        }
    }

    @Override
    public void unregisterPatternRecordedListener(PatternRecordedListener patternRecordedListener) {
        if (patternRecordedListeners.contains(patternRecordedListener)) {
            patternRecordedListeners.remove(patternRecordedListener);
        } else {
            Log.w(TAG, "Attempted to unregister a PatternRecordedListener that isn't registered: " + patternRecordedListener);
        }
    }

    @Override
    public void startRecordingPatterns() {
        Log.d(this.getClass().getSimpleName(), "Starting pattern recording");
    }

    @Override
    public void stopRecordingPatterns() {
        Log.d(this.getClass().getSimpleName(), "Stopping pattern recording");
    }

    @Override
    public void onNewPatternRecorded(Pattern pattern) {
        for (PatternRecordedListener patternRecordedListener : patternRecordedListeners) {
            try {
                patternRecordedListener.onPatternRecorded(pattern);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
