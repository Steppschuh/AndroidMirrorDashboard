package com.steppschuh.mirrordashboard.content.transit;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentProvider;
import com.steppschuh.mirrordashboard.content.weather.Weather;
import com.steppschuh.mirrordashboard.request.RequestHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DeutscheBahn extends ContentProvider {

    /**
     * This provider fetches its data from an extracty.com endpoint,
     * which parses the official "Web-Bahnhofstafel" by DB.
     *
     * To get you local station ID, open this page and copy the "bhf" url
     * parameter from the generated URL:
     * http://aseier.de/bahn/a.html
     */

    private static final String TAG = DeutscheBahn.class.getSimpleName();

    public static final String EXTRACTLY_ENDPOINT = "https://api.extracty.com/v1/353/";
    public static final String EXTRACTLY_TOKEN = "7bc28381-c6f9-438a-89f7-21635782f44a";

    public static final String STATION_POTSDAM_CHARLOTTENHOF = "8010280";

    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_GERMAN = "de";

    private String stationId;
    private String language;

    public DeutscheBahn(String stationId, String language) {
        super(Content.TYPE_TRANSIT);
        this.stationId = stationId;
        this.language = language;
    }

    @Override
    public Content fetchContent() throws Exception {
        String url = getRequestUrl(stationId, language);
        String jsonString = RequestHelper.get(url);

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();

        return createContent(jsonObject);
    }

    private static Content createContent(JsonObject jsonObject) {
        List<Transit> transitList = new ArrayList<>();
        JsonArray transitsJson = jsonObject.getAsJsonArray("result");
        Iterator<JsonElement> transitsIterator = transitsJson.iterator();

        while (transitsIterator.hasNext()) {
            try {
                Transit transit = new Transit();
                JsonObject transitJson = (JsonObject) transitsIterator.next();

                // tripId, destination, note & platform
                transit.setTripId(transitJson.get("tripId").getAsString());
                transit.setDestination(transitJson.get("destination").getAsString());
                transit.setNote(transitJson.get("note").getAsString());
                transit.setPlatform(transitJson.get("platform").getAsString());

                // departure
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("M/d/yy", Locale.US);
                SimpleDateFormat departureDateFormat = new SimpleDateFormat("M/d/yy HH:mm", Locale.US);

                String departureString = new StringBuilder()
                        .append(currentDateFormat.format(new Date()))
                        .append(" ")
                        .append(transitJson.get("departure").getAsString())
                        .toString();
                Date departure = departureDateFormat.parse(departureString);
                transit.setDeparture(departure.getTime());

                // stops
                String stopsCSV = transitJson.get("stops").getAsString();
                if (stopsCSV.length() > 0) {
                    transit.setStops(Arrays.asList(stopsCSV.split("\\s*,\\s*")));
                } else {
                    transit.setStops(new ArrayList<String>());
                }

                // avoid duplicates because of split trains
                if (transitList.size() > 0) {
                    Transit lastTransit = transitList.get(transitList.size() - 1);
                    boolean sameDestination = lastTransit.getDestination().equals(transit.getDestination());
                    boolean sameDeparture = lastTransit.getDeparture() == lastTransit.getDeparture();
                    if (sameDestination && sameDeparture) {
                        continue;
                    }
                }

                transitList.add(transit);
            } catch (Exception ex) {
                Log.e(TAG, "Unable to parse transit: " + ex.getMessage());
            }
        }

        return new Transits(transitList);
    }

    private static String getRequestUrl(String stationId, String language) {
        long rounding = TimeUnit.MINUTES.toMillis(1);
        long roundedTimestamp = Math.round(System.currentTimeMillis() / rounding) * rounding;
        String url = new StringBuilder(EXTRACTLY_ENDPOINT)
                .append(EXTRACTLY_TOKEN)
                .append("/?stopId=").append(stationId)
                .append("&language=").append(language)
                .append("&timestamp=").append(roundedTimestamp) // invalidate cache
                .toString();

        return url;
    }

}
