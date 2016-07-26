package com.steppschuh.mirrordashboard;

import com.steppschuh.mirrordashboard.content.transit.DeutscheBahn;
import com.steppschuh.mirrordashboard.content.transit.Transits;
import com.steppschuh.mirrordashboard.content.weather.Weather;
import com.steppschuh.mirrordashboard.content.weather.YahooWeather;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DeutscheBahnTest {

    @Test
    public void transit_providesData() throws Exception {
        DeutscheBahn deutscheBahn = new DeutscheBahn(DeutscheBahn.STATION_POTSDAM_CHARLOTTENHOF, DeutscheBahn.LANGUAGE_ENGLISH);
        Transits transits = (Transits) deutscheBahn.fetchContent();
        assertTrue(transits.getNextTransits().get(0).getDestination() != null);
    }
}