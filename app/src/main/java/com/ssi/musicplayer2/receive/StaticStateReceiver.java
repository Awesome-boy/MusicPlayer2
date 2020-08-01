package com.ssi.musicplayer2.receive;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import com.ssi.musicplayer2.utils.Logger;

public class StaticStateReceiver extends BroadcastReceiver {
    private static final String TAG = StaticStateReceiver.class.getSimpleName();
    private static final ComponentName TAG_SERVICE = new ComponentName("com.ssi.musicplayer2",
            "com.ssi.musicplayer2.manager.MainIntentService");

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(TAG, "onReceive");
        if(context == null || intent == null) {
            Logger.d(TAG, "StaticStateReceiver return for null");
            return;
        }
        intent.setComponent(TAG_SERVICE);
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
    }
}
