package com.ssi.musicplayer2.btFragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.btFragment.client.LocalBluetoothManager;


public class BTConnectionFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = BTConnectionFragment.class.getSimpleName();
    private static final String BACK_STACK = TAG + "_BACK";

    private AppCompatButton mSure = null;
    private AppCompatButton mCancel = null;

    public BTConnectionFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bt_connection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSure = view.findViewById(R.id.sure);
        mSure.setOnClickListener(this);
        mCancel = view.findViewById(R.id.cancel);
        mCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FragmentActivity activity = getActivity();
        if(activity == null || activity.isDestroyed() || activity.isFinishing()) {
            return;
        }
        switch (v.getId()) {
            case R.id.sure:
                Intent bluetoothIntent = new Intent();
                bluetoothIntent.addCategory(Intent.CATEGORY_DEFAULT);
                bluetoothIntent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
                activity.startActivity(bluetoothIntent);
                break;
            case R.id.cancel:
                activity.finish();
                break;
                default:
                    break;
        }
    }


    //----------public api----------

    public void startThisFragmentIfNeed(Fragment fragment, FragmentManager fm) {
        if(LocalBluetoothManager.isConnectedA2DP()) {
            return;
        }
        if(fragment == null || fragment.getView() == null || fragment.getView().getParent() ==null || fm == null) {
            return;
        }

        FragmentTransaction ft =  fm.beginTransaction();
        ft.hide(fragment)
                .replace( ((ViewGroup)fragment.getView().getParent()).getId(), this, TAG)
                .addToBackStack(BACK_STACK)
                .commit();
    }


    public void finishThisFragment(FragmentActivity activity) {
        if(activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        FragmentManager fm = activity.getSupportFragmentManager();
        fm.popBackStack(BACK_STACK, 1);
    }
}
