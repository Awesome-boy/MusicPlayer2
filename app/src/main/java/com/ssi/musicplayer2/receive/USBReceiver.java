package com.ssi.musicplayer2.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ssi.musicplayer2.manager.MainStateInfo;
import com.ssi.musicplayer2.utils.Logger;

import org.greenrobot.eventbus.EventBus;

public class USBReceiver extends BroadcastReceiver {
    private static final String TAG = USBReceiver.class.getSimpleName();
    private static final String ACTION_REMOVE = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private MainStateInfo mainStateInfo = new MainStateInfo();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            String mountPath = intent.getData().getPath();
            if (!TextUtils.isEmpty(mountPath)) {
                //读取到U盘路径再做其他业务逻辑
                mainStateInfo.setUsbState(1);
                mainStateInfo.isBtConnectChange=false;
                EventBus.getDefault().post(mainStateInfo);
            }
        } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) ||
                action.equals(Intent.ACTION_MEDIA_EJECT) || action.equals(ACTION_REMOVE)) {
            mainStateInfo.isBtConnectChange=false;
            mainStateInfo.setUsbState(0);
            EventBus.getDefault().post(mainStateInfo);
        }else if(Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
            if (mainStateInfo.mUsbState<=0){
                return;
            }
            mainStateInfo.mScanState = MainStateInfo.SCAN_STATE_BEGIN;
            Logger.d(TAG, "ACTION_MEDIA_SCANNER_STARTED intent:" + intent.getData().toString());
            //SharedPreferencesManager.putInt(mAppContext, SharedPreferencesManager.KEY_SCAN_STATE, MainStateInfo.SCAN_STATE_BEGIN);
            mainStateInfo.isConnectStateChange = false;
            mainStateInfo.isScanStateChange = true;
            mainStateInfo.isBtConnectChange=false;
            Log.d("zt","---扫描中--"+"ACTION_MEDIA_SCANNER_STARTED");
        }else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            if (mainStateInfo.mUsbState<=0){
                return;
            }
            Logger.d(TAG, "ACTION_MEDIA_SCANNER_FINISHED intent:" + intent.getData().toString());
            mainStateInfo.mScanState = MainStateInfo.SCAN_STATE_END;
            // SharedPreferencesManager.putInt(mAppContext, SharedPreferencesManager.KEY_SCAN_STATE,  MainStateInfo.SCAN_STATE_END);
            mainStateInfo.isConnectStateChange = false;
            mainStateInfo.isBtConnectChange=false;
            mainStateInfo.isScanStateChange = true;
            Log.d("zt","---扫描完毕--");
        }

    }

}
