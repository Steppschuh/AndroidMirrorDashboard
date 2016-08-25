package com.steppschuh.mirrordashboard;

import com.steppschuh.mirrordashboard.content.tracking.Location;
import com.steppschuh.mirrordashboard.content.tracking.PlaceTracking;
import com.steppschuh.mirrordashboard.content.tracking.SinglePlaceTracking;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SinglePlaceTrackingTest
{

    @Test
    public void singlePlaceTracking_providesData() throws Exception {
        long userId = PlaceTracking.USER_ID_STEPHAN;
        long topicId = PlaceTracking.TOPIC_ID_WORK;
        Location location = new Location();
        location.setSubject("Stephan");
        location.setPlace("Work");

        SinglePlaceTracking placeTracking = new SinglePlaceTracking(userId, topicId, location);
        Location trackedLocation = (Location) placeTracking.fetchContent();
        assertTrue(trackedLocation.getAction() != null);
    }

}