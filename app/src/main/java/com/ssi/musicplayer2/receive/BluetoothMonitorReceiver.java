package com.ssi.musicplayer2.receive;


import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.text.TextUtils;

import com.ssi.musicplayer2.btFragment.service.BluetoothMusicService;
import com.ssi.musicplayer2.manager.MainStateInfo;
import com.ssi.musicplayer2.utils.Logger;

import org.greenrobot.eventbus.EventBus;

public class BluetoothMonitorReceiver extends BroadcastReceiver {
    private MainStateInfo mMainStateInfo = new MainStateInfo();
    private static final String ACTION_REMOVE = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    private static final ComponentName BT_MUSIC_SERVICE = new ComponentName("com.ssi.musicplayer2",
            "com.ssi.musicplayer2.btFragment.service.BluetoothMusicService");
//    private static final ComponentName MAIN_INTENT_SERVICE = new ComponentName("com.ssi.musicplayer",
//            "com.ssi.musicplayer.player.functions.service.MainIntentService");

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            Logger.d("zt", "onReceive action:" + action + ",btmusic is start:" + BluetoothMusicService.mServiceIsStart.get());
            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    mMainStateInfo.isBtConnectChange=true;
                    mMainStateInfo.setBtState(1);
                    intent.setComponent(BT_MUSIC_SERVICE);
                    context.startForegroundService(intent);
                    context.startServiceAsUser(intent, UserHandle.SYSTEM);
                    EventBus.getDefault().post(mMainStateInfo);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mMainStateInfo.setBtState(0);
                    mMainStateInfo.isBtConnectChange=true;
                    EventBus.getDefault().post(mMainStateInfo);
                    Intent stopIntent = new Intent();
                    stopIntent.setComponent(BT_MUSIC_SERVICE);
                    context.stopService(stopIntent);
                    context.startServiceAsUser(intent, UserHandle.SYSTEM);
                    break;
            }
        }
    }
}
