package com.ssi.musicplayer2.btFragment.presenter;


import android.os.Bundle;

import com.ssi.musicplayer2.btFragment.client.BluetoothMusicCase;
import com.ssi.musicplayer2.btFragment.client.UseCase;
import com.ssi.musicplayer2.btFragment.intf.BluetoothMusicContract;
import com.ssi.musicplayer2.btFragment.intf.UseCaseHandler;
import com.ssi.musicplayer2.utils.Logger;


public class BluetoothMusicPresenter implements BluetoothMusicContract.PlayControllerPresenter
        , UseCase.UseCaseCallback<BluetoothMusicCase.BluetoothMusicResponseValue>{

    private static final String TAG = BluetoothMusicPresenter.class.getSimpleName();

    private UseCaseHandler mCaseHandler = null;
    private BluetoothMusicContract.BluetoothMusicView mView = null;
    private BluetoothMusicCase mBluetoothMusicCase = null;
    public BluetoothMusicPresenter(UseCaseHandler handler,
                                   BluetoothMusicContract.BluetoothMusicView view, BluetoothMusicCase bluetoothCase) {
        
        mCaseHandler = handler;
        mView = view;
        mBluetoothMusicCase = bluetoothCase;

        mView.setPresenter(this);
        mCaseHandler.setUseCaseCallback(mBluetoothMusicCase, this);
    }


    //---------------实现BluetoothMusicContract.PlayControllerPresenter------------------------------------------
    @Override
    public void start() {
        Logger.d(TAG, "start");
        mBluetoothMusicCase.onStart();
        
    }


    @Override
    public void stop() {
        Logger.d(TAG, "stop");
        mBluetoothMusicCase.onStop();
        mView = null;
        
    }

    @Override
    public void skipToPrevious(){
        BluetoothMusicCase.BluetoothMusicRequestValues requestValues = new BluetoothMusicCase.BluetoothMusicRequestValues();
        requestValues.mPlayAction = BluetoothMusicCase.BluetoothMusicRequestValues.PREVIOUS_ACTION;
        executeAction(requestValues);
    }

    @Override
    public void pause() {
        BluetoothMusicCase.BluetoothMusicRequestValues requestValues = new BluetoothMusicCase.BluetoothMusicRequestValues();
        requestValues.mPlayAction = BluetoothMusicCase.BluetoothMusicRequestValues.PAUSE_ACTION;
        executeAction(requestValues);
        
    }


    @Override
    public void play() {
        BluetoothMusicCase.BluetoothMusicRequestValues requestValues = new BluetoothMusicCase.BluetoothMusicRequestValues();
        requestValues.mPlayAction = BluetoothMusicCase.BluetoothMusicRequestValues.PLAYA_CTION;
        executeAction(requestValues);
        
    }

    @Override
    public void skipToNext() {
        BluetoothMusicCase.BluetoothMusicRequestValues requestValues = new BluetoothMusicCase.BluetoothMusicRequestValues();
        requestValues.mPlayAction = BluetoothMusicCase.BluetoothMusicRequestValues.NEXT_ACTION;
        executeAction(requestValues);
        
    }


    @Override
    public void seekTo(long progress) {
        BluetoothMusicCase.BluetoothMusicRequestValues requestValues = new BluetoothMusicCase.BluetoothMusicRequestValues();
        requestValues.mPlayAction = BluetoothMusicCase.BluetoothMusicRequestValues.SEEKTO;
        requestValues.mSeekTo = progress;
        executeAction(requestValues);        
    }


    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        BluetoothMusicCase.BluetoothMusicRequestValues requestValues = new BluetoothMusicCase.BluetoothMusicRequestValues();
        requestValues.mPlayAction = BluetoothMusicCase.BluetoothMusicRequestValues.PLAY_FROM_MEDIA_ID;
        executeAction(requestValues);           
    }

    @Override
    public void subscribe() {
        BluetoothMusicCase.BluetoothMusicRequestValues requestValues = new BluetoothMusicCase.BluetoothMusicRequestValues();
        requestValues.mPlayAction = BluetoothMusicCase.BluetoothMusicRequestValues.SUBSCRIBE;
        executeAction(requestValues);
    }

    //----------------实现UseCase.UseCaseCallback接口------------------
    @Override
    public void onSuccess(BluetoothMusicCase.BluetoothMusicResponseValue response) {
        Logger.d(TAG, "onSuccess response.mResponseAction:" + response.mResponseAction);
        if(mView == null) {
            Logger.w(TAG, "onSuccess return for null");
            return;
        }
        switch (response.mResponseAction) {
            case BluetoothMusicCase.BluetoothMusicResponseValue.RES_ACTION_METADATACHANGE:
                mView.onMetadataChanged(response.mMediaMetadata);
                break;
            case BluetoothMusicCase.BluetoothMusicResponseValue.RES_ACTION_PLAYBACKSTATECHANGE:
                mView.onPlaybackStateChanged(response.mPlaybackState);
                break;
            case BluetoothMusicCase.BluetoothMusicResponseValue.RES_ACTION_CONNECTED:
                mView.onConnected();
                break;
            default:
                Logger.e(TAG, "onSuccess error key!!!");
                break;
        }
        
    }


    @Override
    public void onError() {
        Logger.d(TAG, "onError response");
    }





    private void executeAction(BluetoothMusicCase.BluetoothMusicRequestValues requestValues) {
        mCaseHandler.execute(mBluetoothMusicCase, requestValues, this);
    }
    





}







