package com.ssi.musicplayer2.utils;


import android.util.Log;

public class Logger {

    public static final boolean V_LOG = true;
    public static final boolean D_LOG = true;
    public static final boolean I_LOG = true;
    public static final boolean W_LOG = true;
    public static final boolean E_LOG = true;




    /**
     *
     * @param tag
     * @param msg
     * @return
     */
    public static int v(String tag, String msg) {
        if(!V_LOG) {
            return -1;
        }
        return Log.v(tag, msg);
    }


    public static int v(String tag, String msg, Throwable tr) {
        if (!V_LOG) {
            return -1;
        }
        return Log.v(tag, msg, tr);
    }



    public static int d(String tag, String msg) {
        if (!D_LOG) {
            return -1;
        }
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (!D_LOG) {
            return -1;
        }
        return Log.d(tag, msg, tr);
    }



    public static int i(String tag, String msg) {
        if (!I_LOG) {
            return -1;
        }
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (!I_LOG) {
            return -1;
        }
        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        if (!W_LOG) {
            return -1;
        }
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (!W_LOG) {
            return -1;
        }
        return Log.w(tag, msg, tr);
    }


    public static int e(String tag, String msg) {
        if (!E_LOG) {
            return -1;
        }
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (!E_LOG) {
            return -1;
        }
        return Log.e(tag, msg, tr);
    }
}

