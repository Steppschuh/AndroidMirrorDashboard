package com.steppschuh.mirrordashboard.content.transit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.steppschuh.mirrordashboard.R;
import com.steppschuh.mirrordashboard.content.tracking.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransitListAdapter extends ArrayAdapter<Transit> {

    Context context;
    List<Transit> transits = new ArrayList<>();

    public TransitListAdapter(Context context) {
        super(context, R.layout.transit_item);
        this.context = context;
    }

    public TransitListAdapter(Context context, List<Transit> transits) {
        super(context, R.layout.transit_item, transits);
        this.context = context;
        this.transits = transits;
    }

    @Override
    public int getCount() {
        return transits.size();
    }

    @Override
    public Transit getItem(int position) {
        return transits.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout transitLayout = (RelativeLayout) inflater.inflate(R.layout.transit_item, parent, false);
        transitLayout.setAlpha(getAlpha(position));

        Transit transit = transits.get(position);

        TextView departure = (TextView) transitLayout.findViewById(R.id.transitDeparture);
        departure.setText(transit.getReadableTime());

        TextView destination = (TextView) transitLayout.findViewById(R.id.transitDestination);
        String destinationText = new StringBuilder()
                .append(transit.getDestination())
                .append(" - ")
                .append(transit.getTripId())
                .toString();
        destination.setText(destinationText);

        TextView note = (TextView) transitLayout.findViewById(R.id.transitNote);
        note.setText(transit.getNote());

        return transitLayout;
    }

    @Override
    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();
    }

    public void removeDepartedTransits() {
        List<Transit> departedTransits = new ArrayList<>();
        for (Transit transit : transits) {
            if (transit.getDeparture() < System.currentTimeMillis()) {
                departedTransits.add(transit);
            }
        }
        transits.removeAll(departedTransits);
    }

    private float getAlpha(int position) {
        int minimumCount = 3;
        double maximumDecrease = 0.34;
        boolean shouldHaveFullAlpha = position < minimumCount;
        boolean hasEnoughEntries = (transits.size() - minimumCount) > (1 / maximumDecrease);
        if (shouldHaveFullAlpha || !hasEnoughEntries) {
            return 1;
        } else {
            double decrease = Math.min(maximumDecrease, (float) 1 / (transits.size() - minimumCount));
            return Math.max(0, (float) (1 - ((position - minimumCount + 1) * decrease)));
        }
    }

    public List<Transit> getTransits() {
        return transits;
    }

    public void setTransits(List<Transit> transits) {
        this.transits = transits;
    }

}
