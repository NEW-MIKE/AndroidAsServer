package com.is.androidServer;

import android.app.Application;

import com.is.androidServer.utils.AppUtils;


public class App  extends Application {

    private static App sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        AppUtils.init(this);
    }
    public static App getsInstance() {
        return sInstance;
    }

}
