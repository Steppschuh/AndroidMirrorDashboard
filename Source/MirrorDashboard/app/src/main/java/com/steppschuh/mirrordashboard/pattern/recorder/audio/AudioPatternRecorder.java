package com.steppschuh.mirrordashboard.pattern.recorder.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.steppschuh.mirrordashboard.pattern.recorder.GenericPatternRecorder;

public class AudioPatternRecorder extends GenericPatternRecorder {

    private static final String TAG = AudioPatternRecorder.class.getSimpleName();

    public static final int SAMPLE_RATE_DEFAULT = 8000;

    private int sampleRate;
    private int bufferSize;
    private AudioRecord audioRecord = null;

    public AudioPatternRecorder(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public void startRecordingPatterns() {
        super.startRecordingPatterns();
        if (audioRecord != null) {
            stopRecordingPatterns();
        }
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        audioRecord.startRecording();
    }

    @Override
    public void stopRecordingPatterns() {
        super.stopRecordingPatterns();
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord = null;
        }
    }

    public double getMaximumAmplitude() {
        if (audioRecord != null) {
            return getMaximumAmplitude(audioRecord, bufferSize);
        }
        return 0;
    }

    public static double getMaximumAmplitude(AudioRecord audioRecord, int bufferSize) {
        short[] buffer = new short[bufferSize];
        audioRecord.read(buffer, 0, bufferSize);
        int maximumAmplitude = 0;
        for (short s : buffer) {
            if (Math.abs(s) > maximumAmplitude) {
                maximumAmplitude = Math.abs(s);
            }
        }
        return maximumAmplitude;
    }

    public int getSampleRate()
    {
        return sampleRate;
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    public AudioRecord getAudioRecord()
    {
        return audioRecord;
    }

}