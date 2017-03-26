package com.steppschuh.mirrordashboard.content.location;

import android.util.Log;

import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentProvider;
import com.steppschuh.mirrordashboard.content.ContentUpdateException;

import java.util.HashMap;
import java.util.Map;

public class PlaceTracking extends ContentProvider {

    /**
     * This provider fetches its data from the PlaceTracking API,
     * which is an open-source project powered by the Google App Engine.
     * <p>
     * You need to create you own user and topic IDs (using simple get
     * requests, no sign up required), here's how:
     * https://github.com/Steppschuh/PlaceTracking
     */

    private static final String TAG = PlaceTracking.class.getSimpleName();

    public static final long USER_ID_STEPHAN = 5659313586569216l;
    public static final long USER_ID_LENA = 5629935204958208l;

    public static final long TOPIC_ID_WORK = 5656904915222528l;
    public static final long TOPIC_ID_HOME = 5725273681035264l;
    public static final long TOPIC_ID_UNIVERSITY = 5653495919149056l;
    public static final long TOPIC_ID_POTSDAM_CENTRAL_STATION = 5744581840732160l;

    private long subjectId;
    private String subjectName;
    private HashMap<Long, String> topics = new HashMap<>();
    private HashMap<Long, Location> locations = new HashMap<>();
    private HashMap<Long, SinglePlaceTracking> locationProviders = new HashMap<>();

    public PlaceTracking(long subjectId, String subjectName) {
        super(Content.TYPE_LOCATION);
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    @Override
    public Content fetchContent() throws ContentUpdateException {
        for (Map.Entry<Long, SinglePlaceTracking> placeTrackerEntry : locationProviders.entrySet()) {
            try {
                Location location = (Location) placeTrackerEntry.getValue().fetchContent();
                setLocation(placeTrackerEntry.getKey(), location);
            } catch (Exception ex) {
                Log.w(TAG, "Unable to update location: " + placeTrackerEntry.getValue() + ": " + ex.getMessage());
            }
        }
        Location latestLocation = getLatestLocation();
        if (latestLocation == null) {
            throw new ContentUpdateException("No location data available");
        }
        return latestLocation;
    }

    public void addTopic(long topicId, String topicName) {
        topics.put(topicId, topicName);

        if (!locations.containsKey(topicId)) {
            Location location = new Location();
            location.setSubject(subjectName);
            location.setPlace(topicName);
            locations.put(topicId, location);
        }

        if (!locationProviders.containsKey(topicId)) {
            SinglePlaceTracking placeTracking = new SinglePlaceTracking(subjectId, topicId, locations.get(topicId));
            locationProviders.put(topicId, placeTracking);
        }
    }

    public boolean setLocation(long topicId, Location location) {
        if (!topics.containsKey(topicId)) {
            Log.w(TAG, "Unable to set location, topic is unknown: " + topicId);
            return false;
        }
        locations.put(topicId, location);
        return true;
    }

    public long getLatestTopicId() {
        long latestTopicId = -1;
        long latestChangeTimestamp = 0;
        for (Map.Entry<Long, Location> locationEntry : locations.entrySet()) {
            long currentChangeTimestamp = locationEntry.getValue().getChangeTimestamp();
            if (currentChangeTimestamp > latestChangeTimestamp) {
                latestChangeTimestamp = currentChangeTimestamp;
                latestTopicId = locationEntry.getKey();
            }
        }
        return latestTopicId;
    }

    public Location getLatestLocation() {
        long latestTopicId = getLatestTopicId();
        if (!locations.containsKey(latestTopicId)) {
            return null;
        }
        return locations.get(latestTopicId);
    }

}
