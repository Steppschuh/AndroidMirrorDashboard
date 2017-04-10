package com.steppschuh.mirrordashboard.firebase;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Stephan on 4/9/2017.
 */

public final class Analytics {

    private static final String EVENT_FACE_DETECTED = "face_detected";

    private static Analytics instance;
    private FirebaseAnalytics firebaseAnalytics;

    private Analytics() {
    }

    public static Analytics getInstance() {
        if (instance == null) {
            instance = new Analytics();
        }
        return instance;
    }

    public static void initialize(Context context) {
        getInstance().firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static void faceDetected() {
        Bundle bundle = new Bundle();
        //bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "face");
        getFirebaseAnalytics().logEvent(EVENT_FACE_DETECTED, bundle);
    }

    public static FirebaseAnalytics getFirebaseAnalytics() {
        return getInstance().firebaseAnalytics;
    }

}
