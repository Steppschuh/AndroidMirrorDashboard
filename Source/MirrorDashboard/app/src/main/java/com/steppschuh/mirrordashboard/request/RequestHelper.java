package com.steppschuh.mirrordashboard.request;

import com.steppschuh.mirrordashboard.BuildConfig;

import net.steppschuh.slackmessagebuilder.request.Webhook;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public final class RequestHelper {

    public static final String TAG = RequestHelper.class.getSimpleName();

    private static final String MAKER_CHANNEL_KEY = BuildConfig.MAKER_CHANNEL_KEY;
    private static final String SLACK_WEBHOOK_URL = BuildConfig.SLACK_WEBHOOK_URL;

    private static final MakerChannelTrigger makerChannelTrigger = new MakerChannelTrigger(MAKER_CHANNEL_KEY);
    private static Webhook slackWebhook = new Webhook(SLACK_WEBHOOK_URL);

    public static String get(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.connect();

        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return readStreamToString(con.getInputStream());
        } else {
            throw new IOException(readStreamToString(con.getErrorStream()));
        }
    }

    public static String post(String url, String data) throws IOException {
        boolean success = false;
        String response = null;

        URL requestUrl = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/json");

        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);

        try {
            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(data.getBytes(Charset.forName("UTF-8")));
            out.close();

            int status = urlConnection.getResponseCode();

            InputStream in;
            if (status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                in = new BufferedInputStream(urlConnection.getErrorStream());
            } else {
                in = new BufferedInputStream(urlConnection.getInputStream());
                success = true;
            }

            response = readStreamToString(in);
        } finally {
            urlConnection.disconnect();
        }

        if (!success) {
            throw new IOException(response);
        } else {
            return response;
        }
    }

    public static String readStreamToString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }
        return out.toString();
    }

    public static boolean triggerMakerChannel(String event, String value) {
        return makerChannelTrigger.trigger(event, value);
    }

    public static MakerChannelTrigger getMakerChannelTrigger() {
        return makerChannelTrigger;
    }

    public static Webhook getSlackWebhook() {
        return slackWebhook;
    }

}