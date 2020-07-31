package com.ssi.musicplayer2.btFragment.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import com.ssi.musicplayer2.utils.Logger;


public class Utils {


    private static ComponentName[] mBTMediaBrowserServices = new ComponentName[] {
        new ComponentName("com.android.bluetooth", "com.android.bluetooth.avrcpcontroller.BluetoothMediaBrowserService"),
        new ComponentName("com.android.bluetooth", "com.android.bluetooth.a2dpsink.mbs.A2dpMediaBrowserService")

    };

    public static ComponentName getBTMediaBrowserService(Context context) {
        return new ComponentName("com.android.bluetooth", "com.android.bluetooth.a2dpsink.mbs.A2dpMediaBrowserService");
        /*final int N = mBTMediaBrowserServices.length;
        PackageManager pm = context.getApplicationContext().getPackageManager();
        ComponentName componentName = null;
        Logger.d("MYTEST", "getBTMediaBrowserService ");
        for(int i=0;i<N;i++) {
            Logger.d("MYTEST", "getBTMediaBrowserService i:" + i);
            if(HasService(mBTMediaBrowserServices[i], pm)) {
                Logger.d("MYTEST", "getBTMediaBrowserService i:" + i +",yes");
                componentName = mBTMediaBrowserServices[i];
                break;
            }
        }
        return componentName;*/
    }


    public static boolean HasService(ComponentName com, PackageManager pm) {
        Logger.d("MYTEST", "HasService ");
        ServiceInfo service = null;
        Intent intent = new Intent("android.media.browse.MediaBrowserService");
        intent.setComponent(com);
        intent.resolveSystemService(pm, 0);

        pm.queryIntentServices(intent, 0);
        try {
            service = pm.getServiceInfo(com, PackageManager.MATCH_SYSTEM_ONLY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Logger.d("MYTEST", "HasService 1");
            return false;
        }
        if(service != null) {
            Logger.d("MYTEST", "HasService 2 service:" + service.toString());
        }

        if(service == null) {
            Logger.d("MYTEST", "HasService null");
            return false;
        }
        return service.getClass().equals(com.getClass());
    }
}



