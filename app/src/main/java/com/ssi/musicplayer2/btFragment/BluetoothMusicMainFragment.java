package com.ssi.musicplayer2.btFragment;


import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.dfssi.android.framework.carstate.CarStateCallback;
import com.dfssi.android.framework.carstate.CarStateManager;
import com.dfssi.android.framework.input.SSIKeyEvent;
import com.dfssi.android.framework.utils.CarSpeedUtils;
import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.btFragment.client.BluetoothConnectionHelper;
import com.ssi.musicplayer2.btFragment.client.BluetoothMusicCase;
import com.ssi.musicplayer2.btFragment.intf.BluetoothMusicContract;
import com.ssi.musicplayer2.btFragment.intf.UseCaseHandler;
import com.ssi.musicplayer2.btFragment.presenter.BluetoothMusicPresenter;
import com.ssi.musicplayer2.btFragment.service.BluetoothMusicService;
import com.ssi.musicplayer2.utils.Logger;
import com.ssi.musicplayer2.utils.Utils;


public class BluetoothMusicMainFragment extends Fragment
        implements BluetoothMusicContract.BluetoothMusicView, BluetoothConnectionHelper.ConnectionStateListener ,
                    View.OnClickListener, AppCompatSeekBar.OnSeekBarChangeListener,
                    ValueAnimator.AnimatorUpdateListener {

    private static final ComponentName BT_MUSIC_SERVICE = new ComponentName("com.ssi.musicplayer", "com.ssi.musicplayer.bluetoothmusic.services.BluetoothMusicService");

    private static final String TAG = BluetoothMusicMainFragment.class.getSimpleName();

    private BluetoothMusicContract.PlayControllerPresenter mBTPresenter = null;
    private BluetoothConnectionHelper mBluetoothConnectionHelper;
    private boolean mIsPlaying = false;
    private PlaybackStateCompat mCurrentPlaybackState;
    private CarStateCallback carStateCallback;


    public BluetoothMusicMainFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        carStateCallback = CarSpeedUtils.registerCarState(context, new CarSpeedUtils.SpeedCallBack() {
            @Override
            public void showDialog() {

            }

            @Override
            public void hideDialog() {

            }

            @Override
            public void onKeyEvent(KeyEvent keyevnt) {
//                if (keyevnt.isLongPress()){
//                    handle(keyevnt);
//                }else {
//                    handle(keyevnt);
//                }
                handle(keyevnt);

            }
        });
    }

    private void handle(KeyEvent keyevnt) {
        switch (keyevnt.getKeyCode()){
            case SSIKeyEvent.KEY_CODE_LAST_SONG:
                if (mBTPresenter!=null){
                    mBTPresenter.skipToPrevious();
                }
                break;
            case SSIKeyEvent.KEY_CODE_NEXT_SONG:
                if (mBTPresenter!=null){
                    mBTPresenter.skipToNext();
                }
                break;


        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        FragmentActivity activity = getActivity();
        if(activity == null || activity.isDestroyed() || activity.isFinishing()) {
            Logger.w(TAG, "return for host is null");
            return;
        }
        if(!BluetoothMusicService.mServiceIsStart.get()) {
            Logger.w(TAG, "bt music service not yet started so start now");
            Intent intent = new Intent();
            intent.setComponent(BT_MUSIC_SERVICE);
            activity.getApplicationContext().startForegroundService(intent);
        }
        initPresenter();
        mBluetoothConnectionHelper = BluetoothConnectionHelper.getInstance();
        mBluetoothConnectionHelper.addConnectionListenr(this);
       // mBluetoothConnectionHelper.start();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.bt_music_main_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBluetoothConnectionHelper != null) {
            mBluetoothConnectionHelper.rmConnectionListenr(this);
            //mBluetoothConnectionHelper.stop();
            mBluetoothConnectionHelper = null;
        }
        CarStateManager.getInstance().unregisterCarStateCallback(carStateCallback);
        unInitPresenter();
    }




    //-----------------实现BluetoothMusicContract.BluetoothMusicView接口，接受歌曲变化信息------------------------
    @Override
    public void setPresenter(BluetoothMusicContract.PlayControllerPresenter presenter) {
        mBTPresenter = presenter;
    }


    private MediaMetadataCompat mCurrentMediaMetadata;

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Logger.d(TAG, "onMetadataChanged metadata:" + (metadata== null));
       // mCurrentMediaMetadata = metadata;
        fillView(metadata, mCurrentPlaybackState);
        
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        Logger.d(TAG, "onMetadataChanged metadata:" + (state== null));
        //mCurrentPlaybackState = state;
        if(mCurrentPlaybackState != null) {
            mIsPlaying = mCurrentPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        }
        fillView(mCurrentMediaMetadata, state);
    }

    @Override
    public void onConnected() {
        Logger.d(TAG, "onConnected");
        if(mBluetoothConnectionHelper != null) {
            Logger.d(TAG, "onConnected 111");
            //mBluetoothConnectionHelper.start();
        }
    }

    //---------实现BluetoothConnectionHelper.AvrcpControllerConnectionStateChanged，接受Avrcp连接状态变化--------------------------------
    //@Override
    public void onConnectionStateChanged(boolean isConnectioned) {
        Logger.d(TAG, "onConnectionStateChanged isConnectioned:" + isConnectioned);
        if(mBTPresenter == null) {
            Logger.w(TAG, "onConnectionStateChanged return for null");
            return;
        }
        if(isConnectioned) {
            Logger.d(TAG, "onConnectionStateChanged yes");
            mBTPresenter.subscribe();
            //initPresenter();
        }else {
            Logger.d(TAG, "onConnectionStateChanged no");
            //unInitPresenter();
        }
        
    }

    @Override
    public void onClick(View view) {
        final  int id = view.getId();
        Logger.d(TAG, "onClick");
        if(mBTPresenter == null) {
            Logger.w(TAG, "onClick return for null");
            return;
        }
        if(id == R.id.button_previous) {
            Logger.d(TAG, "onClick yes button_previous");
            mBTPresenter.skipToPrevious();
        }else if(id == R.id.button_next) {
            Logger.d(TAG, "onClick yes button_next");
            mBTPresenter.skipToNext();
        }else if(id == R.id.button_play) {
            Logger.d(TAG, "onClick yes button_play:" + mIsPlaying);
            if(mIsPlaying) {
                mBTPresenter.pause();
            }else {
                mBTPresenter.play();
            }

        }

    }


    //AppCompatSeekBar.OnSeekBarChangeListener

    private boolean mIsTrackingTouch = false;


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsTrackingTouch = false;
//        if (mBTPresenter!=null){
//            mBTPresenter.seekTo(seekBar.getProgress());
//        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(R.id.seekbar_audio == seekBar.getId() && mSongProgressTimeView != null) {
            mSongProgressTimeView.setText(Utils.formatTime(progress));

        }
    }


    //ValueAnimator.AnimatorUpdateListener
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        // If the user is changing the slider, cancel the animation.
        if(mIsTrackingTouch) {
            animation.cancel();
            return;
        }
        //final int animatedIntValue = (int) animation.getAnimatedValue();
        if(mAudioSeekBar != null) {
            mAudioSeekBar.setProgress((int) animation.getAnimatedValue());
        }
    }

    private void initPresenter() {
        if(mBTPresenter != null  || getActivity() == null) {
            Logger.w(TAG, "initPresenter return for null");
            return;
        }
        BluetoothMusicCase bluetoothCase = new BluetoothMusicCase(getActivity().getApplicationContext());
        new BluetoothMusicPresenter(UseCaseHandler.getInstance(), this, bluetoothCase);
        mBTPresenter.start();
    }

    private void unInitPresenter() {
        if(mBTPresenter != null) {
            mBTPresenter.stop();
            mBTPresenter = null;
        }
    }

    private AppCompatTextView mSongTileView;
    private AppCompatTextView mSongAlbumView;
    private AppCompatTextView mSongArtistView;
    private AppCompatButton mPlayView;
    private AppCompatImageView mSongImgView;
    private AppCompatSeekBar mAudioSeekBar;
    private AppCompatTextView mSongDurationTime;
    private AppCompatTextView mSongProgressTimeView;

    private void initView(View view) {
        mSongTileView = view.findViewById(R.id.song_title);
        mSongAlbumView = view.findViewById(R.id.song_album);
        mSongArtistView = view.findViewById(R.id.song_artist);
        mPlayView = view.findViewById(R.id.button_play);
        mPlayView.setOnClickListener(this);
        view.findViewById(R.id.button_previous).setOnClickListener(this);
        view.findViewById(R.id.button_next).setOnClickListener(this);
        mSongImgView = view.findViewById(R.id.song_img);
        mAudioSeekBar = view.findViewById(R.id.seekbar_audio);
        mAudioSeekBar.setOnSeekBarChangeListener(this);
        mMaxTime = mAudioSeekBar.getMax();
        mSongDurationTime = view.findViewById(R.id.song_duration_time);
        mSongProgressTimeView = view.findViewById(R.id.song_progress_time);
        fillView(mCurrentMediaMetadata, mCurrentPlaybackState);
    }



    private ValueAnimator mProgressAnimator;
    private long mMaxTime = -1l;



    private void fillView(MediaMetadataCompat metadata, PlaybackStateCompat stateCompat) {
        Logger.d(TAG, "fillView");
        if(mSongTileView == null) {
            Logger.w(TAG, "fillView return for null");
            return;
        }
        if(metadata != null && !metadata.equals(mCurrentMediaMetadata)) {
            Logger.w(TAG, "fillView metadata yes");
            mCurrentMediaMetadata = metadata;
            mSongTileView.setText(mCurrentMediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            mSongAlbumView.setText(mCurrentMediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
            mSongArtistView.setText(mCurrentMediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            Bitmap albumArt = mCurrentMediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
            Logger.w(TAG, "fillView albumArt != null ? " + (albumArt != null));
            if(albumArt != null) {
                mSongImgView.setImageBitmap(albumArt);
            }
            mAudioSeekBar.setMin(0);
            mMaxTime = mCurrentMediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            mAudioSeekBar.setMax((int) mMaxTime);
            mSongDurationTime.setText(Utils.formatTime(mMaxTime));
        }


        if(stateCompat != null && !stateCompat.equals(mCurrentPlaybackState)) {
            Logger.w(TAG, "fillView PlaybackState yes");
            mCurrentPlaybackState = stateCompat;
            mIsPlaying = mCurrentPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;
            setPlayButtonState(mPlayView,mIsPlaying);
            final long position = mCurrentPlaybackState.getPosition();
            final int playState = mCurrentPlaybackState.getState();
            if(mProgressAnimator != null) {
                mProgressAnimator.removeUpdateListener(this);
                mProgressAnimator.cancel();
                mProgressAnimator = null;
            }

            if(position <= mMaxTime) {
                mAudioSeekBar.setProgress((int) position);
                int timeToEnd = -1;
                if(playState == PlaybackStateCompat.STATE_PLAYING) {
                    timeToEnd = (int) ((mMaxTime - position) / mCurrentPlaybackState.getPlaybackSpeed());
                }
                if(timeToEnd <= 0) {
                    Logger.e(TAG, "onPlaybackStateChanged return for timeToEnd is negative");
                    return;
                }
                mProgressAnimator = ValueAnimator.ofInt((int) position, (int)mMaxTime)
                        .setDuration(timeToEnd);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.addUpdateListener(this);
                mProgressAnimator.start();
            }






        }






    }





    private void setPlayButtonState(AppCompatButton bu, boolean isPlaying) {
        if(isPlaying) {
            bu.setBackgroundResource(R.drawable.img_pause);
        }else {
            bu.setBackgroundResource(R.drawable.img_play);
        }
    }
    




}


