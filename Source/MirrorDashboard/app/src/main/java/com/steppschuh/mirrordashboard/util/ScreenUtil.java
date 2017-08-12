package com.steppschuh.mirrordashboard.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.WINDOW_SERVICE;

public final class ScreenUtil {

    public static final double INACTIVITY_BRIGHTNESS_FACTOR = 0.25;

    private static ScreenUtil instance;
    private Window window;

    private static ScreenUtil getInstance() {
        if (instance == null) {
            instance = new ScreenUtil();
        }
        return instance;
    }

    public static ScreenUtil from(Window window) {
        ScreenUtil screenBrightness = getInstance();
        screenBrightness.window = window;
        return screenBrightness;
    }

    public void maximizeScreenBrightness() {
        setScreenBrightness(1f);
    }

    /**
     * Adjusts the device's screen brightness
     *
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

    /**
     * Returns the device screen angle in degrees.
     *
     * @param context
     * @return
     */
    public static int getRotation(Context context) {
        Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int degrees = 0;
        switch (display.getRotation()) {
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        // adjust degrees, because manufacturers failed..
        if (Build.MODEL.equals("KFJWI")) {
            degrees = (degrees + 90) % 360;
        } else if (Build.MODEL.equals("Pixel")) {
            degrees = (degrees + 270) % 360;
        } else if (Build.MODEL.equals("Nexus 9")) {
            degrees = (degrees + 270) % 360;
        } else if (Build.MODEL.equals("Nexus 5X")) {
            degrees = (degrees + 270) % 360;
        }

        //Log.v(ScreenUtil.class.getSimpleName(), Build.MODEL + " degrees: " + degrees);

        return degrees;
    }

}
