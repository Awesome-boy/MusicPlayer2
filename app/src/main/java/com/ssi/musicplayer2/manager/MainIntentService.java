package com.ssi.musicplayer2.manager;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.ssi.musicplayer2.utils.Logger;

public class MainIntentService extends IntentService {

    private static final String TAG = MainIntentService.class.getSimpleName();
    private static final ComponentName MEDIA_SERVICE = new ComponentName("com.ssi.musicplayer2",
            "com.ssi.musicplayer2.manager.MediaService");

    public MainIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null) {
            Logger.w(TAG, "onHandleIntent return for null");
            return;
        }
        Logger.w(TAG, "onHandleIntent");
        handleIntent(intent);
    }



    @WorkerThread
    private void handleIntent(Intent intent) {
        String action = "";
        if(intent == null || TextUtils.isEmpty(action = intent.getAction())) {
            Logger.w(TAG, "handleIntent return for null");
            return;
        }
        Logger.w(TAG, "handleIntent action:" + action);
        final Context context = getApplicationContext();
        final Uri uri = intent.getData();

        String scheme;
        if(Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            if(uri == null || TextUtils.isEmpty(scheme = uri.getScheme()) ||
                    !"file".equals(scheme)) {
                Logger.w(TAG, "handleIntent return for usb type error");
                return;
            }
            //SharedPreferencesManager.putBoolean(context, SharedPreferencesManager.KEY_IS_USB_SD, true);
            intent.setComponent(MEDIA_SERVICE);
            startServiceAsUser(intent, UserHandle.SYSTEM);
        }else if(Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            if(uri == null || TextUtils.isEmpty(scheme = uri.getScheme()) ||
                    !"file".equals(scheme)) {
                Logger.w(TAG, "handleIntent return for usb type error");
                return;
            }
            //SharedPreferencesManager.putBoolean(context, SharedPreferencesManager.KEY_IS_USB_SD, false);
            intent.setComponent(MEDIA_SERVICE);
            startServiceAsUser(intent, UserHandle.SYSTEM);
        }else if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            intent.setComponent(MEDIA_SERVICE);
            startServiceAsUser(intent, UserHandle.SYSTEM);
        }else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)
                || BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            intent.setComponent(MEDIA_SERVICE);
            startServiceAsUser(intent, UserHandle.SYSTEM);
        }else if(Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
            if(uri == null || TextUtils.isEmpty(scheme = uri.getScheme()) ||
                    !"file".equals(scheme)) {
                Logger.w(TAG, "handleIntent ACTION_MEDIA_SCANNER_STARTED return for usb type error");
                return;
            }
            //SharedPreferencesManager.putInt(context, SharedPreferencesManager.KEY_SCAN_STATE, 1);
        }else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            if(uri == null || TextUtils.isEmpty(scheme = uri.getScheme()) ||
                    !"file".equals(scheme)) {
                Logger.w(TAG, "handleIntent ACTION_MEDIA_SCANNER_FINISHED return for usb type error");
                return;
            }

           // SharedPreferencesManager.putInt(context, SharedPreferencesManager.KEY_SCAN_STATE, 0);
        }


    }









}
