package com.steppschuh.mirrordashboard.pattern.recorder.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.steppschuh.mirrordashboard.pattern.recorder.GenericPatternRecorder;

import java.util.concurrent.TimeUnit;

public class AudioPatternRecorder extends GenericPatternRecorder {

    private static final String TAG = AudioPatternRecorder.class.getSimpleName();

    public static final int SAMPLE_RATE_DEFAULT = 8000;
    private static final int[] SAMPLE_RATES = new int[]{8000, 11025, 22050, 44100};
    private static final short[] ENCODINGS = new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT};
    private static final short[] CONFIGS = new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};
    public static final long EXTRACTION_INTERVAL_DEFAULT = 100;

    private int sampleRate;

    private int bufferSize;
    private AudioRecord audioRecord = null;

    private Handler patternExtractionHandler;
    private HandlerThread patternExtractionThread;
    private boolean shouldExtractPatterns;
    private long extractionInterval = EXTRACTION_INTERVAL_DEFAULT;

    public AudioPatternRecorder() {
        this.sampleRate = SAMPLE_RATE_DEFAULT;
    }

    @Override
    public void startRecordingPatterns() {
        super.startRecordingPatterns();
        if (audioRecord != null) {
            stopRecordingPatterns();
        }
        initializeAudioRecord();
        audioRecord.startRecording();
        startPatternExtraction();
    }

    @Override
    public void stopRecordingPatterns() {
        super.stopRecordingPatterns();
        stopPatternExtraction();
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    public void initializeAudioRecord() {
        for (int sampleRate : SAMPLE_RATES) {
            for (short encoding : ENCODINGS) {
                for (short config : CONFIGS) {
                    try {
                        Log.d(TAG, "Attempting rate " + sampleRate + "Hz, encoding: " + encoding + ", config: " + config);
                        bufferSize = AudioRecord.getMinBufferSize(sampleRate, config, encoding);
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, config, encoding, bufferSize);
                            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                                return;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        Log.e(TAG, "Unable to initialize audio record, no working config found");
        audioRecord = null;
    }

    public void startPatternExtraction() {
        try {
            shouldExtractPatterns = true;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        extractPattern();
                    } catch (Exception ex) {
                        Log.w(TAG, "Unable to extract pattern: " + ex.getMessage());
                    } finally {
                        if (shouldExtractPatterns) {
                            patternExtractionHandler.postDelayed(this, extractionInterval);
                        } else {
                            patternExtractionHandler.removeCallbacks(this);
                        }
                    }
                }
            };

            patternExtractionThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
            patternExtractionThread.start();
            patternExtractionHandler = new Handler(patternExtractionThread.getLooper());
            patternExtractionHandler.postDelayed(runnable, 50);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stopPatternExtraction() {
        try {
            shouldExtractPatterns = false;
            if (patternExtractionThread != null && patternExtractionThread.isAlive()) {
                patternExtractionThread.quit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void extractPattern() throws Exception {
        Log.v(TAG, "extractPattern(): " + getMaximumAmplitude());
    }

    public double getMaximumAmplitude() {
        if (audioRecord != null) {
            return getMaximumAmplitude(audioRecord, bufferSize / 100);
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

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

}