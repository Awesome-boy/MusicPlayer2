package com.ssi.musicplayer2.usbFragment;


import android.os.Parcel;
import android.os.Parcelable;

import com.mediabrowser.xiaxl.client.model.IMusicInfo;
import com.ssi.musicplayer2.javabean.MusicInfo;

public class MusicInfoBean implements IMusicInfo,Comparable, Parcelable {
    private String mediaId;
    private String title;
    private long duration;
    private String singer;
    private String album;
    private String path;
    private String parentPath; //父目录路径
    private String firstLetter;


//    private int love; //1设置我喜欢 0未设置


    public MusicInfoBean(Parcel in) {
        mediaId = in.readString();
        title = in.readString();
        duration = in.readLong();
        singer = in.readString();
        album = in.readString();
        path = in.readString();
        parentPath = in.readString();
        firstLetter = in.readString();
    }



    public MusicInfoBean() {

    }

    public static final Creator<MusicInfoBean> CREATOR = new Creator<MusicInfoBean>() {
        @Override
        public MusicInfoBean createFromParcel(Parcel in) {
            return new MusicInfoBean(in);
        }

        @Override
        public MusicInfoBean[] newArray(int size) {
            return new MusicInfoBean[size];
        }
    };

    @Override
    public int compareTo(Object o) {
        MusicInfoBean info = (MusicInfoBean) o;
        return mediaId.compareTo(info.getMediaId());
    }


    public void setPath(String path) {
        this.path = path;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    @Override
    public String getMediaId() {
        return this.mediaId;
    }

    @Override
    public String getSource() {
        return path;
    }

    @Override
    public String getArtUrl() {
        return null;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getArtist() {
        return singer;
    }

    @Override
    public String getAlbum() {
        return album;
    }

    @Override
    public String getAlbumArtUrl() {
        return null;
    }

    @Override
    public String getGenre() {
        return "1";
    }

    @Override
    public String freeType() {
        return "1";
    }

    @Override
    public long getDuration() {
        return duration;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mediaId);
        dest.writeString(title);
        dest.writeLong(duration);
        dest.writeString(singer);
        dest.writeString(album);
        dest.writeString(path);
        dest.writeString(parentPath);
        dest.writeString(firstLetter);
    }
}
