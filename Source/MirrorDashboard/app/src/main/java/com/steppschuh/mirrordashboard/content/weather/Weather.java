package com.steppschuh.mirrordashboard.content.weather;

import com.steppschuh.mirrordashboard.content.Content;

public class Weather extends Content {

    private int temperature;
    private String unit;
    private String condition;
    private int forecastHigh;
    private int forecastLow;
    private String forecastCondition;
    private long sunrise;
    private long sunset;
    private float humidity;

    public Weather() {
        super(Content.TYPE_WEATHER);
    }

    @Override
    public String toString() {
        return new StringBuilder("Currently ")
                .append(condition)
                .append(" with ").append(getReadableTemperature())
                .append(" (").append(getReadableTemperatureRange()).append(")")
                .append(", forecast condition: ").append(forecastCondition)
                .toString();
    }

    public String getReadableTemperatureRange() {
        return getReadableTemperatureRange(forecastLow, forecastHigh);
    }

    public static String getReadableTemperatureRange(int min, int max) {
        return new StringBuilder()
                .append(getReadableTemperature(min))
                .append("↓ ‧ ")
                .append(getReadableTemperature(max))
                .append("↑")
                .toString();
    }

    public String getReadableTemperature() {
        return getReadableTemperature(temperature);
    }

    public static String getReadableTemperature(int temperature) {
        return temperature + "°";
    }

    public static String getReadableTemperature(int temperature, String unit) {
        return getReadableTemperature(temperature) + unit;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getForecastHigh() {
        return forecastHigh;
    }

    public void setForecastHigh(int forecastHigh) {
        this.forecastHigh = forecastHigh;
    }

    public int getForecastLow() {
        return forecastLow;
    }

    public void setForecastLow(int forecastLow) {
        this.forecastLow = forecastLow;
    }

    public String getForecastCondition() {
        return forecastCondition;
    }

    public void setForecastCondition(String forecastCondition) {
        this.forecastCondition = forecastCondition;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }
}
