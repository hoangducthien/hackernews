package com.hoangthien.hackernews.ui;

import android.app.Application;

import com.hoangthien.hackernews.utils.ImageLoader;

/**
 * Created by thien on 5/11/17.
 */

public class HackerNewsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.getInstance(this);
    }
}
