package com.steppschuh.mirrordashboard.pattern.recorder;

import com.steppschuh.mirrordashboard.pattern.Pattern;
import com.steppschuh.mirrordashboard.pattern.PatternRecordedListener;

public interface PatternRecorder {

    void startRecordingPatterns();

    void stopRecordingPatterns();

    void registerPatternRecordedListener(PatternRecordedListener patternRecordedListener);

    void unregisterPatternRecordedListener(PatternRecordedListener patternRecordedListener);

    void onNewPatternRecorded(Pattern pattern);

}
