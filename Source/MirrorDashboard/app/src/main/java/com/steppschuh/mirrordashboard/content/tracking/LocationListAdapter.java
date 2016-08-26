package com.steppschuh.mirrordashboard.content.tracking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.steppschuh.mirrordashboard.R;
import com.steppschuh.mirrordashboard.request.SlackLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class LocationListAdapter extends ArrayAdapter<Location> {

    private static final String TAG = LocationListAdapter.class.getSimpleName();

    Context context;
    List<Location> locations = new ArrayList<>();

    public LocationListAdapter(Context context) {
        super(context, R.layout.transit_item);
        this.context = context;
    }

    public LocationListAdapter(Context context, List<Location> locations) {
        super(context, R.layout.transit_item, locations);
        this.context = context;
        this.locations = locations;
    }

    @Override
    public int getCount() {
        return locations.size();
    }

    @Override
    public Location getItem(int position) {
        return locations.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout transitLayout = (RelativeLayout) inflater.inflate(R.layout.location_item, parent, false);
        Location location = locations.get(position);

        TextView change = (TextView) transitLayout.findViewById(R.id.locationChange);
        change.setText(location.getReadablePassedTime(context));

        TextView description = (TextView) transitLayout.findViewById(R.id.locationDescription);
        String destinationText = location.getReadableDescription(context);
        description.setText(destinationText);

        TextView note = (TextView) transitLayout.findViewById(R.id.locationNote);
        //TODO: check if something is unusual
        note.setText("");

        return transitLayout;
    }

    @Override
    public void notifyDataSetChanged()
    {
        Collections.sort(locations, new Comparator<Location>() {
            @Override
            public int compare(Location location1, Location location2) {
                return (int) (location2.getChangeTimestamp() - location1.getChangeTimestamp());
            }
        });
        super.notifyDataSetChanged();
    }

    public void updateLocation(Location location) {
        Location existingLocation = getLocation(location);
        if (existingLocation != null) {
            if (location.getChangeTimestamp() > existingLocation.getChangeTimestamp()) {
                SlackLog.v(TAG, "Location changed: " + location.getReadableString(context));
            }
            locations.remove(existingLocation);
        }
        locations.add(location);
    }

    public Location getLocation(Location location) {
        return getLocation(location.getSubject(), location.getPlace());
    }

    public Location getLocation(String subject, String place) {
        for (Location location : locations) {
            if (!location.getSubject().equals(subject)) {
                continue;
            }
            if (!location.getPlace().equals(place)) {
                continue;
            }
            return location;
        }
        return null;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

}
