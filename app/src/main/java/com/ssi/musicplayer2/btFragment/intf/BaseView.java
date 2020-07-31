package com.ssi.musicplayer2.btFragment.intf;

public interface BaseView<T extends BasePresenter> {

    void setPresenter(T presenter);


    
}