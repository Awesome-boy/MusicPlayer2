package com.ssi.musicplayer2.manager;

public class ScanState {

    public static final int STATE_SCAN_NULL = 0;

    public static final int STATE_BEGIN_SCAN_USB = 1;

    public static final int STATE_END_SCAN_USB = 2;


    public volatile int mScanState =  STATE_SCAN_NULL;

    public volatile boolean mIsStateChange = false;

    public volatile boolean mHasAudio = false;












}
