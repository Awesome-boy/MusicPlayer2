package com.ssi.musicplayer2.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {



    private static final String KEY_PREFERENCE = "ssi_key_preference";


    public static final String KEY_IS_USB_SD = "key_is_usb_sd";
    public static final String KEY_IS_BT_CONNECTION = "key_is_bt_connection";

    public static final String KEY_MUSIC_PARENT_ID = "music_parent_id";

    public static final String KEY_SCAN_STATE = "key_scan_state";

    public synchronized static void putString(Context context, String key, String value) {
        synchronized (KEY_PREFERENCE) {
            SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public synchronized static String getString(Context context, String key, String defaultValue) {
        synchronized (KEY_PREFERENCE) {
            SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE);
            return preferences.getString(key, defaultValue);
        }
    }




    public synchronized static void putBoolean(Context context, String key, boolean value) {
        synchronized (KEY_PREFERENCE) {
            SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

    public  static boolean getBoolean(Context context, String key, boolean defaultValue) {
        synchronized (KEY_PREFERENCE) {
            SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE);
            return preferences.getBoolean(key, defaultValue);
        }
    }



    public synchronized static void putInt(Context context, String key, int value) {
        synchronized (KEY_PREFERENCE) {
            SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    public synchronized static int getInt(Context context, String key, int defaultValue) {
        synchronized (KEY_PREFERENCE) {
            SharedPreferences preferences = context.getSharedPreferences(KEY_PREFERENCE, Context.MODE_PRIVATE);
            return preferences.getInt(key, defaultValue);
        }
    }



}
