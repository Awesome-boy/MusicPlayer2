package com.ssi.musicplayer2.manager;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;


import com.ssi.musicplayer2.utils.Logger;
import com.ssi.musicplayer2.utils.Utils;

import java.util.List;

public class MainManager extends BaseManager implements Observer<Object> {

    private static final String TAG = MainManager.class.getSimpleName();


    private static final int WHAT_INIT = 100;



    private static MainManager sInstance;
    private MainStateInfoViewModel mMainStateInfoViewModel;
    private final MainStateInfo mMainStateInfo = new MainStateInfo();


    public static synchronized MainManager createInstance(Context context, LifecycleOwner owner) {

        sInstance = createInstance(context, owner, sInstance, MainManager.class);

        return sInstance;
    }

    public static MainManager getInstance() {
        return sInstance;
    }


    @Override
    protected void initState(Context context, LifecycleOwner owner) {
        super.initState(context, owner);

    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        super.onCreate(owner);
        mMainStateInfoViewModel = MainStateInfoViewModel.getInstance();
        mMainStateInfoViewModel.addMediaServiceIntentListener(owner, this);
        sendEmptyMessToBgHandler(WHAT_INIT);
    }


    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        super.onDestroy(owner);
        mMainStateInfoViewModel.removeMediaServiceIntentListener(this);
    }

    @Override
    protected IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter.addDataScheme("file");
        return filter;
    }

    @Override
    protected void handleLocalMessage(Message message) {
        super.handleLocalMessage(message);
        final int what = message.what;
        if(what == WHAT_INIT) {
            //init();
        }
    }

    // Observer<Object>
    @Override
    public void onChanged(Object o) {
        if(o instanceof  Intent) {
            handIntent((Intent) o);
        }

    }

    @WorkerThread
    private void init() {
        mMainStateInfo.setBtState(0);
        mMainStateInfo.setUsbState(0);
        mMainStateInfo.mScanState = MainStateInfo.SCAN_STATE_NULL; //SharedPreferencesManager.
        mMainStateInfo.isScanStateChange = true;
        mMainStateInfo.isBtConnectChange=false;
        mMainStateInfo.mInit = true;
        mMainStateInfoViewModel.postMainStateInfo(mMainStateInfo);
    }

    @Override
    protected void handBroadcastIntent(Intent intent) {
        super.handBroadcastIntent(intent);
        handIntent(intent);
    }

    public void handIntent(Intent intent) {
        String action = "";
        if(intent == null || TextUtils.isEmpty(action = intent.getAction())) {
            Logger.w(TAG, "handIntent");
            return;
        }
        Logger.d(TAG, "handleIntent action:" + action);
        final Uri uri = intent.getData();
        String scheme;
        if(Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            if(uri == null || TextUtils.isEmpty(scheme = uri.getScheme()) ||
                    !"file".equals(scheme)) {
                Logger.w(TAG, "handleIntent return for usb type error");
                return;
            }

            Logger.d(TAG, "ACTION_MEDIA_MOUNTED--------" + action);

            List<String> sdDir= Utils.getUSBPaths(mAppContext);
            Log.d("zt","usb----"+sdDir.size());
            if (sdDir!=null&&sdDir.size()>0){
                mMainStateInfo.UsbCountAdd();
                mMainStateInfo.setUsbState(1);
            }else {
                mMainStateInfo.setUsbState(0);

            }
            mMainStateInfo.mScanState = MainStateInfo.SCAN_STATE_NULL;
            mMainStateInfo.isConnectStateChange = true;
            mMainStateInfo.isBtConnectChange=false;
            mMainStateInfoViewModel.postMainStateInfo(mMainStateInfo);
        }else if(Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            if(uri == null || TextUtils.isEmpty(scheme = uri.getScheme()) ||
                    !"file".equals(scheme)) {
                Logger.w(TAG, "handleIntent return for usb type error");
                return;
            }
            mMainStateInfo.isConnectStateChange = true;
            mMainStateInfo.isBtConnectChange=false;
            mMainStateInfo.UsbCountSub();
            mMainStateInfo.setUsbState(0);
            mMainStateInfoViewModel.postMainStateInfo(mMainStateInfo);
        }else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            mMainStateInfo.setBtState(1);
            mMainStateInfo.isConnectStateChange = true;
            mMainStateInfo.isBtConnectChange=true;
            mMainStateInfoViewModel.postMainStateInfo(mMainStateInfo);
        }else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            mMainStateInfo.setBtState(0);
            mMainStateInfo.isConnectStateChange = true;
            mMainStateInfo.isBtConnectChange=true;
            mMainStateInfoViewModel.postMainStateInfo(mMainStateInfo);
        }else if(Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
            if(uri == null || TextUtils.isEmpty(scheme = uri.getScheme()) ||
                    !"file".equals(scheme) || mMainStateInfo.mUsbState == 0) {
                Logger.w(TAG, "handleIntent ACTION_MEDIA_SCANNER_STARTED return for usb type error");
                return;
            }
            if (mMainStateInfo.mUsbCount<=0){
                return;
            }
            mMainStateInfo.mScanState = MainStateInfo.SCAN_STATE_BEGIN;
            Logger.d(TAG, "ACTION_MEDIA_SCANNER_STARTED intent:" + intent.getData().toString());
            mMainStateInfo.isConnectStateChange = false;
            mMainStateInfo.isScanStateChange = true;
            mMainStateInfoViewModel.postMainStateInfo(mMainStateInfo);
        }else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            if (mMainStateInfo.mUsbCount<=0){
                return;
            }
            if(uri == null || TextUtils.isEmpty(scheme = uri.getScheme()) ||
                    !"file".equals(scheme) || mMainStateInfo.mUsbState == 0) {
                Logger.w(TAG, "handleIntent ACTION_MEDIA_SCANNER_FINISHED return for usb type error");
                return;
            }
            Logger.d(TAG, "ACTION_MEDIA_SCANNER_FINISHED intent:" + intent.getData().toString());
            mMainStateInfo.mScanState = MainStateInfo.SCAN_STATE_END;
            mMainStateInfo.isConnectStateChange = false;
            mMainStateInfo.isScanStateChange = true;
            mMainStateInfoViewModel.postMainStateInfo(mMainStateInfo);
        }
    }
}
