package com.ssi.musicplayer2.btFragment.intf;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;


public interface BluetoothMusicContract {

    public interface PlayControllerPresenter extends BasePresenter {

        public void skipToPrevious();

        public void pause();
    
        public void play();
    
        public void skipToNext();

        public void seekTo(long progress);

        public void onPlayFromMediaId(String mediaId, Bundle extras);

        void subscribe();
    }

    public interface BluetoothMusicView extends BaseView<PlayControllerPresenter>{
        void onMetadataChanged(final MediaMetadataCompat metadata);
        void onPlaybackStateChanged(final PlaybackStateCompat state);
        default void onConnected(){

        }
    }



}




