package com.ssi.musicplayer2.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;


import java.util.List;
import java.util.logging.Logger;

public class MainActivityFragmentAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = MainActivityFragmentAdapter.class.getSimpleName();

    private List<Fragment> mFragments = null;


    public MainActivityFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mFragments = fragments;
    }


    @Override
    public Fragment getItem(int i) {
        if(i > mFragments.size()) {
            return null;
        }
        return mFragments.get(i);
    }


    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void setData(List<Fragment> data) {
        if(data == null || data.isEmpty()) {
            Log.d(TAG, "setData return for null");
            return;
        }
        mFragments = data;
        notifyDataSetChanged();

    }


}



