package com.ssi.musicplayer2;

import android.app.Application;
import android.content.Context;
import android.content.Intent;


/**
 * Created by lijunyan on 2017/2/8.
 */

public class MyApplication extends Application{
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }



    public static Context getContext() {
        return context;
    }
}
