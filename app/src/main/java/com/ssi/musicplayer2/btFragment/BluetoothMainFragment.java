package com.ssi.musicplayer2.btFragment;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.btFragment.client.BluetoothConnectionHelper;
import com.ssi.musicplayer2.utils.Logger;


public class BluetoothMainFragment extends SubFragment implements BluetoothConnectionHelper.ConnectionStateListener{


    private static final String TAG = BluetoothMainFragment.class.getSimpleName();

    private BluetoothConnectionHelper mBluetoothConnectionHelper;

    private FragmentManager mFm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mBluetoothConnectionHelper = BluetoothConnectionHelper.getInstance();
        mBluetoothConnectionHelper.addConnectionListenr(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bluetooth_main_fragment, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onDestroy() {
        if (mBluetoothConnectionHelper != null) {
            mBluetoothConnectionHelper.rmConnectionListenr(this);
            mBluetoothConnectionHelper = null;
        }
        super.onDestroy();
    }

    //BluetoothConnectionHelper.ConnectionStateListener
    @Override
    public void onConnectionStateChanged(boolean isConnectioned) {
        toggleFragment();
    }

    private BTConnectionFragment mBTConnectionFragment = null;
    private BluetoothMusicMainFragment mBluetoothMusicMainFragment = null;

    private void initView(View view) {
        toggleFragment();
    }

    private void toggleFragment() {
        final FragmentActivity activity = getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            Logger.e(TAG, "initView return for host activity is null");
            return;
        }
//        getActivity().findViewById(R.id.empty_view).setVisibility(View.GONE);
        if (mFm == null) {
            mFm = activity.getSupportFragmentManager();
        }
        if (BluetoothConnectionHelper.isConnectedToOtherDevice() == BluetoothAdapter.STATE_DISCONNECTED) {
            Log.d("zt","-- 蓝牙未连接的fragment----");
            if (mBTConnectionFragment == null) {
                mBTConnectionFragment = new BTConnectionFragment();
            }
            mFm.beginTransaction().replace(R.id.fragment_container, mBTConnectionFragment).commit();
        } else {
            Log.d("zt","-- 蓝牙连接的fragment----");
            if (mBluetoothMusicMainFragment == null) {
                mBluetoothMusicMainFragment = new BluetoothMusicMainFragment();
            }
            mFm.beginTransaction().replace(R.id.fragment_container, mBluetoothMusicMainFragment).commit();


        }

    }

    @Override
    public void onResume() {
        super.onResume();
        toggleFragment();
    }
}
