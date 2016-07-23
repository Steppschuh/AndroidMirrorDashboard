package com.steppschuh.mirrordashboard;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.content.ContentManager;
import com.steppschuh.mirrordashboard.content.ContentUpdateListener;
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

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

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
    }

    private void setupContent() {
        contentManager = new ContentManager();
        contentManager.registerContentUpdateListener(this);

        YahooWeather yahooWeather = new YahooWeather(YahooWeather.WOEID_POTSDAM, YahooWeather.UNIT_CELSIUS);
        contentManager.addContentUpdater(yahooWeather);

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
                    default: {
                        Log.w(TAG, "Unable to render content, type unknown: " + content.getType());
                    }
                }
            }
        });
    }

    @Override
    public void onContentUpdateFailed(Exception exception) {
        SlackLog.e(TAG, exception);
    }

    private void renderWeather(Weather weather) {
        Log.v(TAG, "Weather updated: " + weather);

        weatherTemperature.setText(weather.getReadableTemperature());
        weatherDescription.setText(weather.getReadableTemperatureRange());
        weatherIcon.setImageResource(YahooWeather.getConditionIcon(weather.getForecastCondition()));
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
