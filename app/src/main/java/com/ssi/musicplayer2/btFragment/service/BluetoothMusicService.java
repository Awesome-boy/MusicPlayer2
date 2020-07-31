package com.ssi.musicplayer2.btFragment.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.lifecycle.LifecycleService;


import com.ssi.musicplayer2.btFragment.client.BluetoothConnectionHelper;
import com.ssi.musicplayer2.btFragment.client.BluetoothMusicMediaBrowserHelper;
import com.ssi.musicplayer2.btFragment.notifications.BluetoothMediaNotificationManager;
import com.ssi.musicplayer2.utils.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothMusicService extends LifecycleService implements BluetoothConnectionHelper.ConnectionStateListener{


    private static final String TAG = BluetoothMusicService.class.getSimpleName();

    public static final AtomicBoolean mServiceIsStart = new AtomicBoolean(false);

    private BluetoothConnectionHelper mBluetoothConnectionHelper = null;
    //private BluetoothMusicMediaBrowserHelper mMusicMediaBrowserHelper = null;
    //private BluetoothMediaNotificationManager mMediaNotificationManager = null;



    public BluetoothMusicService() {
        super();

    }


    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }


    @Override
    public void onCreate() {
        Logger.w(TAG, "onCreate");
        mServiceIsStart.set(true);
        final Context appContext = getApplicationContext();
        mBluetoothConnectionHelper = BluetoothConnectionHelper.createInstance(appContext, this);
        mBluetoothConnectionHelper.addConnectionListenr(this);
        /*mMediaNotificationManager = */
        BluetoothMediaNotificationManager.createInstance(this, this);
        /*mMusicMediaBrowserHelper =*/
        BluetoothMusicMediaBrowserHelper.createInstance(appContext, Utils.getBTMediaBrowserService(appContext), this);
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.w(TAG, "onStartCommand");
        super.onStartCommand(intent, flags,startId);
        mBluetoothConnectionHelper.handleBTState(intent);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Logger.w(TAG, "onDestroy");
        mServiceIsStart.set(false);
        if(mBluetoothConnectionHelper != null) {
            mBluetoothConnectionHelper.rmConnectionListenr(this);
            mBluetoothConnectionHelper = null;
        }
        super.onDestroy();

    }


    //BluetoothConnectionHelper.ConnectionStateListener
    @Override
    public void onConnectionStateChanged(boolean isConnectioned) {
        Logger.d(TAG, "onConnectionStateChanged isConnectioned:" + isConnectioned);
        //if(!isConnectioned) {
            //Logger.d(TAG, "onConnectionStateChanged stopSelf");
            //stopSelf();
      //  }

    }
}