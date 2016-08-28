package com.steppschuh.mirrordashboard;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentManager;
import com.steppschuh.mirrordashboard.content.ContentUpdateListener;
import com.steppschuh.mirrordashboard.content.ContentUpdater;
import com.steppschuh.mirrordashboard.content.location.Location;
import com.steppschuh.mirrordashboard.content.location.LocationListAdapter;
import com.steppschuh.mirrordashboard.content.location.PlaceTracking;
import com.steppschuh.mirrordashboard.content.transit.DeutscheBahn;
import com.steppschuh.mirrordashboard.content.transit.TransitListAdapter;
import com.steppschuh.mirrordashboard.content.transit.Transits;
import com.steppschuh.mirrordashboard.content.weather.Weather;
import com.steppschuh.mirrordashboard.content.weather.YahooWeather;
import com.steppschuh.mirrordashboard.pattern.Pattern;
import com.steppschuh.mirrordashboard.pattern.PatternManager;
import com.steppschuh.mirrordashboard.pattern.PatternMatchedListener;
import com.steppschuh.mirrordashboard.pattern.recorder.audio.AudioPatternRecorder;
import com.steppschuh.mirrordashboard.request.SlackLog;
import com.steppschuh.mirrordashboard.util.ScreenBrightness;

import java.util.concurrent.TimeUnit;

public class DashboardActivity extends AppCompatActivity implements ContentUpdateListener, PatternMatchedListener {

    private static final String TAG = DashboardActivity.class.getSimpleName();
    private static final long SCREEN_REFRESH_INTERVAL = TimeUnit.SECONDS.toMillis(30);

    private boolean shouldRefreshScreen = false;
    private Handler screenRefreshHandler = new Handler();

    private ContentManager contentManager;
    private PatternManager patternManager;

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
        setupPatterns();
        logAppStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        startRefreshingScreen();
    }

    @Override
    protected void onPause()
    {
        stopRefreshingScreen();
        super.onPause();
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

        // Temperature content
        weatherTemperature = (TextView) findViewById(R.id.weatherTemperature);
        weatherDescription = (TextView) findViewById(R.id.weatherDescription);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);

        // Transit content
        transitList = (ListView) findViewById(R.id.transitList);
        transitListAdapter = new TransitListAdapter(this);
        transitList.setAdapter(transitListAdapter);

        // Location content
        locationList = (ListView) findViewById(R.id.locationList);
        locationListAdapter = new LocationListAdapter(this);
        locationList.setAdapter(locationListAdapter);
    }

    private void setupPatterns() {
        patternManager = new PatternManager();
        patternManager.registerPatternListener(this);

        // simple pattern to trigger whatever
        patternManager.registerPattern(Pattern.createPattern(
                Pattern.HIGH,
                Pattern.HIGH,
                Pattern.HIGH
        ));

        // register pattern recorder
        patternManager.startAndRegisterPatternRecorder(new AudioPatternRecorder());
    }

    @Override
    public void onPatternDetected(Pattern pattern) {
        SlackLog.d(TAG, "Pattern detected: " + pattern);
    }

    /**
     * Sets up the {@link ContentManager} and registers all the desired
     * {@link ContentUpdater}s
     */
    private void setupContent() {
        contentManager = new ContentManager();
        contentManager.registerContentUpdateListener(this);

        // Temperature content
        String locationId = YahooWeather.WOEID_POTSDAM;
        String unit = YahooWeather.UNIT_CELSIUS;
        YahooWeather yahooWeather = new YahooWeather(locationId, unit);
        contentManager.addContentUpdater(yahooWeather);

        // Transit content
        String stationId = DeutscheBahn.STATION_POTSDAM_CHARLOTTENHOF;
        String language = DeutscheBahn.LANGUAGE_GERMAN;
        DeutscheBahn deutscheBahn = new DeutscheBahn(stationId, language);
        contentManager.addContentUpdater(deutscheBahn);

        // Location content
        PlaceTracking placeTracking = new PlaceTracking(PlaceTracking.USER_ID_STEPHAN, "Stephan");
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_WORK, getString(R.string.location_work));
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_HOME, getString(R.string.location_home));
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_POTSDAM_CENTRAL_STATION, getString(R.string.location_potsdam_central_station));
        contentManager.addContentUpdater(placeTracking);

        placeTracking = new PlaceTracking(PlaceTracking.USER_ID_LENA, "Lena");
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_HOME, getString(R.string.location_home));
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_UNIVERSITY, getString(R.string.location_university));
        placeTracking.addTopic(PlaceTracking.TOPIC_ID_POTSDAM_CENTRAL_STATION, getString(R.string.location_potsdam_central_station));
        contentManager.addContentUpdater(placeTracking);

        // start updating content
        contentManager.startAllContentUpdaters();
    }

    /**
     * Gets called by the {@link ContentManager} if some {@link ContentUpdater}
     * has fetched new {@link Content}
     */
    @Override
    public void onContentUpdated(final Content content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (content.getType()) {
                        case Content.TYPE_WEATHER: {
                            renderWeather((Weather) content);
                            break;
                        }
                        case Content.TYPE_TRANSIT: {
                            renderTransit((Transits) content);
                            break;
                        }
                        case Content.TYPE_LOCATION: {
                            renderLocation((Location) content);
                            break;
                        }
                        default: {
                            throw new Exception("Content type unknown: " + content.getType());
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Unable to render content: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Gets called by the {@link ContentManager} if the specified {@link ContentUpdater}
     * was unable to fetch the latest {@link Content}
     */
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
        transitListAdapter.removeDepartedTransits();
        transitListAdapter.notifyDataSetChanged();
    }

    private void renderLocation(Location location) {
        Log.v(TAG, "Location updated: " + location.getReadableString(this));
        locationListAdapter.updateLocation(location);
        locationListAdapter.notifyDataSetChanged();
    }

    /**
     * Regularly invokes the {@link #refreshScreen()} method.
     * Keep in mind that this doesn't fetch new content, it just
     * re-renders the existing content
     */
    private void startRefreshingScreen() {
        try {
            if (shouldRefreshScreen) {
                throw new Exception("Screen refreshing has already been started");
            }
            shouldRefreshScreen = true;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        refreshScreen();
                    } catch (Exception ex) {
                        Log.e(TAG, "Unable to refresh screen: " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        if (shouldRefreshScreen) {
                            screenRefreshHandler.postDelayed(this, SCREEN_REFRESH_INTERVAL);
                        } else {
                            screenRefreshHandler.removeCallbacks(this);
                        }
                    }
                }
            };
            screenRefreshHandler.postDelayed(runnable, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void stopRefreshingScreen() {
        shouldRefreshScreen = false;
    }

    /**
     * Invokes a re-rendering of previously fetched content,
     * so that e.g. displayed times are up to date
     */
    private void refreshScreen() {
        Log.v(TAG, "Refreshing screen");

        // Adjust screen brightness
        float screenBrightness = ScreenBrightness.getRecommendedScreenBrightness();
        ScreenBrightness.from(getWindow()).setScreenBrightness(screenBrightness);

        // Adjust content update interval
        float intervalFactor = ContentManager.getRecommendedUpdateIntervalFactor();
        contentManager.adjustUpdateInterval(intervalFactor);

        // Location content
        locationListAdapter.notifyDataSetChanged();

        // Transit content
        transitListAdapter.removeDepartedTransits();
        transitListAdapter.notifyDataSetChanged();
    }

    /**
     * Hides all system views and leaves the app in fullscreen mode
     */
    private void hideSystemUI() {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * Shows all system views and leaves the app in default mode
     */
    private void showSystemUI() {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }



    /**
     * Reports an event to Slack with details about the currently started app
     */
    private void logAppStart() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            SlackLog.i(TAG, "App version " + packageInfo.versionName + " started :relaxed:");
        } catch (PackageManager.NameNotFoundException e) {
            SlackLog.e(TAG, e);
        }
    }

}
