package com.ssi.musicplayer2.btFragment.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import com.ssi.musicplayer2.MainActivity;
import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.btFragment.client.BluetoothMusicMediaBrowserHelper;
import com.ssi.musicplayer2.btFragment.service.BluetoothMusicService;
import com.ssi.musicplayer2.utils.Logger;

import java.lang.ref.WeakReference;

public class BluetoothMediaNotificationManager implements DefaultLifecycleObserver, BluetoothMusicMediaBrowserHelper.MediaControllerListener {


    private static final String TAG = BluetoothMediaNotificationManager.class.getSimpleName();

    public static final int BT_NOTIFICATION_ID_MUSIC_SERVICE = 410;


    private static final String BT_NOTIFICATION_CHANNEL_ID_MUSIC_SERVICE = "com.ssi.musicplayer.btplayer";
    private static final String BT_CHANNEL_ID = "com.ssi.musicplayer.btplayer.channel";
    private static final int BT_NOTIFICATION_REQUEST_CODE_MUSIC_SERVICE = 503;

    private static final String ACTION_BT_PLAY = "com.ssi.musicplayer_action_play_music_bt";
    private static final String ACTION_BT_NEXT = "com.ssi.musicplayer_action_next_music_bt";
    private static final String ACTION_BT_PRE = "com.ssi.musicplayer_action_pre_music_bt";



    private Context mAppContext;
    private BluetoothMusicMediaBrowserHelper mBluetoothMusicMediaBrowserHelper;
    private static BluetoothMediaNotificationManager mInstance = null;
    private NotificationManagerCompat mNotificationManager = null;
    private WeakReference<BluetoothMusicService> mHostService = null;
    private NotificationChannel mNotificationChannel = null;



    private static final @LayoutRes
    int MUSIC_NOTIFICATION_LAYOUT_EXPANDED = R.layout.expanded_notification_local_music;
    private static final @LayoutRes
    int MUSIC_NOTIFICATION_LAYOUT = R.layout.notification_local_music;

    private RemoteViews mNotificationExpandedRemoteViews = null;
    private RemoteViews mNotificationRemoteViews = null;

    private MediaMetadataCompat mCurrentMediaMetadata = null;
    private PlaybackStateCompat mCurrentPlaybackState = null;
    private LocalReceiver mLocalReceiver;
    private LocalHandler mLocalHandler;




    public synchronized static BluetoothMediaNotificationManager getInstance() {
        if(mInstance == null) {
            mInstance = new BluetoothMediaNotificationManager();
        }
        return mInstance;
    }


    public static BluetoothMediaNotificationManager createInstance(BluetoothMusicService hostService, LifecycleOwner owner) {
        if(mInstance == null) {
            mInstance = new BluetoothMediaNotificationManager();
        }
        mInstance.init(hostService, owner);
        return mInstance;
    }

    private void init(BluetoothMusicService hostService, LifecycleOwner owner) {
        if(mAppContext != null) {
            Logger.w(TAG, "yet init");
            return;
        }
        mHostService = new WeakReference<>(hostService);
        owner.getLifecycle().removeObserver(this);
        owner.getLifecycle().addObserver(this);
        mAppContext = hostService.getApplicationContext();
        mBluetoothMusicMediaBrowserHelper = BluetoothMusicMediaBrowserHelper.getInstance();
        mBluetoothMusicMediaBrowserHelper.registerCallback(this);
        mNotificationManager = NotificationManagerCompat.from(mAppContext);
    }


    private BluetoothMediaNotificationManager() {

    }







    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        final Service service = getHostService();
        if(service != null) {
            service.startForeground(BT_NOTIFICATION_ID_MUSIC_SERVICE, getNotification());
        }
        mBluetoothMusicMediaBrowserHelper.registerCallback(this);
        mLocalHandler = new LocalHandler(this, Looper.getMainLooper());
        mLocalReceiver = new LocalReceiver(this);
        mLocalReceiver.registerReceiver(mAppContext);
    }


    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if(mHostService != null) {
            final Service service = getHostService();
            if(service != null) {
                service.stopForeground(true);
            }
            mHostService.clear();
            mHostService = null;
        }
        mLocalReceiver.unregisterReceiver(mAppContext);
        if(mLocalHandler != null) {
            mLocalHandler.destroy();
            mLocalHandler = null;
        }

        if(mBluetoothMusicMediaBrowserHelper != null) {
            mBluetoothMusicMediaBrowserHelper.unregisterCallback(this);
            mBluetoothMusicMediaBrowserHelper = null;

        }
        mAppContext = null;
    }


    //---------BluetoothMusicMediaBrowserHelper.MediaControllerListener -----------


    @Override
    public void onConnected() {

    }




    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if(metadata == null) {
            Logger.w(TAG, "onMetadataChanged return for null");
            return;
        }
        mCurrentMediaMetadata = metadata;


    }


    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        final Service service = getHostService();
        if(service == null || state == null) {
            Logger.w(TAG, "onPlaybackStateChanged return for null");
            return;
        }
        mCurrentPlaybackState = state;
        int playState = mCurrentPlaybackState.getState();
        if(playState == PlaybackStateCompat.STATE_PLAYING) {
            service.startForeground(BT_NOTIFICATION_ID_MUSIC_SERVICE, getNotification());
        }else {
            service.stopForeground(false);
            mNotificationManager.notify(BT_NOTIFICATION_ID_MUSIC_SERVICE, getNotification());
        }
    }

    @Override
    public void onDisConnected() {

    }


    private Service getHostService() {
        return mHostService != null ? mHostService.get() : null;
    }

    private Notification getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mAppContext, BT_NOTIFICATION_CHANNEL_ID_MUSIC_SERVICE)
                .setContentTitle(mAppContext.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_stat_image_audiotrack)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(false)
                .setContentIntent(createContentIntent())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomContentView(getNotificationLayout(mCurrentPlaybackState))
                .setCustomBigContentView(getNotificationLayoutExpanded(mCurrentPlaybackState, mCurrentMediaMetadata))
                .setFullScreenIntent(createContentIntent(), true);
        Notification notify = builder.build();
        notify.flags = Notification.FLAG_NO_CLEAR;
        createChannel();
        mNotificationChannel = new NotificationChannel(BT_NOTIFICATION_CHANNEL_ID_MUSIC_SERVICE, mAppContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
        return notify;

    }

    private void createChannel() {
        if(mNotificationChannel == null || mNotificationManager.getNotificationChannel(BT_CHANNEL_ID) == null) {
            mNotificationChannel = new NotificationChannel(BT_NOTIFICATION_CHANNEL_ID_MUSIC_SERVICE, mAppContext.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
    }


    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mAppContext.getApplicationContext(), MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                mAppContext, BT_NOTIFICATION_REQUEST_CODE_MUSIC_SERVICE , openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private PendingIntent createPlayIntent() {
        Intent openUI = new Intent(ACTION_BT_PLAY);
        return PendingIntent.getBroadcast( mAppContext, BT_NOTIFICATION_REQUEST_CODE_MUSIC_SERVICE , openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent createPreIntent() {
        Intent openUI = new Intent(ACTION_BT_PRE);
        return PendingIntent.getBroadcast( mAppContext, BT_NOTIFICATION_REQUEST_CODE_MUSIC_SERVICE , openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent createNextIntent() {
        Intent openUI = new Intent(ACTION_BT_NEXT);
        return PendingIntent.getBroadcast( mAppContext, BT_NOTIFICATION_REQUEST_CODE_MUSIC_SERVICE , openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    @NonNull
    private RemoteViews getNotificationLayout(PlaybackStateCompat state) {
        if(mNotificationRemoteViews == null) {
            mNotificationRemoteViews = new RemoteViews(mAppContext.getPackageName(),MUSIC_NOTIFICATION_LAYOUT);
            mNotificationRemoteViews.setOnClickPendingIntent(R.id.button_previous, createPreIntent());
            mNotificationRemoteViews.setOnClickPendingIntent(R.id.button_play, createPlayIntent());
            mNotificationRemoteViews.setOnClickPendingIntent(R.id.button_next,  createNextIntent());
        }

        //PlaybackState state = mediaSession.getController().getPlaybackState();
        if(state != null) {
            int s =  state.getState();
            if(s ==  PlaybackState.STATE_PLAYING) {
                mNotificationRemoteViews.setImageViewResource(R.id.button_play, R.drawable.img_pause);
            }else{
                mNotificationRemoteViews.setImageViewResource(R.id.button_play, R.drawable.img_play);
            }
        }

        return mNotificationRemoteViews;
    }




    @NonNull
    private RemoteViews getNotificationLayoutExpanded(PlaybackStateCompat state, MediaMetadataCompat mediaMetadata) {
        if(mNotificationExpandedRemoteViews == null) {
            mNotificationExpandedRemoteViews = new RemoteViews(mAppContext.getPackageName(), MUSIC_NOTIFICATION_LAYOUT_EXPANDED);
            mNotificationExpandedRemoteViews.setOnClickPendingIntent(R.id.button_previous, createPreIntent());
            mNotificationExpandedRemoteViews.setOnClickPendingIntent(R.id.button_play, createPlayIntent());
            mNotificationExpandedRemoteViews.setOnClickPendingIntent(R.id.button_next,  createNextIntent());
        }
        if(state != null) {
            int s =  state.getState();
            if(s ==  PlaybackState.STATE_PLAYING) {
                mNotificationExpandedRemoteViews.setImageViewResource(R.id.button_play, R.drawable.selector_pause);
            }else{
                mNotificationExpandedRemoteViews.setImageViewResource(R.id.button_play, R.drawable.play_selector);
            }
        }
        mNotificationExpandedRemoteViews.setImageViewResource(R.id.music_ico, R.drawable.ic_stat_image_audiotrack);
        if(mediaMetadata != null) {
            String title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            title = !TextUtils.isEmpty(title) ?  title : mAppContext.getString(R.string.unknown_song_title);
            mNotificationExpandedRemoteViews.setTextViewText(R.id.music_title, title);
            mNotificationExpandedRemoteViews.setImageViewBitmap(R.id.music_ico, mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON));
        }

        return mNotificationExpandedRemoteViews;
    }


    private static class LocalReceiver extends BroadcastReceiver {

        private WeakReference<BluetoothMediaNotificationManager> mHost;
        private IntentFilter mIntentFilter;

        private LocalReceiver(BluetoothMediaNotificationManager host) {
            mHost = new WeakReference<>(host);
            mIntentFilter = new IntentFilter(ACTION_BT_NEXT);
            mIntentFilter.addAction(ACTION_BT_PLAY);
            mIntentFilter.addAction(ACTION_BT_PRE);
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothMediaNotificationManager host;
            if(context == null || intent == null || mHost == null ||((host = mHost.get()) == null)) {
                Logger.w(TAG, "LocalReceiver onReceive return for null");
                return;
            }
            if(!mIntentFilter.hasAction(intent.getAction())) {
                Logger.w(TAG, "LocalReceiver onReceive return for no action");
                return;
            }

            host.postHandPlayAction(intent);
        }

        private void registerReceiver(Context context) {
            context.registerReceiver(this, mIntentFilter);
        }

        private void unregisterReceiver(Context context) {
            context.unregisterReceiver(this);
        }
    }

    private static final class LocalHandler extends Handler {

        private static final int WHAT_HANDLE_PLAY_ACTION = 101;

        private WeakReference<BluetoothMediaNotificationManager> mHost;

        private LocalHandler(BluetoothMediaNotificationManager host, Looper looper) {
            super(looper);
            mHost = new WeakReference<>(host);

        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            BluetoothMediaNotificationManager host;
            if(mHost == null || ((host = mHost.get()) == null)) {
                Logger.w(TAG, "handleMessage return for null");
                return;
            }
            int wht = msg.what;
            if(wht == WHAT_HANDLE_PLAY_ACTION) {
                host.handPlayAction((Intent) msg.obj);
            }
            super.handleMessage(msg);
        }


        private void destroy() {
            removeCallbacksAndMessages(null);
            if(mHost != null) {
                mHost.clear();
                mHost = null;
            }

        }
    }


    private void postHandPlayAction(Intent intent) {
        if(mLocalHandler != null) {
            Message msg = Message.obtain();
            msg.what = LocalHandler.WHAT_HANDLE_PLAY_ACTION;
            msg.obj = intent;
            mLocalHandler.sendMessage(msg);
        }
    }

    private void handPlayAction(Intent intent) {
        String action = "";
        if(mBluetoothMusicMediaBrowserHelper == null || intent == null
                    || (TextUtils.isEmpty(action = intent.getAction()))) {
            Logger.w(TAG, "handPlayAction return for null");
            return;
        }
        MediaControllerCompat controller = mBluetoothMusicMediaBrowserHelper.getMediaController();
        if(controller == null) {
            Logger.w(TAG, "handPlayAction return for controller is null");
           return;
        }
        if(ACTION_BT_NEXT.equals(action)) {
            controller.getTransportControls().skipToNext();
        }else if(ACTION_BT_PRE.equals(action)) {
            controller.getTransportControls().skipToPrevious();
        }else if(ACTION_BT_PLAY.equals(action)) {
            final PlaybackStateCompat stateCompat =  controller.getPlaybackState();
            if(stateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                controller.getTransportControls().pause();
            }else {
                controller.getTransportControls().pause();
            }
        }








    }
}
