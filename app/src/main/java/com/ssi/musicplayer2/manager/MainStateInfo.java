package com.ssi.musicplayer2.manager;



public class MainStateInfo {

    public static final int UNINIT_STATE = -1;

    public static final int BT_NO_AND_USB_NO= 0;

    public static final int BT_NO_AND_USB_YES = 1;

    public static final int BT_YES_AND_USB_NO= 2;

    public static final int BT_YES_AND_USB_YES = 3;



    public int mConnectedState = UNINIT_STATE;


    public boolean mInit = false;


    public boolean isConnectStateChange = false;
    public boolean isBtConnectChange=false;

    public int mBtState = 0;
    public int mUsbState = 0;
    public int mUsbCount = 0;

    public void setBtState(int state) {
        mBtState = state;
        calculationState();
    }

    public void setUsbState(int state) {
        mUsbState = state;
        calculationState();
    }

    public void UsbCountAdd() {
        mUsbCount++;
        mUsbState = mUsbCount >= 1 ? 1 :0;
        calculationState();
    }
    public void UsbCountSub() {
        mUsbCount--;
        mUsbState = mUsbCount >= 1 ? 1 :0;
        calculationState();
    }

    private void calculationState() {
        mConnectedState = mUsbState | (mBtState << 1);
    }


    public static final int SCAN_STATE_NULL = 0;
    public static final int SCAN_STATE_BEGIN = 1;
    public static final int SCAN_STATE_END = 2;
    public boolean isScanStateChange = false;
    public int mScanState = SCAN_STATE_NULL;



}
