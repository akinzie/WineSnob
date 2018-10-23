package com.britt.winesnob.winesnob;

import android.app.Application;

import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler;

public class WineApp extends Application {


    public void onCreate() {
        super.onCreate();

        // registers lifecycle callback
        registerActivityLifecycleCallbacks(AppLifecycleCallbackHandler.getInstance());
    }
}
