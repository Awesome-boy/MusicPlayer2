package com.ssi.musicplayer2.receive;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;


public class BluetoothMusicReceiver extends BroadcastReceiver{

    private static final String TAG = BluetoothMusicReceiver.class.getSimpleName();

    private static final ComponentName MAIN_INTENT_SERVICE = new ComponentName("com.ssi.musicplayer2",
            "com.ssi.musicplayer2.manager.MainIntentService");


    @Override
    public void onReceive(Context context, Intent intent) {
        if(context == null || intent == null) {
            return;
        }
        String action = intent.getAction();
        final Context context1 = context;
        context1.startForegroundService(intent);
        intent.setComponent(MAIN_INTENT_SERVICE);
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
//        if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
//            context1.startForegroundService(intent);
//            intent.setComponent(MAIN_INTENT_SERVICE);
//            context.startServiceAsUser(intent, UserHandle.SYSTEM);
//        }else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) /*&& BluetoothMusicService.mServiceIsStart.get()*/) {
//            intent.setComponent(MAIN_INTENT_SERVICE);
//            context.startServiceAsUser(intent, UserHandle.SYSTEM);
//        }

    }




}