package com.steppschuh.mirrordashboard.pattern;

import android.util.Log;

import com.steppschuh.mirrordashboard.pattern.recorder.PatternRecorder;

import java.util.ArrayList;
import java.util.List;

public class PatternManager implements PatternRecordedListener {

    private static final String TAG = PatternManager.class.getSimpleName();

    private List<PatternMatchedListener> patternMatchedListeners = new ArrayList<>();
    private List<PatternRecorder> patternRecorders = new ArrayList<>();
    private List<Pattern> patterns = new ArrayList<>();

    @Override
    public void onPatternRecorded(Pattern pattern) {
        checkPattern(pattern);
    }

    public boolean checkPattern(Pattern otherPattern) {
        Pattern matchingPattern = getMatchingPattern(otherPattern);
        if (matchingPattern == null) {
            return false;
        }

        for (PatternMatchedListener patternMatchedListener : patternMatchedListeners) {
            try {
                patternMatchedListener.onPatternDetected(matchingPattern);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    private Pattern getMatchingPattern(Pattern otherPattern) {
        for (Pattern pattern : patterns) {
            if (pattern.matches(otherPattern)) {
                return pattern;
            }
        }
        return null;
    }

    public void registerPatternListener(PatternMatchedListener patternMatchedListener) {
        if (!patternMatchedListeners.contains(patternMatchedListener)) {
            patternMatchedListeners.add(patternMatchedListener);
        } else {
            Log.w(TAG, "Attempted to register a duplicate PatternMatchedListener: " + patternMatchedListener);
        }
    }

    public void unregisterContentUpdateListener(PatternMatchedListener patternMatchedListener) {
        if (patternMatchedListeners.contains(patternMatchedListener)) {
            patternMatchedListeners.remove(patternMatchedListener);
        } else {
            Log.w(TAG, "Attempted to unregister a PatternMatchedListener that isn't registered: " + patternMatchedListener);
        }
    }

    public void startAndRegisterPatternRecorder(PatternRecorder patternRecorder) {
        registerPatternRecorder(patternRecorder);
        patternRecorder.startRecordingPatterns();
    }

    public void registerPatternRecorder(PatternRecorder patternRecorder) {
        if (!patternRecorders.contains(patternRecorder)) {
            patternRecorders.add(patternRecorder);
        } else {
            Log.w(TAG, "Attempted to register a duplicate PatternRecorder: " + patternRecorder);
        }
    }

    public void unregisterPatternRecorder(PatternRecorder patternRecorder) {
        if (patternRecorders.contains(patternRecorder)) {
            patternRecorders.remove(patternRecorder);
        } else {
            Log.w(TAG, "Attempted to unregister a PatternRecorder that isn't registered: " + patternRecorder);
        }
    }

    public void registerPattern(Pattern pattern) {
        if (!patterns.contains(pattern)) {
            patterns.add(pattern);
        } else {
            Log.w(TAG, "Attempted to register a duplicate Pattern: " + pattern);
        }
    }

    public void unregisterPattern(Pattern pattern) {
        if (patterns.contains(pattern)) {
            patterns.remove(pattern);
        } else {
            Log.w(TAG, "Attempted to unregister a Pattern that isn't registered: " + pattern);
        }
    }

}
