package com.steppschuh.mirrordashboard;

import com.steppschuh.mirrordashboard.content.weather.Weather;
import com.steppschuh.mirrordashboard.content.weather.YahooWeather;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class YahooWeatherTest {

    @Test
    public void weather_providesData() throws Exception {
        YahooWeather yahooWeather = new YahooWeather(YahooWeather.WOEID_POTSDAM, YahooWeather.UNIT_CELSIUS);
        Weather weather = (Weather) yahooWeather.fetchContent();
        assertTrue(weather.getCondition() != null);
    }
}