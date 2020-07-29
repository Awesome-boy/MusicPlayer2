package com.ssi.musicplayer2.service;

import com.ssi.musicplayer2.javabean.MusicInfo;

import java.util.List;

public class MessageEvent {
    private String type;
    private  List<MusicInfo> musicInfoList;
    private int pos;

    public MessageEvent(int position) {
        this.pos=position;
    }

    public MessageEvent() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<MusicInfo> getMusicInfoList() {
        return musicInfoList;
    }

    public void setMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList = musicInfoList;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
