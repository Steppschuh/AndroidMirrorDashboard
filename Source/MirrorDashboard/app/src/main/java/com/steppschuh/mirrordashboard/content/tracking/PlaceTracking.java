package com.steppschuh.mirrordashboard.content.tracking;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentProvider;
import com.steppschuh.mirrordashboard.content.weather.Weather;
import com.steppschuh.mirrordashboard.request.RequestHelper;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlaceTracking extends ContentProvider {

    private static final String TAG = PlaceTracking.class.getSimpleName();

    private static final String TRACKING_API_ENDPOINT = "http://placetracking.appspot.com/api/";

    private long userId;
    private long topicId;

    private String user;
    private String place;

    public PlaceTracking() {
        super(Content.TYPE_LOCATION);
    }

    @Override
    public Content fetchContent() throws Exception {
        String url = getRequestUrl(userId, topicId);
        String jsonString = RequestHelper.get(url);

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();

        return createContent(jsonObject);
    }

    private static Content createContent(JsonObject jsonObject) {
        Weather weather = new Weather();
        JsonObject channelJson = jsonObject.getAsJsonObject("query")
                .getAsJsonObject("results")
                .getAsJsonObject("channel");
        JsonObject itemJson = channelJson.getAsJsonObject("item");

        // unit
        try {
            JsonObject unitJson = channelJson.getAsJsonObject("units");
            weather.setUnit(unitJson.get("temperature").getAsString());
        } catch (Exception ex) {
            Log.e(TAG, "Unable to parse unit: " + ex.getMessage());
        }

        // humidity
        try {
            JsonObject atmosphereJson = channelJson.getAsJsonObject("atmosphere");
            weather.setHumidity(atmosphereJson.get("humidity").getAsFloat() / 100);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to parse humidity: " + ex.getMessage());
        }

        // sunset & sunrise
        try {
            JsonObject astronomyJson = channelJson.getAsJsonObject("astronomy");
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("M/d/yy", Locale.US);
            SimpleDateFormat yahooDateFormat = new SimpleDateFormat("M/d/yy h:mm a", Locale.US);

            String sunriseString = new StringBuilder()
                    .append(currentDateFormat.format(new Date()))
                    .append(" ")
                    .append(astronomyJson.get("sunrise").getAsString())
                    .toString();
            Date sunrise = yahooDateFormat.parse(sunriseString);
            weather.setSunrise(sunrise.getTime());

            String sunsetString = new StringBuilder()
                    .append(currentDateFormat.format(new Date()))
                    .append(" ")
                    .append(astronomyJson.get("sunset").getAsString())
                    .toString();
            Date sunset = yahooDateFormat.parse(sunsetString);
            weather.setSunset(sunset.getTime());
        } catch (Exception ex) {
            Log.e(TAG, "Unable to parse sunset & sunrise: " + ex.getMessage());
        }

        // current condition
        try {
            JsonObject conditionJson = itemJson.getAsJsonObject("condition");
            weather.setCondition(conditionJson.get("text").getAsString());
            weather.setTemperature(conditionJson.get("temp").getAsInt());
        } catch (Exception ex) {
            Log.e(TAG, "Unable to parse current condition: " + ex.getMessage());
        }

        // forecast
        try {
            JsonObject forecastJson = (JsonObject) itemJson.getAsJsonArray("forecast").get(0);
            weather.setForecastHigh(forecastJson.get("high").getAsInt());
            weather.setForecastLow(forecastJson.get("low").getAsInt());
            weather.setForecastCondition(forecastJson.get("text").getAsString());
        } catch (Exception ex) {
            Log.e(TAG, "Unable to parse forecast: " + ex.getMessage());
        }

        return weather;
    }

    private static String getRequestUrl(long userId, long topicId) throws Exception {
        return new StringBuilder(TRACKING_API_ENDPOINT)
                .append("actions/get/)")
                .append("?userId=").append(userId)
                .append("&topicId=").append(topicId)
                .toString();
    }

}
