package com.ssi.musicplayer2.usbFragment;

import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.constraintlayout.widget.Placeholder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.dfssi.android.d760ui.TipsDialog;
import com.ssi.musicplayer2.MainActivity;
import com.ssi.musicplayer2.MyApplication;
import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.btFragment.SubFragment;
import com.ssi.musicplayer2.btFragment.client.BluetoothConnectionHelper;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.javabean.MusicInfo;
import com.ssi.musicplayer2.manager.MainStateInfo;
import com.ssi.musicplayer2.service.AudioPlayerService;
import com.ssi.musicplayer2.service.MediaPlayerHelper;
import com.ssi.musicplayer2.service.MessageEvent;
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UsbFragment extends SubFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, ServiceConnection, MediaPlayerHelper.MediaPlayerUpdateCallBack, LyricView.OnPlayerClickListener, ValueAnimator.AnimatorUpdateListener {

    private static final String TAG = UsbFragment.class.getSimpleName();
    private View rootView;
    private TextView song_progress_time;
    private TextView song_duration_time;
    private Button btn_play;
    private Button btn_model;
    private Button btn_previous;
    private Button btn_next;
    private MediaSeekBar seekBar;
    private List<MusicInfo> musicInfoList;


    private MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_NONE://无任何状态
                case PlaybackStateCompat.STATE_PLAYING:
                    btn_play.setBackgroundResource(R.drawable.img_pause);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_STOPPED:
                    btn_play.setBackgroundResource(R.drawable.img_play);
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT://下一首
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS://上一首
                    break;
                case PlaybackStateCompat.STATE_FAST_FORWARDING://快进
                    break;
                case PlaybackStateCompat.STATE_REWINDING://快退
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null || mMediaPlayerHelper == null) {
                Logger.i(TAG, "onMetadataChanged return for null");
                return;
            }
            int currentId = MyMusicUtil.getIntShared(Constant.KEY_ID) - 1;
            String path = dbManager.getAllMusicFromMusicTable().get(currentId).getPath();
            File lyricFile = new File(path.substring(0, path.lastIndexOf(".")) + ".lrc");
            if (lyricFile.exists()) {
                mLyricView.reset();
                mLyricView.setLyricFile(lyricFile, Utils.getCharset(lyricFile.getName()));

            } else {
                mLyricView.reset();
            }
            refreshItem();
        }
    };
    private Context mContext;
    private AudioPlayerService musicService;
    private MediaPlayerHelper mMediaPlayerHelper;
    private MediaControllerCompat mMediaController;
    private Message msg;
    private DBManager dbManager;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        changFragmentListner = (ChangFragmentListner) context;
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
        Intent intent = new Intent(mContext, AudioPlayerService.class);
        mContext.bindService(intent, this, 0);
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
        if (mMediaPlayerHelper != null && musicInfoList != null && musicInfoList.size() > 0) {
            if (SPUtils.getInstance(mContext).getBoolean("usb", false) && scanData) {
                int posId = mMediaPlayerHelper.getPos();
                String path = musicInfoList.get(posId).getPath();
                String name = musicInfoList.get(posId).getName();
                String album = musicInfoList.get(posId).getAlbum();
                String singer = musicInfoList.get(posId).getSinger();
                song_title.setText(name);
                song_order.setText("(" + (posId + 1) + "/" + musicInfoList.size() + "）");
                song_album.setText(album);
                song_artist.setText(singer);
            }
            if (currentFragment instanceof SinleFragment) {
                ((SinleFragment) currentFragment).refreshItem();
            } else if (currentFragment instanceof AlbumFragment) {
                ((AlbumFragment) currentFragment).refreshItem();
            } else if (currentFragment instanceof ArtistFragment) {
                ((ArtistFragment) currentFragment).refreshItem();
            }
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(Object o) {
        if (o instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) o;
            musicInfoList = event.getMusicInfoList();
            int pos = event.getPos();
            if (mMediaPlayerHelper != null) {
                mMediaPlayerHelper.setPlayeData(musicInfoList);
                mMediaPlayerHelper.setPlayID(pos);
            }
        } else if (o instanceof MainStateInfo) {
            mainStateInfo = (MainStateInfo) o;
            if (mainStateInfo.mConnectedState == 1 || mainStateInfo.mConnectedState == 3) {
                if (mainStateInfo.mScanState == MainStateInfo.SCAN_STATE_END) {
                    final List<String> path = Utils.getUSBPaths(mContext);
                    if (path != null && path.size() > 0) {
                        if (mainStateInfo.isBtConnectChange){
                            return;
                        }
                        showDialog(getString(R.string.text_scaning));
                        startScanLocalMusic(path.get(0));
                        music_lyric.setClickable(true);
                        music_dir.setClickable(true);
                        music_artist.setClickable(true);
                        music_album.setClickable(true);
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
        if (mMediaController != null) {
            mMediaController.getTransportControls().pause();
            mMediaPlayerHelper.mMediaPlayer.reset();
        }
        music_lyric.setClickable(false);
        music_dir.setClickable(false);
        music_artist.setClickable(false);
        music_album.setClickable(false);
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
        if (mMediaController != null) {
            if (mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                btn_play.setBackgroundResource(R.drawable.img_pause);
            } else {
                btn_play.setBackgroundResource(R.drawable.img_play);
            }
        } else {
            btn_play.setBackgroundResource(R.drawable.img_play);
        }
    }

    public interface ChangFragmentListner {
        void changeFragment(int position);
    }

    private ChangFragmentListner changFragmentListner;

    private void scanComplete() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        musicInfoList = dbManager.getAllMusicFromMusicTable();
        Logger.d("zt", "---扫描出的歌曲数：----" + musicInfoList.size());
        if (musicInfoList != null && musicInfoList.size() == 0) {
            if (dialog!=null && dialog.isShowing()){
                dialog.dismiss();
            }
            USBStatusDialog.getInstance(mContext, getString(R.string.no_music_usb)).show();
            if (mainStateInfo!=null) {
                if (mainStateInfo.mConnectedState==MainStateInfo.BT_NO_AND_USB_NO||mainStateInfo.mConnectedState==MainStateInfo.BT_NO_AND_USB_YES){
                    finishThisFragment();
                    getActivity().finish();
                }

            } else {
                if (changFragmentListner != null) {
                    changFragmentListner.changeFragment(1);
                }
            }
        } else {
            if (mMediaPlayerHelper != null) {
                mMediaPlayerHelper.setPlayeData(musicInfoList);
                mMediaPlayerHelper.setPlayID(0);
            }

        }
        refreshItem();

    }

    public void startScanLocalMusic(final String path) {
        dbManager = DBManager.getInstance(MyApplication.getContext());
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
        String[] muiscInfoArray = new String[]{
                MediaStore.Audio.Media.TITLE,               //歌曲名称
                MediaStore.Audio.Media.ARTIST,              //歌曲歌手
                MediaStore.Audio.Media.ALBUM,               //歌曲的专辑名
                MediaStore.Audio.Media.DURATION,            //歌曲时长
                MediaStore.Audio.Media.DATA};               //歌曲文件的全路径
        Cursor cursor = MyApplication.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, muiscInfoArray,
                MediaStore.Audio.Media.DATA + " like ?",
                new String[]{filepath + "%"},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.getCount() != 0) {
            musicInfoList = new ArrayList<MusicInfo>();
            Log.i(TAG, "run: cursor.getCount() = " + cursor.getCount());
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Log.d("zt", "------" + path + "----");
                File file = new File(path);
                String parentPath = file.getParentFile().getPath();

                name = replaseUnKnowe(name);
                singer = replaseUnKnowe(singer);
                album = replaseUnKnowe(album);
                path = replaseUnKnowe(path);

                MusicInfo musicInfo = new MusicInfo();

                musicInfo.setName(name);
                musicInfo.setSinger(singer);
                musicInfo.setAlbum(album);
                musicInfo.setPath(path);
                Log.e(TAG, "run: parentPath = " + parentPath);
                musicInfo.setParentPath(parentPath);
                musicInfo.setFirstLetter(ChineseToEnglish.StringToPinyinSpecial(name).toUpperCase().charAt(0) + "");
                musicInfoList.add(musicInfo);

            }

            //扫描完成获取一下当前播放音乐及路径
//            curMusicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
//            curMusicPath = dbManager.getMusicPath(curMusicId);

            // 根据a-z进行排序源数据
            Collections.sort(musicInfoList);
            dbManager.updateAllMusic(musicInfoList);
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
        Intent startIntent = new Intent(mContext, AudioPlayerService.class);
        mContext.startService(startIntent);
    }

    private void initView(View view) {
        btn_play = view.findViewById(R.id.button_play);
        btn_play.setOnClickListener(this);

        btn_model = view.findViewById(R.id.button_mix_play);
        btn_model.setOnClickListener(this);
        int modelIndex = SPUtils.getInstance(mContext).getInt("model", 0);
        refreshModelBtn(modelIndex);
        btn_previous = view.findViewById(R.id.button_previous);
        btn_previous.setOnClickListener(this);

        btn_next = view.findViewById(R.id.button_next);
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
        mLyricView.setOnPlayerClickListener(this);

        mHolderSongAlbum = view.findViewById(R.id.holder_song_album);
        mHolderSongTitle = view.findViewById(R.id.holder_song_title);
        mHolderSongOrder = view.findViewById(R.id.holder_song_order);
        mHolderSongArtist = view.findViewById(R.id.holder_song_artist);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_play:
                //todo 播放或者暂停
                if (mMediaController == null) {
                    return;
                }
                if (mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mMediaController.getTransportControls().pause();
                } else if (mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                    mMediaController.getTransportControls().play();
                } else {
                    mMediaController.getTransportControls().playFromSearch("", null);
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
                if (mMediaController != null) {
                    mMediaController.getTransportControls().skipToPrevious();
                }

                break;
            case R.id.button_next:
                //todo 下一首
                if (mMediaController != null) {
                    mMediaController.getTransportControls().skipToNext();
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
        final int maxV = musicTime;
        final int timeToEnd = (int) ((maxV - progress) * 1000 / 1);
        Logger.d("zt", "onPlaybackStateChanged progress:" + progress + ",max:" + maxV + ",");
        if (timeToEnd <= 0) {
            Logger.e(TAG, "onPlaybackStateChanged return for timeToEnd is negative");
            return;
        }
        if (mProgressAnimator == null) {

            mProgressAnimator = ValueAnimator.ofInt(progress * 1000, maxV * 1000)
                    .setDuration(timeToEnd);
            mProgressAnimator.setInterpolator(new LinearInterpolator());
            mProgressAnimator.addUpdateListener(UsbFragment.this);
            mProgressAnimator.start();
        }
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

    private boolean mIsTracking = false;

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsTracking = true;

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsTracking = false;
        //更新拖动进度
        mMediaPlayerHelper.getMediaPlayer().seekTo(
                seekBar.getProgress() * mMediaPlayerHelper.getMediaPlayer().getDuration()
                        / seekBar.getMax());
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        if (iBinder instanceof AudioPlayerService.ServiceBinder) {
            try {
                //获取服务
                musicService = ((AudioPlayerService.ServiceBinder) iBinder).getService();
                //获取帮助类
                mMediaPlayerHelper = musicService.getMediaPlayerHelper();
                //设置媒体播放回键听
                mMediaPlayerHelper.setMediaPlayerUpdateListener(this);
                //设置数据源
                mMediaPlayerHelper.setPlayeData(musicInfoList);
                //设置更新的seekBaar
                mMediaPlayerHelper.setSeekBar(seekBar);
                //设置媒体控制器
                mMediaController = new MediaControllerCompat(mContext,
                        musicService.getMediaSessionToken());
                //注册回调
                mMediaController.registerCallback(mMediaControllerCallback);
            } catch (Exception e) {
                Log.e(getClass().getName(), "serviceConnectedException==" + e.getMessage());
            }
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //自动播放下一首
        int playModel = SPUtils.getInstance(mContext).getInt("model", 0);
        if (playModel == 0) {
            mMediaController.getTransportControls().skipToNext();
        } else if (playModel == 1) {
            int currentId = MyMusicUtil.getIntShared(Constant.KEY_ID);
            mMediaPlayerHelper.setPlayID(currentId - 1);
        } else {
            SecureRandom sr = new SecureRandom();
            int nextId = sr.nextInt(musicInfoList.size());
            mMediaPlayerHelper.setPlayID(nextId);
        }

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        //设置二级缓冲显示位置。
        seekBar.setSecondaryProgress(percent);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        musicTime = mediaPlayer.getDuration() / 1000;
        int minute = musicTime / 60;
        int second = musicTime % 60;
        song_duration_time.setText(minute + ":" + (second > 9 ? second : "0" + second));
        if (mProgressAnimator != null) {
            mProgressAnimator.removeUpdateListener(UsbFragment.this);
            mProgressAnimator.cancel();
            mProgressAnimator = null;
        }
    }

    @Override
    public void onPlayerClicked(long progress, String content) {
        if (mMediaController != null) {
            mMediaController.getTransportControls().seekTo(progress);
        }
    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (mLyricView != null) {
            mLyricView.setCurrentTimeMillis((int) animation.getAnimatedValue());
        }
        if (mIsTracking && mProgressAnimator != null) {
            animation.cancel();
            return;
        }
    }
}
