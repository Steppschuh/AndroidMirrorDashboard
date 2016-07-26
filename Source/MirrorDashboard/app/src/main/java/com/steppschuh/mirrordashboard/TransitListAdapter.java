package com.steppschuh.mirrordashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.steppschuh.mirrordashboard.content.transit.Transit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        Transit transit = transits.get(position);

        TextView departure = (TextView) transitLayout.findViewById(R.id.transitDeparture);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);
        departure.setText(dateFormat.format(new Date(transit.getDeparture())));

        TextView destination = (TextView) transitLayout.findViewById(R.id.transitDestination);
        String destinationText = new StringBuilder()
                .append(transit.getTripId())
                .append(" - ")
                .append(transit.getDestination())
                .toString();
        destination.setText(destinationText);

        TextView note = (TextView) transitLayout.findViewById(R.id.transitNote);
        note.setText(transit.getNote());

        return transitLayout;
    }

    public List<Transit> getTransits() {
        return transits;
    }

    public void setTransits(List<Transit> transits) {
        this.transits = transits;
    }

}
