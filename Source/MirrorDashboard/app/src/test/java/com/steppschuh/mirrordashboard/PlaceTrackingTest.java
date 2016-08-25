package com.steppschuh.mirrordashboard;

import com.steppschuh.mirrordashboard.content.tracking.Location;
import com.steppschuh.mirrordashboard.content.tracking.PlaceTracking;
import com.steppschuh.mirrordashboard.content.tracking.SinglePlaceTracking;

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