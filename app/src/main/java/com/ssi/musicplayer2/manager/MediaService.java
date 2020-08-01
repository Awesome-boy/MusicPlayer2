package com.ssi.musicplayer2.manager;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleService;

import com.ssi.musicplayer2.utils.Logger;


public class MediaService extends LifecycleService {
    private static final String TAG = MediaService.class.getSimpleName();

    private MainStateInfoViewModel mMainStateInfoViewModel;
    private MainManager mMainManager;

    @Override
    public void onCreate() {
        Logger.d(TAG, "onCreate");
        mMainStateInfoViewModel = MainStateInfoViewModel.getInstance();
        mMainManager = MainManager.createInstance(this, this);
        super.onCreate();
    }


    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand");
        super.onStartCommand(intent,flags,startId);
        //mMainStateInfoViewModel.postMediaServiceIntent(intent);

        mMainManager.handIntent(intent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        super.onDestroy();
    }





}
