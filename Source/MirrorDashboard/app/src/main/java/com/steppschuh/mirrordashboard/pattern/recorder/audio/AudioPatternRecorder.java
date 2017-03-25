package com.steppschuh.mirrordashboard.pattern.recorder.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.steppschuh.mirrordashboard.pattern.Pattern;
import com.steppschuh.mirrordashboard.pattern.recorder.GenericPatternRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AudioPatternRecorder extends GenericPatternRecorder {

    private static final String TAG = AudioPatternRecorder.class.getSimpleName();

    public static final int SAMPLE_RATE_DEFAULT = 8000;
    private static final int[] SAMPLE_RATES = new int[]{8000, 11025, 22050, 44100};
    private static final short[] ENCODINGS = new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT};
    private static final short[] CONFIGS = new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};

    public static final long EXTRACTION_INTERVAL_DEFAULT = 200;
    private static final int RECENT_AMPLITUDES_COUNT = (int) (TimeUnit.SECONDS.toMillis(5) / EXTRACTION_INTERVAL_DEFAULT);

    private int sampleRate;
    private int bufferSize;
    private AudioRecord audioRecord = null;

    private Handler patternExtractionHandler;
    private HandlerThread patternExtractionThread;
    private boolean shouldExtractPatterns;
    private long extractionInterval = EXTRACTION_INTERVAL_DEFAULT;

    private List<Amplitude> recentAmplitudes = new ArrayList<>();

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
        if (audioRecord != null) {
            audioRecord.startRecording();
            startPatternExtraction();
        }
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
                        processCurrentAudio();
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

    private void processCurrentAudio() {
        // get and process latest amplitude
        Amplitude amplitude = getCurrentAmplitude();
        recentAmplitudes.add(amplitude);
        while (recentAmplitudes.size() > RECENT_AMPLITUDES_COUNT) {
            recentAmplitudes.remove(0);
        }

        //Log.v(TAG, "Most recent amplitude: " + amplitude);

        // detect and process patterns
        List<Pattern> amplitudePatterns = Pattern.extractPatternsFromSequence(recentAmplitudes);
        for (Pattern amplitudePattern : amplitudePatterns) {
            onNewPatternRecorded(amplitudePattern);
        }
    }

    private Amplitude getCurrentAmplitude() {
        short[] currentAmplitudes = readAmplitudes(audioRecord, bufferSize);
        double currentMaximumAmplitude = getMaximumAmplitude(currentAmplitudes);
        double currentAverageAmplitude = getAverageAmplitude(currentAmplitudes);
        double recentAverageAmplitude = getRecentAverageAmplitude();

        double value = currentMaximumAmplitude - currentAverageAmplitude;
        double threshold = recentAverageAmplitude * 7.5;
        return new Amplitude(value, threshold);
    }

    private double getRecentAverageAmplitude() {
        if (recentAmplitudes.isEmpty()) {
            return 0;
        }
        double amplitudeSum = 0;
        for (Amplitude amplitude : recentAmplitudes) {
            amplitudeSum += amplitude.getAmplitude();
        }
        return amplitudeSum / recentAmplitudes.size();
    }

    public static short[] readAmplitudes(AudioRecord audioRecord, int bufferSize) {
        short[] buffer = new short[bufferSize];
        if (audioRecord != null && bufferSize > 0) {
            audioRecord.read(buffer, 0, bufferSize);
        }
        return buffer;
    }

    public static double getMaximumAmplitude(short[] amplitudes) {
        int maximumAmplitude = 0;
        int amplitude;
        for (int i = 0; i < amplitudes.length; i++) {
            amplitude = amplitudes[i] * amplitudes[i];
            if (amplitude > maximumAmplitude) {
                maximumAmplitude = amplitude;
            }
        }
        return Math.sqrt(maximumAmplitude);
    }

    public static double getAverageAmplitude(short[] amplitudes) {
        double averageAmplitude = 0;
        int t = 1;
        for (int i = 0; i < amplitudes.length; i++) {
            averageAmplitude += (Math.abs(amplitudes[i]) - averageAmplitude) / t;
            ++t;
        }
        return averageAmplitude;
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