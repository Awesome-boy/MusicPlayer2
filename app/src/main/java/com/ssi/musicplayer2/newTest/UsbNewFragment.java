package com.ssi.musicplayer2.newTest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.constraintlayout.widget.Placeholder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.dfssi.android.d760ui.TipsDialog;
import com.mediabrowser.xiaxl.client.MusicManager;
import com.mediabrowser.xiaxl.client.listener.OnSaveRecordListener;
import com.ssi.musicplayer2.MyApplication;
import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.btFragment.SubFragment;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.database.DBNewManager;
import com.ssi.musicplayer2.javabean.MusicInfo;
import com.ssi.musicplayer2.manager.MainStateInfo;
import com.ssi.musicplayer2.service.AudioPlayerService;
import com.ssi.musicplayer2.service.MediaPlayerHelper;
import com.ssi.musicplayer2.service.MessageEvent;
import com.ssi.musicplayer2.usbFragment.AlbumFragment;
import com.ssi.musicplayer2.usbFragment.ArtistFragment;
import com.ssi.musicplayer2.usbFragment.FolderFragment;
import com.ssi.musicplayer2.usbFragment.MusicInfoBean;
import com.ssi.musicplayer2.usbFragment.SinleFragment;
import com.ssi.musicplayer2.utils.ChineseToEnglish;
import com.ssi.musicplayer2.utils.Constant;
import com.ssi.musicplayer2.utils.Logger;
import com.ssi.musicplayer2.utils.MyMusicUtil;
import com.ssi.musicplayer2.utils.SPUtils;
import com.ssi.musicplayer2.utils.Utils;
import com.ssi.musicplayer2.view.LyricView;
import com.ssi.musicplayer2.view.MediaSeekBar;
import com.ssi.musicplayer2.view.USBStatusDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UsbNewFragment extends SubFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = UsbNewFragment.class.getSimpleName();
    private View rootView;
    private TextView song_progress_time;
    private TextView song_duration_time;
    private Button btn_play;
    private Button btn_model;
    private Button btn_previous;
    private Button btn_next;
    private MediaSeekBar seekBar;
    private List<MusicInfoBean> mMusicInfos;


    private Context mContext;
    private Message msg;
    private DBNewManager dbManager;

    private ImageView music_lyric;
    private ImageView music_dir;
    private ImageView music_artist;
    private ImageView music_album;
    private Fragment currentFragment;
    private TextView song_title;
    private TextView song_order;
    private TextView song_album;
    private TextView song_artist;
    private FrameLayout frameLayout;
    private View holdView;
    private LyricView mLyricView;
    private Group mLyricGroup;
    private Placeholder mHolderSongAlbum;
    private Placeholder mHolderSongTitle;
    private Placeholder mHolderSongOrder;
    private Placeholder mHolderSongArtist;
    private ValueAnimator mProgressAnimator = null;
    private int musicTime;
    private TipsDialog dialog;
    private MainStateInfo mainStateInfo;
    private ImageView iv_song_img;
    private ImageView iv_song_img_film;
    private MusicManager mMusicManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        initMusicAgent();
        changFragmentListner = (ChangFragmentListner) context;
    }

    /**
     * 初始化音乐引擎
     */
    private void initMusicAgent() {
        // 初始化
        if (mMusicManager == null) {
            mMusicManager = MusicManager.getInstance();
        }
        mMusicManager.init(mContext);
        // 音频变化的监听类
        mMusicManager.addOnAudioStatusListener(mAudioStatusChangeListener);
        // 记录播放记录的监听
        mMusicManager.addOnRecorListener(mOnRecordListener);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        resetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null) {
            //缓存的rootView需要判断是否已经被加过parent， 如果有parent则从parent删除，防止发生这个rootview已经有parent的错误。
            ViewGroup mViewGroup = (ViewGroup) rootView.getParent();
            if (mViewGroup != null) {
                mViewGroup.removeView(rootView);
            }
        }
        rootView = inflater.inflate(R.layout.local_music_player_fragment, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshState();
//        refreshItem();
        if (music_dir.isSelected() || music_album.isSelected() || music_artist.isSelected() || music_lyric.isSelected()) {
            mLyricGroup.setVisibility(View.VISIBLE);
            holdView.setVisibility(View.VISIBLE);
            switchLyicState(View.VISIBLE);
        } else {
            mLyricGroup.setVisibility(View.GONE);
            switchLyicState(View.GONE);
        }
        if (getActivity().isInMultiWindowMode()) {
            iv_song_img.setVisibility(View.GONE);
            iv_song_img_film.setVisibility(View.GONE);
            seekBar.setVisibility(View.INVISIBLE);

        } else {
            iv_song_img.setVisibility(View.VISIBLE);
            iv_song_img_film.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);


        }
    }


    private void switchLyicState(int vi) {
        if (mHolderSongAlbum == null) {
            return;
        }
        if (vi == View.VISIBLE) {
            mHolderSongAlbum.setContentId(R.id.song_album);
            mHolderSongTitle.setContentId(R.id.song_title);
            mHolderSongOrder.setContentId(R.id.song_order);
            mHolderSongArtist.setContentId(R.id.song_artist);
        } else {
            mHolderSongAlbum.setContentId(R.id.holder_view);
            mHolderSongTitle.setContentId(R.id.holder_view);
            mHolderSongOrder.setContentId(R.id.holder_view);
            mHolderSongArtist.setContentId(R.id.holder_view);
        }
    }


    private void refreshItem() {
        if (mMusicInfos != null && mMusicInfos.size() > 0) {
//            if (scanData) {
//                int posId = mMediaPlayerHelper.getPos();
//                String path = musicInfoList.get(posId).getPath();
//                String name = musicInfoList.get(posId).getName();
//                String album = musicInfoList.get(posId).getAlbum();
//                String singer = musicInfoList.get(posId).getSinger();
//                song_title.setText(name);
//                song_order.setText("(" + (posId + 1) + "/" + musicInfoList.size() + "）");
//                song_album.setText(album);
//                song_artist.setText(singer);
//            }
            if (currentFragment instanceof SinleFragment) {
                ((SinleFragment) currentFragment).refreshItem();
            } else if (currentFragment instanceof AlbumFragment) {
                ((AlbumFragment) currentFragment).refreshItem();
            } else if (currentFragment instanceof ArtistFragment) {
                ((ArtistFragment) currentFragment).refreshItem();
            } else if (currentFragment instanceof FolderFragment) {
                ((FolderFragment) currentFragment).refreshItem();
            }
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(Object o) {
        if (o instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) o;
            mMusicInfos = event.getMusicInfoList();
            int pos = event.getPos();
            if (mMusicManager!=null){
                mMusicManager.playMusicList(mMusicInfos,pos);
            }
        } else if (o instanceof MainStateInfo) {
            mainStateInfo = (MainStateInfo) o;
            if (mainStateInfo.mConnectedState == 1 || mainStateInfo.mConnectedState == 3) {
                if (mainStateInfo.mScanState == MainStateInfo.SCAN_STATE_END) {
                    final List<String> path = Utils.getUSBPaths(mContext);
                    if (path != null && path.size() > 0) {
                        if (mainStateInfo.isBtConnectChange) {
                            return;
                        }
                        showDialog(getString(R.string.text_scaning));
                        startScanLocalMusic(path.get(0));

                    }
                }

            } else {
                resetFragment();
            }
        }


    }

    private void showDialog(String msg) {
        if (dialog == null) {
            dialog = TipsDialog.getInstance(mContext, msg);
        }
        dialog.show();
    }

    private void resetFragment() {
        music_artist.setEnabled(false);
        music_lyric.setEnabled(false);
        music_dir.setEnabled(false);
        music_album.setEnabled(false);
        music_lyric.setSelected(false);
        music_dir.setSelected(false);
        music_artist.setSelected(false);
        music_album.setSelected(false);
        song_title.setText("歌曲名称");
        song_order.setText("顺序");
        song_album.setText("专辑名称");
        song_artist.setText("歌手");
        seekBar.setProgress(0);
        frameLayout.setVisibility(View.GONE);
        holdView.setVisibility(View.GONE);
        mLyricGroup.setVisibility(View.GONE);
        music_lyric.setVisibility(View.GONE);
        btn_play.setBackgroundResource(R.drawable.img_play);
    }

    private boolean scanData;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.SCAN_NO_MUSIC:
                    scanData = false;
                    scanComplete();
                    break;
                case Constant.SCAN_ERROR:
                    scanData = false;
                    scanComplete();
                    break;
                case Constant.SCAN_COMPLETE:
                    scanComplete();
                    scanData = true;
                    break;
            }
        }
    };


    private void refreshState() {
    }

    public interface ChangFragmentListner {
        void changeFragment(int position);
    }

    private ChangFragmentListner changFragmentListner;

    private void scanComplete() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        Log.d("zt", "--scanComplete--" + mMusicInfos.size());

        Log.d("zt", "---数据库的所有数据---" + dbManager.getAllMusicFromMusicTable().size());

        if (mMusicInfos != null && mMusicInfos.size() == 0) {
            USBStatusDialog.getInstance(mContext, getString(R.string.no_music_usb)).show();
            if (mainStateInfo != null) {
                if (mainStateInfo.mConnectedState == MainStateInfo.BT_NO_AND_USB_NO || mainStateInfo.mConnectedState == MainStateInfo.BT_NO_AND_USB_YES) {
                    finishThisFragment();
                    getActivity().finish();
                }

            } else {
                if (changFragmentListner != null) {
                    changFragmentListner.changeFragment(1);
                }
            }
        } else {
            if (mMusicInfos.size() > 0) {
                music_artist.setEnabled(true);
                music_lyric.setEnabled(true);
                music_dir.setEnabled(true);
                music_album.setEnabled(true);

                btn_play.setEnabled(true);
                btn_next.setEnabled(true);
                btn_previous.setEnabled(true);
                if (mMusicManager != null) {
                    mMusicManager.playMusicList(mMusicInfos, 0);
                }
            }


        }
        refreshItem();
    }

    public void startScanLocalMusic(final String path) {
        dbManager = DBNewManager.getInstance(MyApplication.getContext());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    querySong1(path);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "run: error = ", e);
                    //扫描出错
                    msg = new Message();
                    msg.what = Constant.SCAN_ERROR;
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    private void querySong1(String filepath) {
        Cursor cursor = MyApplication.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DATA + " like ?",
                new String[]{filepath + "%"},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.getCount() != 0) {
            mMusicInfos = new ArrayList<MusicInfoBean>();
            Log.i(TAG, "run: cursor.getCount() = " + cursor.getCount());
            while (cursor.moveToNext()) {
                long id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                //long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//                String musicFilename = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
//                String genre = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
//                int albumArtResId = (int)cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                String albumArtResName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                File file = new File(path);
                String parentPath = file.getParentFile().getPath();
                Log.d("zt", "--scaning--" + path + "---id--" + String.valueOf(id));
                name = replaseUnKnowe(name);
                singer = replaseUnKnowe(singer);
                album = replaseUnKnowe(album);
                path = replaseUnKnowe(path);

                MusicInfoBean musicInfo = new MusicInfoBean();

                musicInfo.setTitle(name);
                musicInfo.setSinger(singer);
                musicInfo.setAlbum(album);
                musicInfo.setPath(path);
                musicInfo.setMediaId(String.valueOf(id));
                musicInfo.setDuration(duration);
                Log.e(TAG, "run: parentPath = " + parentPath);
                musicInfo.setParentPath(parentPath);
                musicInfo.setFirstLetter(ChineseToEnglish.StringToPinyinSpecial(name).toUpperCase().charAt(0) + "");
                mMusicInfos.add(musicInfo);
                Log.d("zt", "--scaningt--" + mMusicInfos.size());
            }
            // 根据a-z进行排序源数据
            Collections.sort(mMusicInfos);
            Log.d("zt", "--ready--" );
            dbManager.updateAllMusic(mMusicInfos);
            Log.d("zt", "--end--" );
            //扫描完成
            msg = new Message();
            msg.what = Constant.SCAN_COMPLETE;
            handler.sendMessage(msg);  //更新UI界面

        } else {
            msg = new Message();
            msg.what = Constant.SCAN_NO_MUSIC;
            handler.sendMessage(msg);  //更新UI界面
        }
        if (cursor != null) {
            cursor.close();
        }
    }


    public static String replaseUnKnowe(String oldStr) {
        try {
            if (oldStr != null) {
                if (oldStr.equals("<unknown>")) {
                    oldStr = oldStr.replaceAll("<unknown>", "未知");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "replaseUnKnowe: error = ", e);
        }
        return oldStr;
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    private void initView(View view) {
        btn_play = view.findViewById(R.id.button_play);
        btn_play.setEnabled(false);
        btn_play.setOnClickListener(this);

        btn_model = view.findViewById(R.id.button_mix_play);
        btn_model.setOnClickListener(this);
        int modelIndex = SPUtils.getInstance(mContext).getInt("model", 0);
        refreshModelBtn(modelIndex);
        btn_previous = view.findViewById(R.id.button_previous);
        btn_previous.setEnabled(false);
        btn_previous.setOnClickListener(this);

        btn_next = view.findViewById(R.id.button_next);
        btn_next.setEnabled(false);
        btn_next.setOnClickListener(this);

        song_progress_time = view.findViewById(R.id.song_progress_time);
        song_duration_time = view.findViewById(R.id.song_duration_time);

        seekBar = view.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        music_lyric = getActivity().findViewById(R.id.music_lyric);
        music_lyric.setOnClickListener(this);

        music_dir = getActivity().findViewById(R.id.music_dir);
        music_dir.setOnClickListener(this);

        music_artist = getActivity().findViewById(R.id.music_artist);
        music_artist.setOnClickListener(this);

        music_album = getActivity().findViewById(R.id.music_album);
        music_album.setOnClickListener(this);

        song_title = view.findViewById(R.id.song_title);
        song_order = view.findViewById(R.id.song_order);
        song_album = view.findViewById(R.id.song_album);
        song_artist = view.findViewById(R.id.song_artist);
        frameLayout = view.findViewById(R.id.muisc_list);
        holdView = getActivity().findViewById(R.id.empty_view);
        mLyricGroup = view.findViewById(R.id.lyric_group);
        mLyricView = view.findViewById(R.id.custom_lyric_view);
//        mLyricView.setOnPlayerClickListener(this);

        mHolderSongAlbum = view.findViewById(R.id.holder_song_album);
        mHolderSongTitle = view.findViewById(R.id.holder_song_title);
        mHolderSongOrder = view.findViewById(R.id.holder_song_order);
        mHolderSongArtist = view.findViewById(R.id.holder_song_artist);

        iv_song_img = view.findViewById(R.id.song_img);
        iv_song_img_film = view.findViewById(R.id.song_img_film);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_play:
                //todo 播放或者暂停
                if (mIsPlaying) {
                    if (mMusicManager != null) {
                        mMusicManager.pause();
                    }
                } else {
                    if (mMusicManager != null) {
                        mMusicManager.playMusicList(mMusicInfos, 0);
                    }
                }
                break;
            case R.id.button_mix_play:
                //todo 循环模式
                int playmoedl = SPUtils.getInstance(mContext).getInt("model", 0);
                if (playmoedl == 0) {
                    SPUtils.getInstance(mContext).save("model", 1);
                    refreshModelBtn(1);
                } else if (playmoedl == 1) {
                    SPUtils.getInstance(mContext).save("model", 2);
                    refreshModelBtn(2);
                } else if (playmoedl == 2) {
                    SPUtils.getInstance(mContext).save("model", 0);
                    refreshModelBtn(0);
                }

                break;
            case R.id.button_previous:
                //todo 上一首
                Log.d("zt", "--上一首---");
                if (mMusicManager != null) {
                    mMusicManager.skipToPrevious();
                }
                break;
            case R.id.button_next:
                //todo 下一首
                Log.d("zt", "--下一首---");
                if (mMusicManager != null) {
                    mMusicManager.skipToNext();
                }

                break;
            case R.id.music_lyric:
                music_lyric.setSelected(true);
                music_dir.setSelected(false);
                music_artist.setSelected(false);
                music_album.setSelected(false);
                switchFragment(new SinleFragment());
                break;
            case R.id.music_dir:
                music_lyric.setSelected(false);
                music_dir.setSelected(true);
                music_artist.setSelected(false);
                music_album.setSelected(false);
                switchFragment(new FolderFragment());
                break;
            case R.id.music_artist:
                switchFragment(new ArtistFragment());
                music_lyric.setSelected(false);
                music_dir.setSelected(false);
                music_artist.setSelected(true);
                music_album.setSelected(false);
                break;
            case R.id.music_album:
                switchFragment(new AlbumFragment());
                music_lyric.setSelected(false);
                music_dir.setSelected(false);
                music_artist.setSelected(false);
                music_album.setSelected(true);
                break;

        }
    }

    private void refreshModelBtn(int index) {
        switch (index) {
            case 0:
                //全部循环
                btn_model.setBackgroundResource(R.drawable.img_loop_play);
                break;
            case 1:
                //单曲循环
                btn_model.setBackgroundResource(R.drawable.img_single_play);
                break;
            case 2:
                //随机
                btn_model.setBackgroundResource(R.drawable.img_mix_play);
                break;
        }
    }

    //正确的做法
    private void switchFragment(Fragment targetFragment) {
        holdView.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (!targetFragment.isAdded()) {
            transaction.add(R.id.muisc_list, targetFragment)
                    .commit();
        } else {
            transaction.show(targetFragment)
                    .commit();
        }
        currentFragment = targetFragment;
        refreshItem();
        mLyricGroup.setVisibility(View.VISIBLE);
        switchLyicState(View.VISIBLE);

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //更新时实播放进度
        song_progress_time.setText(turnTime(progress));

    }

    /**
     * 秒转为分:秒
     *
     * @param second
     * @return
     */
    public String turnTime(int second) {
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        return (d > 0 ? d > 9 ? d : "0" + d : "00") + ":" + (s > 9 ? s : "0" + s);
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {


    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 数据
     */
    // 是否正在播放的标识
    private boolean mIsPlaying;

    /**
     * 音频播放状态变化的回调
     *
     * @param playbackState
     */
    private void onMediaPlaybackStateChanged(PlaybackStateCompat playbackState) {
        if (playbackState == null) {
            return;
        }
        // 正在播放
        mIsPlaying =
                playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;

        // 更新UI
        if (mIsPlaying) {
            btn_play.setBackgroundResource(R.drawable.img_pause);
        } else {
            btn_play.setBackgroundResource(R.drawable.img_play);
        }

        /**
         * 设置播放进度
         */
        final int progress = (int) playbackState.getPosition();
        seekBar.setProgress(progress);
        switch (playbackState.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
//                final int timeToEnd = (int) ((mSeekBarAudio.getMax() - progress) / playbackState.getPlaybackSpeed());
//                seekBar.startProgressAnima(progress, mSeekBarAudio.getMax(), timeToEnd);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
//                seekBar.stopProgressAnima();
                break;

        }

    }


    /**
     * 播放音频数据 发生变化的回调
     *
     * @param mediaMetadata
     */
    private void onMediaMetadataChanged(MediaMetadataCompat mediaMetadata) {
        if (mediaMetadata == null) {
            return;
        }
        // 音频的标题
        song_title.setText(
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        // 音频作者
        song_artist.setText(
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        // 音频图片
//        mAlbumArtImg.setImageBitmap(MusicLibrary.getAlbumBitmap(
//                MainActivity.this,
//                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));

        // 进度条
        final int max = mediaMetadata != null
                ? (int) mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                : 0;
        seekBar.setProgress(0);
        seekBar.setMax(max);
    }

    // ############################################################################################


    /**
     * 音频变化回调
     */
    MusicManager.OnAudioStatusChangeListener mAudioStatusChangeListener = new MusicManager.OnAudioStatusChangeListener() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            // 播放音频 状态变化
            onMediaPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            // 播放音频变化的回调
            onMediaMetadataChanged(metadata);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            // TODO 播放队列发生变化
        }
    };

    /**
     * 记录播放位置的回调
     */
    OnSaveRecordListener mOnRecordListener = new OnSaveRecordListener() {
        @Override
        public void onSaveRecord(MediaMetadataCompat mediaMetadataCompat, long postion) {
            // TODO 保存播放记录用
        }
    };


}
