package com.steppschuh.mirrordashboard;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentManager;
import com.steppschuh.mirrordashboard.content.ContentUpdateListener;
import com.steppschuh.mirrordashboard.content.ContentUpdater;
import com.steppschuh.mirrordashboard.content.tracking.Location;
import com.steppschuh.mirrordashboard.content.tracking.LocationListAdapter;
import com.steppschuh.mirrordashboard.content.tracking.PlaceTracking;
import com.steppschuh.mirrordashboard.content.transit.DeutscheBahn;
import com.steppschuh.mirrordashboard.content.transit.TransitListAdapter;
import com.steppschuh.mirrordashboard.content.transit.Transits;
import com.steppschuh.mirrordashboard.content.weather.Weather;
import com.steppschuh.mirrordashboard.content.weather.YahooWeather;
import com.steppschuh.mirrordashboard.request.SlackLog;

public class DashboardActivity extends AppCompatActivity implements ContentUpdateListener {

    private static final String TAG = DashboardActivity.class.getSimpleName();

    private ContentManager contentManager;
    private View decorView;

    private TextView weatherTemperature;
    private TextView weatherDescription;
    private ImageView weatherIcon;

    private ListView transitList;
    private TransitListAdapter transitListAdapter;

    private ListView locationList;
    private LocationListAdapter locationListAdapter;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.dashboard);

        setupUi();
        setupContent();
        maximizeScreenBrightness();
        logAppStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onDestroy() {
        SlackLog.i(TAG, "App closing :pensive:");
        contentManager.unregisterContentUpdateListener(this);
        contentManager.stopAllContentUpdaters();
        super.onDestroy();
    }

    private void setupUi() {
        decorView = getWindow().getDecorView();

        weatherTemperature = (TextView) findViewById(R.id.weatherTemperature);
        weatherDescription = (TextView) findViewById(R.id.weatherDescription);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);

        transitList = (ListView) findViewById(R.id.transitList);
        transitListAdapter = new TransitListAdapter(this);
        transitList.setAdapter(transitListAdapter);

        locationList = (ListView) findViewById(R.id.locationList);
        locationListAdapter = new LocationListAdapter(this);
        locationList.setAdapter(locationListAdapter);
    }

    private void setupContent() {
        contentManager = new ContentManager();
        contentManager.registerContentUpdateListener(this);

        // weather
        String locationId = YahooWeather.WOEID_POTSDAM;
        String unit = YahooWeather.UNIT_CELSIUS;
        YahooWeather yahooWeather = new YahooWeather(locationId, unit);
        contentManager.addContentUpdater(yahooWeather);

        // transit
        String stationId = DeutscheBahn.STATION_POTSDAM_CHARLOTTENHOF;
        String language = DeutscheBahn.LANGUAGE_ENGLISH;
        DeutscheBahn deutscheBahn = new DeutscheBahn(stationId, language);
        contentManager.addContentUpdater(deutscheBahn);

        // location
        PlaceTracking placeTracking = new PlaceTracking(PlaceTracking.USER_ID_STEPHAN, "Stephan");
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_WORK, getString(R.string.location_work));
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_HOME, getString(R.string.location_home));
        contentManager.addContentUpdater(placeTracking);

        placeTracking = new PlaceTracking(PlaceTracking.USER_ID_LENA, "Lena");
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_HOME, getString(R.string.location_home));
        contentManager.addContentUpdater(placeTracking);

        contentManager.startAllContentUpdaters();
    }

    @Override
    public void onContentUpdated(final Content content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (content.getType()) {
                    case Content.TYPE_WEATHER: {
                        renderWeather((Weather) content);
                        break;
                    }
                    case Content.TYPE_TRANSIT: {
                        renderTransit((Transits) content);
                        break;
                    }case Content.TYPE_LOCATION: {
                        renderLocation((Location) content);
                        break;
                    }
                    default: {
                        Log.w(TAG, "Unable to render content, type unknown: " + content.getType());
                    }
                }
            }
        });
    }

    @Override
    public void onContentUpdateFailed(ContentUpdater contentUpdater, Exception exception) {
        SlackLog.e(TAG, contentUpdater + " failed to update content", exception);
        exception.printStackTrace();
    }

    private void renderWeather(Weather weather) {
        Log.v(TAG, "Weather updated: " + weather);

        weatherTemperature.setText(weather.getReadableTemperature());
        weatherDescription.setText(weather.getReadableTemperatureRange());
        weatherIcon.setImageResource(YahooWeather.getConditionIcon(weather.getForecastCondition()));
    }

    private void renderTransit(Transits transits) {
        Log.v(TAG, "Transit updated: " + transits);

        transits.trimNextTransits(Transits.TRANSITS_COUNT_DEFAULT);
        transitListAdapter.setTransits(transits.getNextTransits());
        transitListAdapter.notifyDataSetChanged();
    }

    private void renderLocation(Location location) {
        Log.v(TAG, "Location updated: " + location.getReadableString(this));
        locationListAdapter.updateLocation(location);
        locationListAdapter.notifyDataSetChanged();
    }

    private void hideSystemUI() {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void maximizeScreenBrightness() {
        setScreenBrightness(1f);
    }

    private void setScreenBrightness(float value) {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = value;
        getWindow().setAttributes(layout);
    }

    private void logAppStart() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            SlackLog.i(TAG, "App version " + packageInfo.versionName + " started :relaxed:");
        } catch (PackageManager.NameNotFoundException e) {
            SlackLog.e(TAG, e);
        }
    }

}
