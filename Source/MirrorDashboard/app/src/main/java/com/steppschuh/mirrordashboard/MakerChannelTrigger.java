package com.steppschuh.mirrordashboard;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MakerChannelTrigger {

    public static final String TAG = MakerChannelTrigger.class.getSimpleName();

    public static final String IFTTT_MAKER_TRIGGER_ENDPOINT = "https://maker.ifttt.com/trigger/";
    public static final String EVENT_LOG = "log";
    public static final String EVENT_DEBUG = "debug";

    private String key;

    public MakerChannelTrigger(String key) {
        this.key = key;
    }

    public boolean trigger(String event, String value1) {
        return trigger(event, value1, "", "");
    }

    public boolean trigger(String event, String value1, String value2, String value3) {
        try {
            String response = trigger(key, event, value1, value2, value3);
            Log.v(TAG, "Trigger response: " + response);
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "Unable to trigger event: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public static String trigger(String key, String event, String value1, String value2, String value3) throws Exception {
        URL url = new URL(getTriggerUrl(key, event));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String input = getJsonFromValues(value1, value2, value3);
        System.out.println(input);

        OutputStream os = conn.getOutputStream();
        os.write(input.getBytes());
        os.flush();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP response code: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder outputBuilder = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            outputBuilder.append(output);
        }

        conn.disconnect();
        return outputBuilder.toString();
    }

    public static String getTriggerUrl(String key, String event) {
        return IFTTT_MAKER_TRIGGER_ENDPOINT + event + "/with/key/" + key;
    }

    public static String getJsonFromValues(String value1, String value2, String value3) throws Exception {
        return getJsonFromEncodedValues(encodeValue(value1), encodeValue(value2), encodeValue(value3));
    }

    public static String encodeValue(String value) {
        return value.replace("\n", "\\n");
    }

    public static String getJsonFromEncodedValues(String value1, String value2, String value3) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("   \"value1\": \"" + value1 + "\",\n");
        sb.append("   \"value2\": \"" + value2 + "\",\n");
        sb.append("   \"value3\": \"" + value3 + "\"\n");
        sb.append("}");
        return sb.toString();
    }

}