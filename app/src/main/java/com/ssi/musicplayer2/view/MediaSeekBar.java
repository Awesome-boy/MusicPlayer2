package com.ssi.musicDemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;

import java.util.ArrayList;
import java.util.List;

public class MediaSeekBar extends AppCompatSeekBar
        implements SeekBar.OnSeekBarChangeListener{

    private static final String TAG = MediaSeekBar.class.getSimpleName();
    private List<OnSeekBarChangeListener> mOnSeekBarChangeListener = new ArrayList<>();

    public MediaSeekBar(Context context) {
        this(context, null);
    }

    public MediaSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs,android.R.attr.seekBarStyle);
    }

    public MediaSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mOnSeekBarChangeListener.clear();
        setOnSeekBarChangeListener(null);
    }





    //----------------实现接口SeekBar.OnSeekBarChangeListene------------------------
    @Override
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
        for(OnSeekBarChangeListener listener : mOnSeekBarChangeListener) {
            listener.onProgressChanged(seekbar, progress, fromUser);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        for(OnSeekBarChangeListener listener : mOnSeekBarChangeListener) {
            listener.onStartTrackingTouch(seekBar);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        for(OnSeekBarChangeListener listener : mOnSeekBarChangeListener) {
            listener.onStopTrackingTouch(seekBar);
        }
    }






    public void addOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        if(mOnSeekBarChangeListener.contains(listener)) {
            return;
        }
        mOnSeekBarChangeListener.add(listener);
    }

    public void rmOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mOnSeekBarChangeListener.remove(listener);
    }

}


