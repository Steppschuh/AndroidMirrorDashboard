package com.steppschuh.mirrordashboard.pattern.recorder.audio;

import com.steppschuh.mirrordashboard.pattern.Pattern;
import com.steppschuh.mirrordashboard.pattern.PatternItem;

import net.steppschuh.markdowngenerator.progress.ProgressBar;
import net.steppschuh.markdowngenerator.table.TableRow;

import java.util.Arrays;

public class Amplitude implements PatternItem {

    public static final double THRESHOLD_DEFAULT = 100;

    double threshold = THRESHOLD_DEFAULT;
    double amplitude;

    public Amplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    public Amplitude(double amplitude, double threshold) {
        this(amplitude);
        this.threshold = threshold;
    }

    @Override
    public int getPatternValue() {
        return (amplitude > threshold) ? Pattern.HIGH : Pattern.LOW;
    }

    @Override
    public String toString() {
        return toTableRow().toString();
    }

    public TableRow getTableHeaders() {
        return new TableRow<>(Arrays.asList(
                "Value",
                "Amplitude",
                "Threshold"
        ));
    }

    public TableRow toTableRow() {
        ProgressBar amplitudeBar = new ProgressBar(amplitude);
        amplitudeBar.setMaximumValue(threshold);

        return new TableRow<>(Arrays.asList(
                getPatternValue(),
                amplitudeBar,
                String.format("%.2f", threshold)
        ));
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }
}
