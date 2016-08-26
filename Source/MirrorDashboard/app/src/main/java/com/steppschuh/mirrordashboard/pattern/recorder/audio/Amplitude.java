package com.steppschuh.mirrordashboard.pattern.recorder.audio;

import com.steppschuh.mirrordashboard.pattern.Pattern;
import com.steppschuh.mirrordashboard.pattern.PatternItem;

public class Amplitude implements PatternItem
{

    public static final double THRESHOLD_DEFAULT = 10;

    double threshold = THRESHOLD_DEFAULT;
    double amplitude;

    public Amplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    public Amplitude(double amplitude, double threshold)
    {
        this(amplitude);
        this.threshold = threshold;
    }

    @Override
    public int getPatternValue()
    {
        if (amplitude > threshold) {
            return Pattern.HIGH;
        } else {
            return Pattern.LOW;
        }
    }
}
