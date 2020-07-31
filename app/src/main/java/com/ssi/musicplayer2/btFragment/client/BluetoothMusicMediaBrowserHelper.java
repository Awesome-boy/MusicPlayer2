package com.ssi.musicplayer2.btFragment.client;


import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import com.ssi.musicplayer2.utils.Logger;

import java.util.ArrayList;
import java.util.List;

//import android.media.MediaMetadata;
//import android.media.browse.MediaBrowser;
//import android.media.session.MediaController;
//import android.media.session.PlaybackState;
//import android.media.session.MediaSession.QueueItem;

public class BluetoothMusicMediaBrowserHelper implements DefaultLifecycleObserver {

    private static final  String TAG = BluetoothMusicMediaBrowserHelper.class.getSimpleName();
    private static final String  ROOT = "__ROOT__";


    private Context mContext = null;
    //必须包含它android.service.media.MediaBrowserService的子类
    private ComponentName mBtServiceComponentName = null;
    private MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private MediaControllerCallback mMediaControllerCallback;
    private MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;
    private MediaControllerCompat mMediaController = null;
    private MediaBrowserCompat mMediaBrowser = null;
    private List<MediaControllerListener> mCallbackList = new ArrayList<>();
    private static BluetoothMusicMediaBrowserHelper mMediaBrowserHelper = null;

    private BluetoothMusicMediaBrowserHelper() {

    }

    public static BluetoothMusicMediaBrowserHelper getInstance() {
        Logger.w(TAG, "getInstance");
        if(mMediaBrowserHelper == null) {
            Logger.w(TAG, "getInstance1");
            mMediaBrowserHelper = new BluetoothMusicMediaBrowserHelper();
        }
        return mMediaBrowserHelper;

    }


    public static BluetoothMusicMediaBrowserHelper createInstance(Context context,
                                                            ComponentName serviceCom, LifecycleOwner owner){
        if(mMediaBrowserHelper == null) {
            mMediaBrowserHelper = new BluetoothMusicMediaBrowserHelper();
        }
        mMediaBrowserHelper.init(context, serviceCom, owner);
        return mMediaBrowserHelper;
    }

    private void init(Context context,
                      ComponentName serviceCom, LifecycleOwner owner) {
        if(mContext != null) {
            Logger.w(TAG, "yet init");
            return;
        }
        owner.getLifecycle().removeObserver(this);
        owner.getLifecycle().addObserver(this);
        mContext = context.getApplicationContext();
        mBtServiceComponentName = serviceCom;
        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
        Logger.w(TAG, "init mBtServiceComponentName:" + mBtServiceComponentName.toString());
        Log.d("zt", "mMediaController---init--"+String.valueOf(mMediaController==null));
    }


    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        onStart();

    }


    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        mContext = null;
        onStop();

    }

    public void onStart() {
        Logger.w(TAG, "onStart");
        if (mMediaBrowser == null) {
            mMediaBrowser =
                    new MediaBrowserCompat(
                            mContext,
                            mBtServiceComponentName,
                            mMediaBrowserConnectionCallback,
                            null);
            mMediaBrowser.connect();
        }



    }

    public void onStop() {
        Logger.w(TAG, "onStop");
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
        resetState();
    }

    public final MediaControllerCompat getMediaController() {
        if (mMediaController == null) {
            throw new IllegalStateException("MediaController is null!");
        }
        
        return mMediaController;
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        if (mMediaController == null) {
            throw new IllegalStateException("MediaController is null!");
        }
        return mMediaController.getTransportControls();
    }

    public void subscribe() {
        Logger.d(TAG, "subscribe");
        if(mMediaBrowser == null) {
            Logger.w(TAG, "subscribe return for null");
            return;
        }
        mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);

    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback{
       
        @Override
        public void onConnected() {
            Logger.w("zt", "MediaBrowserConnectionCallback onConnected");
            super.onConnected();

            try {
                mMediaController = new MediaControllerCompat(mContext,
                mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);
                mMediaControllerCallback.onConnected();
                mMediaControllerCallback.onMetadataChanged(mMediaController.getMetadata());
                mMediaControllerCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
                //mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
                //mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
                Log.d("zt", "mMediaController---onConnected--"+String.valueOf(mMediaController==null));
            } catch (Exception e) {
                Log.d("zt", "mMediaController---Exception--"+String.valueOf(mMediaController==null));
                throw new RuntimeException(e);

            }
 

        }




        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
            Logger.w(TAG, "onConnectionSuspended !!!");
        }

        @Override
        public void onConnectionFailed() {
            Logger.w(TAG, "onConnectionFailed !!!");
            super.onConnectionFailed();
        }
    }


    private List<MediaBrowserCompat.MediaItem> mMusicList = null;
    private String mParentId = null;
    private class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback{
        @Override
        public void onChildrenLoaded(String parentId, List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            Logger.d(TAG, "MediaBrowserSubscriptionCallback onChildrenLoaded");
            //订阅成功，进行播放准备
            mMediaController.getTransportControls().prepare();
            mMediaControllerCallback.onMetadataChanged(mMediaController.getMetadata());
            mMediaControllerCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
            //performOnAllCallbacks((listener) -> {
                //public void perform(final MediaControllerListener listener) {
                  //  listener.onChildrenLoaded(parentId, children);
               // }
           // });
        }
    }


    public void registerCallback(MediaControllerListener callback) {
        Logger.w(TAG, "registerCallback");
        if(callback != null && !mCallbackList.contains(callback)) {
            Logger.w(TAG, "registerCallback yes");
            mCallbackList.add(callback);
            if(mMediaController != null) {
                final MediaMetadataCompat metadata = mMediaController.getMetadata();
                if(metadata != null) {
                    callback.onMetadataChanged(metadata);
                }
                final PlaybackStateCompat playbackState = mMediaController.getPlaybackState();
                if(playbackState != null) {
                    callback.onPlaybackStateChanged(playbackState);
                }

            }


        }

    }


    public void unregisterCallback(MediaControllerListener callback) {
        if(callback != null) {
            mCallbackList.remove(callback);
        }

    }

    private void resetState() {
        performOnAllCallbacks(new CallbackCommand() {
            @Override
            public void perform(MediaControllerListener listener) {
                listener.onPlaybackStateChanged(null);
            }
        });
        //mCallbackList.clear();
    }

    private void performOnAllCallbacks(CallbackCommand command) {
        for(MediaControllerListener callback: mCallbackList) {
            if(callback != null) {
                command.perform(callback);
            }
        }
    }


    private interface CallbackCommand {
        void perform(MediaControllerListener listener);
    }

    public interface MediaControllerListener {
        void onMetadataChanged(final MediaMetadataCompat metadata);
        void onPlaybackStateChanged(final PlaybackStateCompat state);
        //void onChildrenLoaded(String parentId, List<MediaBrowserCompat.MediaItem> children);
        void onConnected();
        void onDisConnected();
    }

    



    private class MediaControllerCallback extends MediaControllerCompat.Callback{

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            Logger.d(TAG, "onQueueChanged");
            super.onQueueChanged(queue);
        }

        @Override
        public void onMetadataChanged(final MediaMetadataCompat metadata) {
            Logger.d(TAG, "onMetadataChanged");
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(final MediaControllerListener listener) {
                    listener.onMetadataChanged(metadata);
                }
            });
            
        }

        @Override
        public void onPlaybackStateChanged(final PlaybackStateCompat state) {
            Logger.d(TAG, "onPlaybackStateChanged");
            super.onPlaybackStateChanged(state);
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(MediaControllerListener listener) {
                    listener.onPlaybackStateChanged(state);
                }
            });
        }

        @Override
        public void onSessionDestroyed() {
            Logger.d(TAG, "onSessionDestroyed");
            super.onSessionDestroyed();
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(MediaControllerListener listener) {
                    listener.onDisConnected();
                }
            });
            resetState();
            onPlaybackStateChanged(null);
        }

        public void onConnected() {
            Logger.d(TAG, "onConnected");
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(MediaControllerListener listener) {
                    listener.onConnected();
                }
            });
        }

    }








}
