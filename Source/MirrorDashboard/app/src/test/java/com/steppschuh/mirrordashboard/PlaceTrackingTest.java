package com.steppschuh.mirrordashboard;

import com.steppschuh.mirrordashboard.content.location.Location;
import com.steppschuh.mirrordashboard.content.location.PlaceTracking;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PlaceTrackingTest
{

    @Test
    public void placeTracking_providesData() throws Exception {
        PlaceTracking placeTracking = new PlaceTracking(PlaceTracking.USER_ID_STEPHAN, "Stephan");
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_WORK, "Arbeit");
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_HOME, "Zu Hause");
        Location trackedLocation = (Location) placeTracking.fetchContent();
        assertTrue(trackedLocation.getAction() != null);
    }
    
}