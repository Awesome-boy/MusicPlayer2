package com.ssi.musicplayer2.receive;


import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.ssi.musicplayer2.manager.MainStateInfo;

import org.greenrobot.eventbus.EventBus;

public class BluetoothMonitorReceiver extends BroadcastReceiver {
    private MainStateInfo mMainStateInfo = new MainStateInfo();
    private static final String ACTION_REMOVE = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    mMainStateInfo.isBtConnectChange=true;
                    mMainStateInfo.setBtState(1);
                    EventBus.getDefault().post(mMainStateInfo);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mMainStateInfo.setBtState(0);
                    mMainStateInfo.isBtConnectChange=true;
                    EventBus.getDefault().post(mMainStateInfo);
                    break;
            }
        }
    }
}
