package com.ssi.musicplayer2.btFragment.client;

import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAvrcpController;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


import com.ssi.musicplayer2.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothConnectionHelper implements DefaultLifecycleObserver,
        BluetoothMusicMediaBrowserHelper.MediaControllerListener {

    private static final String TAG = BluetoothConnectionHelper.class.getSimpleName();


    private Context mContext;

    private BluetoothA2dpSink mA2dpSinkService = null;
    private BluetoothDevice mSelectedDevice = null;

    private BluetoothAvrcpController mAvrcpController = null;
    private static final AtomicBoolean mIsAvrcpControllerConnection = new AtomicBoolean(false);
    private static final AtomicBoolean mIsA2DPSINKControllerConnection = new AtomicBoolean(false);
    private static final AtomicBoolean mIsControllerConnection = new AtomicBoolean(false);
    private static final AtomicBoolean mCurrentConnection = new AtomicBoolean(false);

    private static BluetoothConnectionHelper mInstance = null;

    private BluetoothAdapter mBTAdapter = null;
    private BluetoothMusicMediaBrowserHelper mBluetoothMusicMediaBrowserHelper = null;




    private BluetoothConnectionHelper() {

    }



    public static BluetoothConnectionHelper getInstance() {
        if(mInstance == null) {
            mInstance = new BluetoothConnectionHelper();
        }
        return mInstance;
    }


    public static BluetoothConnectionHelper createInstance(Context context, LifecycleOwner owner) {
        if(mInstance == null) {
            mInstance = new BluetoothConnectionHelper();
        }
        mInstance.init(context , owner);
        return mInstance;
    }


    private void init(Context context, LifecycleOwner owner) {
        if(mContext != null) {
            Logger.w(TAG, "BluetoothConnectionHelper yet inited");
            return;
        }
        owner.getLifecycle().removeObserver(this);
        owner.getLifecycle().addObserver(this);
        mContext = context.getApplicationContext();
        mSelectedDevice = getConnectedDevice();
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothMusicMediaBrowserHelper = BluetoothMusicMediaBrowserHelper.getInstance();
        mBluetoothMusicMediaBrowserHelper.registerCallback(this);
    }




    //------------------A2dpSink-----------------------
    private void startA2dpSinkConnection() {
        Logger.d(TAG, "startA2dpSinkConnection");
        if(!mIsA2DPSINKControllerConnection.get() && mBTAdapter != null) {
            mBTAdapter.getProfileProxy(mContext, mA2dpSinkServiceListener, BluetoothProfile.A2DP_SINK);
        }
    }

    private void stopA2dpSinkConnection() {
        Logger.d(TAG, "stopA2dpSinkConnection");
        if(mIsA2DPSINKControllerConnection.get() && mBTAdapter != null) {
            mBTAdapter.closeProfileProxy(BluetoothProfile.A2DP_SINK, mA2dpSinkService);
        }
    }


    private BluetoothProfile.ServiceListener mA2dpSinkServiceListener = 
            new BluetoothProfile.ServiceListener() {

        
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Logger.d(TAG, "onServiceConnected A2DP_SINK mSelectedDevice:" + (mSelectedDevice == null));
            if(profile == BluetoothProfile.A2DP_SINK && mSelectedDevice != null) {
                mA2dpSinkService = (BluetoothA2dpSink)proxy;
                boolean isCon = mA2dpSinkService.connect(mSelectedDevice);
                Logger.d(TAG, "onServiceConnected A2DP_SINK yes isCon:" + isCon);
                mIsA2DPSINKControllerConnection.set(isCon);
                notifitionConnectionStateChanged(isControllerConnection());
            }

        }

        @Override
        public void onServiceDisconnected(int profile) {
            Logger.d(TAG, "onServiceDisconnected A2DP_SINK");
            if(profile == BluetoothProfile.A2DP_SINK && mA2dpSinkService != null) {
                Logger.d(TAG, "onServiceDisconnected A2DP_SINK yes");
                mA2dpSinkService.disconnect(mSelectedDevice);
                mIsA2DPSINKControllerConnection.set(false);
                notifitionConnectionStateChanged(isControllerConnection());
                mA2dpSinkService = null;
            }

        }        
    };



    //----------------AvrcpController---------------------------


    private void startAvrcpControllerConnection() {
        Logger.d(TAG, "startAvrcpControllerConnection");
        if(!mIsAvrcpControllerConnection.get() && mBTAdapter != null) {
            Logger.d(TAG, "startAvrcpControllerConnection yes");
            mBTAdapter.getProfileProxy(mContext, mAvrcpControllerListener, BluetoothProfile.AVRCP_CONTROLLER);
        }
    }


    private void stopAvrcpControllerConnection() {
        Logger.d(TAG, "stopAvrcpControllerConnection");
        if(mIsAvrcpControllerConnection.get() && mBTAdapter != null) {
            Logger.d(TAG, "stopAvrcpControllerConnection yes");
            mBTAdapter.closeProfileProxy(BluetoothProfile.AVRCP_CONTROLLER, mAvrcpController);
        }

    }



    private BluetoothProfile.ServiceListener mAvrcpControllerListener = 
            new BluetoothProfile.ServiceListener(){

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Logger.d(TAG, "onServiceConnected AVRCP_CONTROLLER");
            if(profile == BluetoothProfile.AVRCP_CONTROLLER) {
                Logger.d(TAG, "onServiceConnected AVRCP_CONTROLLER yes");
                mAvrcpController = (BluetoothAvrcpController)proxy;
               // BluetoothAvrcpPlayerSettings settings = new BluetoothAvrcpPlayerSettings(1);
                //mAvrcpController.sendGroupNavigationCmd(mSelectedDevice, 1, 1);
                //mAvrcpController.getPlayerSettings(mSelectedDevice, settings);
                //mAvrcpController.setPlayerApplicationSetting(mAvrcpController.getPlayerSettings(mSelectedDevice));
                mIsAvrcpControllerConnection.set(true);
                notifitionConnectionStateChanged(isControllerConnection());
            }
            
        }
    
        @Override
        public void onServiceDisconnected(int profile) {
            Logger.d(TAG, "onServiceDisconnected AVRCP_CONTROLLER");
            if(profile == BluetoothProfile.AVRCP_CONTROLLER) {
                Logger.d(TAG, "onServiceDisconnected AVRCP_CONTROLLER yes");
                mIsAvrcpControllerConnection.set(false);
                mAvrcpController = null;
                notifitionConnectionStateChanged(isControllerConnection());
            }
            
        }
    };


    public boolean isControllerConnection() {
        Logger.d(TAG, "isControllerConnection mIsA2DPSINKControllerConnection:" + mIsA2DPSINKControllerConnection.get()
                + ", mIsAvrcpControllerConnection:" + mIsAvrcpControllerConnection.get());
        mIsControllerConnection.set(mIsA2DPSINKControllerConnection.get() && mIsAvrcpControllerConnection.get());
        return mIsControllerConnection.get();
    }




    //-----------AvrcpController连接变化监听------------------------------
    private List<ConnectionStateListener> mConnectionStateListeners = new ArrayList<>();

    public interface ConnectionStateListener {
        void onConnectionStateChanged(boolean isConnectioned);
    }

    public void addConnectionListenr(ConnectionStateListener listener) {
        Logger.d(TAG, "addConnectionListenr");
        if(listener != null && !mConnectionStateListeners.contains(listener)) {
            mConnectionStateListeners.add(listener);
            listener.onConnectionStateChanged(isControllerConnection());
        }
    }

    public void rmConnectionListenr(ConnectionStateListener listener) {
        Logger.d(TAG, "rmConnectionListenr");
        if(listener != null) {
            mConnectionStateListeners.remove(listener);
        }
    }

    private void cleanAllListener() {
        mConnectionStateListeners.clear();
    }


    private void notifitionConnectionStateChanged(boolean isConnectioned) {
        if(mCurrentConnection.get() == isConnectioned) {
            Logger.d(TAG, "notifitionConnectionStateChanged return for same");
            return;
        }
        mCurrentConnection.set(isConnectioned);
        final int N = mConnectionStateListeners.size();
        Logger.d(TAG, "notifitionConnectionStateChange dd N:" + N + ",isConnectioned:" + isConnectioned);
        for(int i=0;i<N;i++) {
            mConnectionStateListeners.get(i).onConnectionStateChanged(isConnectioned);
        }
    }



    @MainThread
    public void start() {
        Logger.d(TAG, "start");
        startA2dpSinkConnection();
        startAvrcpControllerConnection();
    }


    @MainThread
    public void stop() {
        Logger.d(TAG, "stop");
        mContext = null;
        stopA2dpSinkConnection();
        stopAvrcpControllerConnection();
        cleanAllListener();

    }


    //---------DefaultLifecycleObserver------------


    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        //start();
        mBluetoothMusicMediaBrowserHelper.registerCallback(this);
    }


    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        mBluetoothMusicMediaBrowserHelper.unregisterCallback(this);
        stop();
    }

    //BluetoothMusicMediaBrowserHelper.MediaControllerListener


    @Override
    public void onConnected() {
        Logger.d(TAG, "onConnected");
        start();
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Logger.d(TAG, "onMetadataChanged");
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        Logger.d(TAG, "onPlaybackStateChanged");
    }

    @Override
    public void onDisConnected() {
        Logger.d(TAG, "onDisConnected");
        stop();
    }

    public void handleBTState(Intent intent) {
        String action;
        if(intent == null || TextUtils.isEmpty(action = intent.getAction()) ) {
            Logger.w(TAG, "handleBTState return for null");
            return;
        }
        if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            mSelectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //startA2dpSinkConnection();
            //startAvrcpControllerConnection();
        }else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            mSelectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //stopA2dpSinkConnection();
            //stopAvrcpControllerConnection();
        }

    }












    public static int isConnectedToOtherDevice() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
            return BluetoothAdapter.STATE_DISCONNECTED;
        }
        return adapter.getConnectionState();
    }


    public static BluetoothDevice getConnectedDevice() {
        Logger.d(TAG, "getConnectedDevice");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
            return null;
        }
        for (BluetoothDevice device:adapter.getBondedDevices()) {
            if(device.isConnected()) {
                Logger.d(TAG, "getConnectedDevice yes");
                return device;
            }
        }
        return null;
    }




    
}



