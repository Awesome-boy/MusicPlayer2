package com.ssi.musicplayer2.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import com.ssi.musicplayer2.javabean.MusicInfo;

import java.util.List;

public class MusicLibraryRecyclerView extends RecyclerView{

    public MusicLibraryRecyclerView(Context context) {
        this(context, null);
    }


    public MusicLibraryRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public MusicLibraryRecyclerView(Context context,
                                    AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }



}



