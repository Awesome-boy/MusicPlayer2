package com.ssi.musicplayer2.btFragment;


import android.view.View;
import android.view.ViewParent;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ssi.musicplayer2.utils.Logger;


public class SubFragment extends Fragment {

    private static final String TAG = SubFragment.class.getSimpleName();



    public void startThisFragment(@IdRes int contentId, FragmentManager fm,
                                  @Nullable Fragment contentFragment){
        FragmentTransaction ft = fm.beginTransaction();
        if(contentFragment != null) {
            ft.hide(contentFragment);
        }
        if(isAddToBackStack()) {
            ft.add(contentId, this, getFragmentTAG()).addToBackStack(getBackTAG()).commit();
        }else {
            ft.add(contentId, this, getFragmentTAG()).commitNow();
        }

    }


    protected boolean isAddToBackStack() {
        return true;
    }

    protected @IdRes int getViewId() {
        final View view = getView();
        ViewParent parent;
        if(view == null || ((parent  = view.getParent()) == null)
                || !(parent instanceof View)) {
            return View.NO_ID;
        }

        return ((View)parent).getId();



    }

    private SubFragment mCurrentSubFragment = null;
    protected void startFragment(SubFragment fragment) {
        final FragmentActivity activity = getHostActivity();
        final int contentId = getViewId();
        if(activity == null || contentId == View.NO_ID || fragment == null) {
            Logger.w(TAG, "startFragment return for null!!! fragment:" + getFragmentTAG());
            return;
        }
        fragment.startThisFragment(contentId, activity.getSupportFragmentManager(), this);
        mCurrentSubFragment = fragment;
    }




    private void finishThisFragment(FragmentManager fm){
        if(fm == null) {
            Logger.w(TAG, "finishThisFragment return for null");
            return;
        }
        if(isAddToBackStack()) {
            fm.popBackStack(getBackTAG(), 1);
        }else if(mCurrentSubFragment != null) {
            fm.beginTransaction().hide(mCurrentSubFragment).commit();
            mCurrentSubFragment = null;
        }
    }

    protected void finishThisFragment() {
        final FragmentActivity activity = getHostActivity();
        if(activity == null) {
            Logger.w(TAG, "finishThisFragment fail for activity is null fragment:" + getFragmentTAG());
            return;
        }

        finishThisFragment(activity.getSupportFragmentManager());


    }


    /**
     * 该方法调用说明parent fragment发生的主列表切换，在此前需要finish fragment
     */
    public void onLeave() {
        final FragmentActivity activity = getHostActivity();
        if(activity == null) {
            Logger.w(getFragmentTAG(), "onLeave failed for null, fragment:" + getFragmentTAG());
            return;
        }
        if(mCurrentSubFragment != null) {
            mCurrentSubFragment.onLeave();
            mCurrentSubFragment = null;
        }
        finishThisFragment(activity.getSupportFragmentManager());
    }




    protected String getFragmentTAG() {
        return "";
    }

    private String getBackTAG() {
        return "BACK_" + getFragmentTAG();
    }



    protected FragmentActivity getHostActivity() {
        final FragmentActivity activity = getActivity();
        return isFinishingOrDestroyed(activity) ? null : activity;
    }


    protected boolean isFinishingOrDestroyed(FragmentActivity activity) {
        return  activity == null || activity.isDestroyed() || activity.isFinishing();
    }

    public void onMusicViewClick(View view) {


    }
}