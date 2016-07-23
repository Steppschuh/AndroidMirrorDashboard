package com.steppschuh.mirrordashboard.content.weather;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.steppschuh.mirrordashboard.R;
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

    public static int getConditionIcon(String condition) {
        /*
        0	tornado
        1	tropical storm
        2	hurricane
        3	severe thunderstorms
        4	thunderstorms
        5	mixed rain and snow
        6	mixed rain and sleet
        7	mixed snow and sleet
        8	freezing drizzle
        9	drizzle
        10	freezing rain
        11	showers
        12	showers
        13	snow flurries
        14	light snow showers
        15	blowing snow
        16	snow
        17	hail
        18	sleet
        19	dust
        20	foggy
        21	haze
        22	smoky
        23	blustery
        24	windy
        25	cold
        26	cloudy
        27	mostly cloudy (night)
        28	mostly cloudy (day)
        29	partly cloudy (night)
        30	partly cloudy (day)
        31	clear (night)
        32	sunny
        33	fair (night)
        34	fair (day)
        35	mixed rain and hail
        36	hot
        37	isolated thunderstorms
        38	scattered thunderstorms
        39	scattered thunderstorms
        40	scattered showers
        41	heavy snow
        42	scattered snow showers
        43	heavy snow
        44	partly cloudy
        45	thundershowers
        46	snow showers
        47	isolated thundershowers
         */

        String description = condition.toLowerCase();

        if (description.contains("cloudy")) {
            if (description.contains("partly")) {
                return R.drawable.weather_cloud_day;
            } else {
                return R.drawable.weather_cloud;
            }
        } else if (description.contains("showers") ||description.contains("snow")) {
            return R.drawable.weather_rain;
        } else if (description.contains("thunder")) {
            return R.drawable.weather_thunder;
        } else if (description.contains("clear") || description.contains("fair") || description.contains("hot")) {
            return R.drawable.weather_sun;
        }

        return R.drawable.weather_cloud;
    }

}
