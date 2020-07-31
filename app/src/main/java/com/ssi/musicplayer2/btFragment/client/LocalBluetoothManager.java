package com.ssi.musicplayer2.btFragment.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class LocalBluetoothManager {


    private static LocalBluetoothManager mInstance = null;

    private final List<BluetoothStateListener> mListeners;


    /**
     * 对LocalHandler操作，必加锁
     * */
    private LocalHandler mUIHandler = null;
    private static final Object mHandlerLock = new Object();

    private static final ComponentName BT_MUSIC_SERVICE = new ComponentName("com.ssi.musicplayer", "com.ssi.musicplayer.bluetoothmusic.services.BluetoothMusicService");







    private LocalBluetoothManager() {
        mListeners = new CopyOnWriteArrayList<>();
    }



    public synchronized static LocalBluetoothManager getInstance() {
        if(mInstance == null) {
            mInstance = new LocalBluetoothManager();
        }
        return mInstance;
    }



    public void handleBTState(Context context,Intent intent) {
        if(context == null || intent == null) {
            return;
        }
        intent.setComponent(BT_MUSIC_SERVICE);
        context.startService(intent);
        //context.startActivity(intent);
        synchronized (mHandlerLock) {
            if(mUIHandler == null) {
                return;
            }
           Message.obtain(mUIHandler, LocalHandler.WHAT_HANDLE_BT_STATE, intent).sendToTarget();
        }


    }





    @MainThread
    public void addBluetoothStateListener(BluetoothStateListener listener) {
        if(listener == null || mListeners.contains(listener)) {
            return;
        }
        mListeners.add(listener);
        synchronized (mHandlerLock) {
            if(mUIHandler == null) {
                mUIHandler = new LocalHandler(Looper.getMainLooper(), this);
            }
        }


    }

    @MainThread
    public void rmBluetoothStateListener(BluetoothStateListener listener) {
        if(listener == null || !mListeners.contains(listener)) {
            return;
        }
        mListeners.remove(listener);
        if(mListeners.size() <=0) {
            synchronized (mHandlerLock) {
                if(mUIHandler != null) {
                    mUIHandler.destroy();
                    mUIHandler = null;
                }
            }

        }
    }


    @MainThread
    private void notifyAllListener(Intent intent) {
        if(intent == null || mListeners.isEmpty()) {
            return;
        }
        for (int i=0;i<mListeners.size();i++) {
            mListeners.get(i).onBluetoothStateChanged(intent);
        }

    }




    private static class LocalHandler extends Handler {
        private static final int WHAT_HANDLE_BT_STATE = 1;
        private WeakReference<LocalBluetoothManager> mHost;
        private LocalHandler(Looper looper, LocalBluetoothManager host) {
            super(looper);
            mHost = new WeakReference<>(host);
        }



        @Override
        public void handleMessage(@NonNull Message msg) {
            LocalBluetoothManager host;
            if(mHost == null || ((host = mHost.get()) == null)) {
                return;
            }
            if(msg.what == WHAT_HANDLE_BT_STATE) {
                host.notifyAllListener((Intent) msg.obj);
            }
            super.handleMessage(msg);
        }

        private void destroy() {
            if(mHost != null) {
                mHost.clear();
                mHost = null;
            }
            removeCallbacksAndMessages(null);

        }
    }






    public interface BluetoothStateListener {
        void onBluetoothStateChanged(Intent intent);
    }

    public static class BTState {
        //public List<BluetoothDevice> m

    }

    public static BluetoothAdapter getBTAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }



    public static boolean isConnectedA2DP() {
        BluetoothAdapter adapter = getBTAdapter();
        if(adapter == null || !adapter.isEnabled()) {
            return false;
        }
        int state = adapter.getProfileConnectionState(BluetoothProfile.A2DP);
        return state == BluetoothProfile.STATE_CONNECTED;
    }


    private static class BTReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }








}
