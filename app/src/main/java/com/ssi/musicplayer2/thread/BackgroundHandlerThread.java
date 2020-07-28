package com.ssi.musicplayer2.thread;


import android.os.HandlerThread;

import androidx.annotation.UiThread;


import com.ssi.musicplayer2.utils.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 后台线程
 */
public class BackgroundHandlerThread extends HandlerThread {
    private static final String TAG =  "lib"+BackgroundHandlerThread.class.getSimpleName();



    private AtomicInteger mStartCount;
    private static BackgroundHandlerThread mInstance;



    public synchronized static BackgroundHandlerThread getInstance() {
        if(mInstance == null) {
            mInstance = new BackgroundHandlerThread();
        }
        return mInstance;
    }





    private BackgroundHandlerThread() {
        super(TAG);
        mStartCount = new AtomicInteger(0);
    }


    @Override
    public void run() {
        super.run();
        Logger.w(TAG , "run");
    }


    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
    }


    private boolean isDirectStart = true;

    @UiThread
    public void doStart() {
        if(mStartCount.incrementAndGet() <=1) {
            isDirectStart = false;
            start();
            isDirectStart = true;
        }

    }

    @UiThread
    public void doStop() {
        if(mStartCount.decrementAndGet() <= 0) {
            quitSafely();
            mInstance = null;
        }

    }


    @Override
    public synchronized void start() {
        Logger.w(TAG, "start :" + isDirectStart);
        if(isDirectStart) {
            throw new IllegalArgumentException("please call doStart method!!!");
        }
        super.start();
    }
}

