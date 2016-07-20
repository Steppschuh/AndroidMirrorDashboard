package com.steppschuh.mirrordashboard.content.weather;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentProvider;
import com.steppschuh.mirrordashboard.request.RequestHelper;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YahooWeather extends ContentProvider {

    private static final String TAG = YahooWeather.class.getSimpleName();

    // Where On Earth IDs
    public static final String WOEID_POTSDAM = "685783";

    public static final String UNIT_CELSIUS = "c";
    public static final String UNIT_FAHRENHEIT = "f";

    private String woeid;
    private String unit;

    public YahooWeather(String woeid, String unit) {
        super(Content.TYPE_WEATHER);
        this.woeid = woeid;
        this.unit = unit;
    }

    @Override
    public Content fetchContent() throws Exception {
        String url = getRequestUrl(woeid, unit);
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

    private static String getRequestUrl(String woeid, String unit) throws Exception {
        String query = new StringBuilder("select * from weather.forecast")
                .append(" where woeid = ").append(woeid)
                .append(" and u = \"").append(unit).append("\"")
                .toString();

        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String url = new StringBuilder("https://query.yahooapis.com/v1/public/yql")
                .append("?q=").append(encodedQuery)
                .append("&format=json")
                .toString();

        return url;
    }

}
