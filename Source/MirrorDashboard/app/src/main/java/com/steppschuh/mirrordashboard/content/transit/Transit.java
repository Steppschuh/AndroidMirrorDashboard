package com.steppschuh.mirrordashboard.content.transit;

import com.steppschuh.mirrordashboard.content.Content;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Transit extends Content {

    private String tripId;
    private String source;
    private String destination;
    private List<String> stops;
    private long departure;
    private String platform;
    private String note;

    public Transit() {
        super(Content.TYPE_TRANSIT);
    }

    public String getReadableTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
        return dateFormat.format(new Date(departure));
    }

    @Override
    public String toString() {
        return new StringBuilder(getReadableTime())
                .append(" - ").append(tripId)
                .append(" to ").append(destination)
                .toString();
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<String> getStops() {
        return stops;
    }

    public void setStops(List<String> stops) {
        this.stops = stops;
    }

    public long getDeparture() {
        return departure;
    }

    public void setDeparture(long departure) {
        this.departure = departure;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
