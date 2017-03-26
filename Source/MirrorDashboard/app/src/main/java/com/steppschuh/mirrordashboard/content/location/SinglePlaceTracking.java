package com.steppschuh.mirrordashboard.content.location;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentProvider;
import com.steppschuh.mirrordashboard.content.ContentUpdateException;
import com.steppschuh.mirrordashboard.request.RequestHelper;

public class SinglePlaceTracking extends ContentProvider {

    private static final String TAG = SinglePlaceTracking.class.getSimpleName();

    private static final String TRACKING_API_ENDPOINT = "http://placetracking.appspot.com/api/";

    private long userId;
    private long topicId;
    private Location lastLocation;

    public SinglePlaceTracking(long userId, long topicId, Location location) {
        super(Content.TYPE_LOCATION);
        this.userId = userId;
        this.topicId = topicId;
        this.lastLocation = location;
    }

    @Override
    public Content fetchContent() throws ContentUpdateException {
        try {
            String url = getRequestUrl(userId, topicId);
            String jsonString = RequestHelper.get(url);

            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();

            lastLocation = createContent(jsonObject, lastLocation);
            return lastLocation;
        } catch (Exception e) {
            throw new ContentUpdateException(e);
        }
    }

    private static Location createContent(JsonObject jsonObject, Location lastLocation) throws Exception {
        Location location = new Location(lastLocation);

        JsonArray contentJson = jsonObject.getAsJsonArray("content");
        if (contentJson.size() < 1) {
            throw new Exception("No tracking data available for " + lastLocation);
        }

        JsonObject entryJson = (JsonObject) contentJson.get(0);
        location.setAction(entryJson.get("name").getAsString());
        location.setChangeTimestamp(entryJson.get("timestamp").getAsLong());

        return location;
    }

    private static String getRequestUrl(long userId, long topicId) {
        return new StringBuilder(TRACKING_API_ENDPOINT)
                .append("actions/get/)")
                .append("?userId=").append(userId)
                .append("&topicId=").append(topicId)
                .append("&limit=1")
                .toString();
    }

}
