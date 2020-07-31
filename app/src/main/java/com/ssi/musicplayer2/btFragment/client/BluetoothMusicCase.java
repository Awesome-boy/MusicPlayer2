package com.ssi.musicplayer2.btFragment.client;


import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.ssi.musicplayer2.utils.Logger;


public class BluetoothMusicCase extends 
        UseCase<BluetoothMusicCase.BluetoothMusicRequestValues, 
        BluetoothMusicCase.BluetoothMusicResponseValue>
        implements BluetoothMusicMediaBrowserHelper.MediaControllerListener{
    
    private static final String TAG = BluetoothMusicCase.class.getSimpleName();


    private Context mApplicationContext;
    private BluetoothMusicMediaBrowserHelper mBThMusicMediaBrowserHelper;

    public BluetoothMusicCase(Context context) {
        mApplicationContext = context.getApplicationContext();
        mBThMusicMediaBrowserHelper = BluetoothMusicMediaBrowserHelper
            .getInstance();
    }




    //---------------------------UseCase------------
    @Override
    public void onStart() {
        Logger.d(TAG, "start");
        super.onStart();
        //mBThMusicMediaBrowserHelper.onStart();
        mBThMusicMediaBrowserHelper.registerCallback(this);
    }


    @Override
    public void onStop() {
        Logger.d(TAG, "onStop");
        super.onStop();
        //mBThMusicMediaBrowserHelper.onStop();
        if (mBThMusicMediaBrowserHelper != null) {
            mBThMusicMediaBrowserHelper.unregisterCallback(this);
            mBThMusicMediaBrowserHelper = null;
        }

    }

    @Override
    protected void executeUseCase(BluetoothMusicRequestValues requestValues) {
        switch (requestValues.mPlayAction) {
            case BluetoothMusicRequestValues.PLAYA_CTION:
                mBThMusicMediaBrowserHelper.getTransportControls().play();
                break;
            case BluetoothMusicRequestValues.PAUSE_ACTION:
                mBThMusicMediaBrowserHelper.getTransportControls().pause();
                break;
            case BluetoothMusicRequestValues.NEXT_ACTION:
                mBThMusicMediaBrowserHelper.getTransportControls().skipToNext();
                break;
            case BluetoothMusicRequestValues.PREVIOUS_ACTION:
                mBThMusicMediaBrowserHelper.getTransportControls().skipToPrevious();
                break;
            case BluetoothMusicRequestValues.SEEKTO:
                mBThMusicMediaBrowserHelper.getTransportControls().seekTo(requestValues.mSeekTo);
                break;
            case BluetoothMusicRequestValues.SUBSCRIBE:
                mBThMusicMediaBrowserHelper.subscribe();
                break;
            default:
                break;
        }
            
    }


   

    //---实现BluetoothMusicMediaBrowserHelper.MediaControllerListener------
    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        BluetoothMusicResponseValue responseValue = new BluetoothMusicResponseValue();
        responseValue.mResponseAction = BluetoothMusicResponseValue.RES_ACTION_METADATACHANGE;
        responseValue.mMediaMetadata = metadata;
        getUseCaseCallback().onSuccess(responseValue);
        
    }


    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        BluetoothMusicResponseValue responseValue = new BluetoothMusicResponseValue();
        responseValue.mResponseAction = BluetoothMusicResponseValue.RES_ACTION_PLAYBACKSTATECHANGE;
        responseValue.mPlaybackState = state;
        getUseCaseCallback().onSuccess(responseValue);       
    }

    @Override
    public void onConnected() {
        BluetoothMusicResponseValue responseValue = new BluetoothMusicResponseValue();
        responseValue.mResponseAction = BluetoothMusicResponseValue.RES_ACTION_CONNECTED;
        getUseCaseCallback().onSuccess(responseValue);


    }


    @Override
    public void onDisConnected() {
        
    }







    public static class BluetoothMusicRequestValues implements UseCase.RequestValues {

        //播放动作类型
        public int mPlayAction = PLAYA_CTION;
        public static final int PLAYA_CTION = 1;
        public static final int PAUSE_ACTION = 2;
        public static final int NEXT_ACTION = 3;
        public static final int PREVIOUS_ACTION = 4;
        public static final int SEEKTO = 5;
        public static final int QUERY_ALL_SONG = 6;
        public static final int SUBSCRIBE = 7;
        public static final int PLAY_FROM_MEDIA_ID = 8;


        //跳转到指定位置
        public long mSeekTo = 0;

    }


    public static class BluetoothMusicResponseValue implements UseCase.ResponseValue {
        //响应的动作类型
        public int mResponseAction = 0;
        public static final int RES_ACTION_METADATACHANGE = 1;
        public static final int RES_ACTION_PLAYBACKSTATECHANGE = 2;
        public static final int RES_ACTION_CONNECTED = 3;
        //public static final int RES_ACTION_QUERY_ALL_SONG = 3;
        public MediaMetadataCompat mMediaMetadata;
        public PlaybackStateCompat mPlaybackState;
    }


    
}









