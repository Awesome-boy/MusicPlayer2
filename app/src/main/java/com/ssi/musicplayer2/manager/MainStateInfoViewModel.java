package com.ssi.musicplayer2.manager;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class MainStateInfoViewModel extends ViewModel {

    private static MainStateInfoViewModel sInstance;

    private final MutableLiveData<MainStateInfo> mMainStateInfo;
    private final MutableLiveData<ScanState> mScanState;

    private final MutableLiveData<Intent> mMediaServiceIntent;

    public synchronized static MainStateInfoViewModel getInstance() {
        if(sInstance == null) {
            sInstance = new MainStateInfoViewModel();
        }
        return sInstance;
    }


    private MainStateInfoViewModel() {
        mMainStateInfo = new MutableLiveData<>();
        mScanState = new MutableLiveData<>();
        mMediaServiceIntent = new MutableLiveData<>();
    }


    public void addMainStateInfoListener(LifecycleOwner owner, Observer<Object> observer) {
        mMainStateInfo.removeObserver(observer);
        mMainStateInfo.observe(owner, observer);
    }

    public void removeMainStateInfoListener(Observer<Object> observer) {
        mMainStateInfo.removeObserver(observer);
    }

    @Nullable
    public MainStateInfo getMainStateInfo() {
        return mMainStateInfo.getValue();
    }

    public void postMainStateInfo(MainStateInfo info) {
        mMainStateInfo.postValue(info);
    }




    public void addScanStateListener(LifecycleOwner owner, Observer<Object> observer) {
        mScanState.removeObserver(observer);
        mScanState.observe(owner, observer);
    }

    public void removeScanStateListener(Observer<Object> observer) {
        mScanState.removeObserver(observer);
    }

    @Nullable
    public ScanState getScanState() {
        return mScanState.getValue();
    }

    public synchronized void postScanState(ScanState info) {
        mScanState.postValue(info);
    }



    //mediaService intent
    public void addMediaServiceIntentListener(LifecycleOwner owner, Observer<Object> observer) {
        mMediaServiceIntent.removeObserver(observer);
        mMediaServiceIntent.observe(owner, observer);
    }

    public void removeMediaServiceIntentListener(Observer<Object> observer) {
        mMediaServiceIntent.removeObserver(observer);
    }

    @Nullable
    public Intent getMediaServiceIntent() {
        return mMediaServiceIntent.getValue();
    }

    public synchronized void postMediaServiceIntent(Intent intent) {
        mMediaServiceIntent.postValue(intent);
    }


}
