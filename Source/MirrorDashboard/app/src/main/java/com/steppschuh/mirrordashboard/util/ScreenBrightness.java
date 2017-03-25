package com.steppschuh.mirrordashboard.util;

import android.view.Window;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public final class ScreenBrightness {

    public static final double INACTIVITY_BRIGHTNESS_FACTOR = 0.25;

    private static ScreenBrightness instance;
    private Window window;

    private static ScreenBrightness getInstance() {
        if (instance == null) {
            instance = new ScreenBrightness();
        }
        return instance;
    }

    public static ScreenBrightness from(Window window) {
        ScreenBrightness screenBrightness = getInstance();
        screenBrightness.window = window;
        return screenBrightness;
    }

    public void maximizeScreenBrightness() {
        setScreenBrightness(1f);
    }

    /**
     * Adjusts the device's screen brightness
     * @param value between 0 and 1
     */
    public void setScreenBrightness(float value) {
        WindowManager.LayoutParams layout = window.getAttributes();
        layout.screenBrightness = value;
        window.setAttributes(layout);
    }

    /**
     * Returns a recommended device screen brightness based on the current
     * condition
     */
    public static float getRecommendedScreenBrightness() {
        Date now = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(now);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 23 || hour <= 6) {
            return 0.33f;
        } else if (hour >= 21 || hour <= 7) {
            return 0.66f;
        } else {
            return 1f;
        }
    }

}
