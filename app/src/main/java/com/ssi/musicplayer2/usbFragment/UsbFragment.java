package com.ssi.musicplayer2.usbFragment;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.constraintlayout.widget.Placeholder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.dfssi.android.d760ui.TipsDialog;
import com.mediabrowser.xiaxl.client.MusicManager;
import com.mediabrowser.xiaxl.client.listener.OnSaveRecordListener;
import com.mediabrowser.xiaxl.client.utils.MusicMetadataConstant;
import com.mediabrowser.xiaxl.client.utils.SPUtils;
import com.ssi.musicplayer2.MyApplication;
import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.btFragment.SubFragment;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.manager.MainStateInfo;
import com.ssi.musicplayer2.manager.MainStateInfoViewModel;
import com.ssi.musicplayer2.service.MessageEvent;
import com.ssi.musicplayer2.utils.ChineseToEnglish;
import com.ssi.musicplayer2.utils.Constant;
import com.ssi.musicplayer2.utils.Logger;
import com.ssi.musicplayer2.utils.MyMusicUtil;
import com.ssi.musicplayer2.utils.Utils;
import com.ssi.musicplayer2.view.LyricView;
import com.ssi.musicplayer2.view.MediaSeekBar;
import com.ssi.musicplayer2.view.USBStatusDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UsbFragment extends SubFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, ValueAnimator.AnimatorUpdateListener, Observer<Object> {

    private static final String TAG = UsbFragment.class.getSimpleName();
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
    private ImageView iv_song_img;
    private ImageView iv_song_img_film;
    private MusicManager mMusicManager;

    private static final ComponentName MEDIA_SERVICE = new ComponentName("com.ssi.musicplayer2",
            "com.mediabrowser.xiaxl.service.MusicService");
    private MainStateInfoViewModel mMainStateInfoViewModel;
    private getPCMFileAsyncTask asyncTask;
    private boolean scanPcm;
    private List<String> pcmList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMusicAgent();
        initstate();
        EventBus.getDefault().register(this);
    }

    private void initstate() {
        mMainStateInfoViewModel = MainStateInfoViewModel.getInstance();
        mMainStateInfoViewModel.addMainStateInfoListener(this, this);
        mMainStateInfoViewModel.addScanStateListener(this, this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
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
        Log.d("zt", "--onDestroyView----");
        if (mMusicManager != null) {
            mMusicManager.removeAudioStateListener(mAudioStatusChangeListener);
            mMusicManager.stop();
        }
        if (mMainStateInfoViewModel != null) {
            mMainStateInfoViewModel.removeMainStateInfoListener(this);
            mMainStateInfoViewModel.removeScanStateListener(this);
            mMainStateInfoViewModel = null;
        }
        Intent intent = new Intent();
        intent.setComponent(MEDIA_SERVICE);
        mContext.stopService(intent);

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
            if (mMusicManager != null) {
                mMusicManager.playMusicList(mMusicInfos, pos);
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
        switchLyicState(View.INVISIBLE);
        cancelLirViewListener();
        seekBar.stopProgressAnima();
        seekBar.setProgress(0);
        frameLayout.setVisibility(View.GONE);
        holdView.setVisibility(View.GONE);
        mLyricGroup.setVisibility(View.GONE);
        mLyricView.setVisibility(View.GONE);
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
                    scanData = true;
                    scanComplete();
                    break;
                case Constant.SCAN_PCM_COMPLETE:
                    pcmList.clear();
                    pcmList = (List<String>) msg.obj;
                    scanPcm = true;
                    scanComplete();
                    break;
            }
        }
    };


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (mLyricView != null) {
            mLyricView.setCurrentTimeMillis((int) animation.getAnimatedValue());
        }


    }

    @Override
    public void onChanged(Object o) {
        if (o instanceof MainStateInfo) {
            mainStateInfo = (MainStateInfo) o;
            Log.d("zt", "---状态改变了--");
            if (mainStateInfo.mConnectedState == 1 || mainStateInfo.mConnectedState == 3) {
                if (mainStateInfo.mScanState == MainStateInfo.SCAN_STATE_END) {
                    final List<String> path = Utils.getUSBPaths(mContext);
                    if (path != null && path.size() > 0) {
                        if (mainStateInfo.isBtConnectChange) {
                            return;
                        }
                        Log.d("zt", "---开始扫描--");
                        showDialog(getString(R.string.text_scaning));
                        asyncTask = new getPCMFileAsyncTask();
                        asyncTask.execute(path.get(0));
                        startScanLocalMusic(path.get(0));
                    }
                }

            } else {
                resetFragment();
            }
        }
    }

    //在异步方法中 调用
    private class getPCMFileAsyncTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<String> doInBackground(String... params) {
            return getPCMFile(params[0]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            asyncTask.cancel(true);
        }

        @Override
        protected void onPostExecute(List<String> dataList) {
            Log.d("zt", "---onPostExecute---" + dataList.size());
            msg = new Message();
            msg.what = Constant.SCAN_PCM_COMPLETE;
            msg.obj = dataList;
            handler.sendMessage(msg);  //更新UI界面

        }
    }

    public interface ChangFragmentListner {
        void changeFragment(int position);
    }


    private ChangFragmentListner changFragmentListner;

    private void scanComplete() {
        Log.d("zt", "---scanComplete---" + scanData+"----"+scanPcm);
        if (scanData && scanPcm) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.d("zt", "---pcm文件---" + pcmList.size());
            if (pcmList.size() > 0) {
                for (int i = 0; i < pcmList.size(); i++) {
                    MusicInfoBean musicInfo = new MusicInfoBean();
                    String path = pcmList.get(i);
                    Log.d("zt", "---pcm文件路径---" + path);
                    File file = new File(path);
                    musicInfo.setTitle(file.getName());
                    musicInfo.setSinger("未知");
                    musicInfo.setAlbum("未知");
                    musicInfo.setPath(path);
                    musicInfo.setMediaId(String.valueOf( i+ 1000));
                    long duration = Utils.getFileDuration(path);
//                    if (duration==0){
//                        continue;
//                    }
                    Log.d("zt", "---时长---" + duration);
                    musicInfo.setDuration(duration);
                    String parentPath = file.getParentFile().getPath();
                    musicInfo.setParentPath(parentPath);
                    musicInfo.setFirstLetter(ChineseToEnglish.StringToPinyinSpecial(file.getName()).toUpperCase().charAt(0) + "");
                   mMusicInfos.add(musicInfo);
                }
            }
            dbManager.updateAllMusic(mMusicInfos);
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
                        Log.d("zt", "第一个播放的歌曲id" + mMusicInfos.get(0).getMediaId());
                    }
                }


            }
            refreshItem();
        }

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

    private List<String> getPCMFile(String path) {
        List<String> files = new ArrayList<>();
        SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File myFile = file.toFile();
                String name = myFile.getName().toLowerCase();
                if (name.endsWith("pcm") || name.endsWith("adpcm")) {
                    files.add(myFile.getAbsolutePath());
                    Log.d("zt", "---getImageFile---" + files.size());
                }
                return super.visitFile(file, attrs);
            }
        };

        try {
            java.nio.file.Files.walkFileTree(Paths.get(path), finder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;

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
            }
            // 根据id进行排序源数据
            Collections.sort(mMusicInfos);
            dbManager.updateAllMusic(mMusicInfos);
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
                if (mMusicManager == null) {
                    return;
                }
                if (mIsPlaying) {
                    mMusicManager.pause();
                } else {
                    mMusicManager.play();
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
                if (mMusicManager != null) {
                    mMusicManager.skipToPrevious();
                }
                break;
            case R.id.button_next:
                //todo 下一首
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
        song_progress_time.setText(Utils.formatTime(progress));

    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mMusicManager != null) {
            if (!mIsPlaying) {
                mMusicManager.play();
            }
            mMusicManager.seekTo(seekBar.getProgress());
        }
    }

    /**
     * 数据
     */
    // 是否正在播放的标识
    private boolean mIsPlaying;

    private void refreshPlayState(boolean mIsPlaying) {
        if (btn_play == null) {
            return;
        }
        if (mIsPlaying) {
            btn_play.setBackgroundResource(R.drawable.img_pause);
        } else {
            btn_play.setBackgroundResource(R.drawable.img_play);
        }
    }

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
        mIsPlaying = playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        Log.d("zt", "-----" + mIsPlaying);
        // 更新UI
        refreshPlayState(mIsPlaying);

        /**
         * 设置播放进度
         */
        cancelLirViewListener();

        final int progress = (int) playbackState.getPosition();
        if (seekBar == null) {
            return;
        }
        seekBar.setProgress(progress);
        song_progress_time.setText(Utils.formatTime(progress));
        switch (playbackState.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                final int timeToEnd = (int) ((seekBar.getMax() - progress) / playbackState.getPlaybackSpeed());
                if (timeToEnd <= 0) {
                    Logger.e(TAG, "onPlaybackStateChanged return for timeToEnd is negative");
                    return;
                }
                seekBar.startProgressAnima(progress, seekBar.getMax(), timeToEnd);
                if (mProgressAnimator == null) {
                    mProgressAnimator = ValueAnimator.ofInt(progress, seekBar.getMax())
                            .setDuration(timeToEnd);
                    mProgressAnimator.setInterpolator(new LinearInterpolator());
                    mProgressAnimator.addUpdateListener(UsbFragment.this);
                    mProgressAnimator.start();
                }
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                seekBar.stopProgressAnima();
                cancelLirViewListener();
                break;

        }

    }

    private void cancelLirViewListener() {
        if (mProgressAnimator != null) {
            mProgressAnimator.removeUpdateListener(UsbFragment.this);
            mProgressAnimator.cancel();
            mProgressAnimator = null;
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
        if (song_title == null) {
            return;
        }
        song_title.setText(
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        // 音频作者

        song_artist.setText(
                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        song_album.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));

        String id = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        for (int i=0;i<mMusicInfos.size();i++){
            if (mMusicInfos.get(i).getMediaId().equals(id)){
                int currentPos=i;
                song_order.setText("（"+(currentPos+1)+"/"+mMusicInfos.size()+")");
                break;
            }

        }
        MyMusicUtil.setShared(Constant.KEY_ID, id);
        Log.d("zt", "----当前播放的歌曲id--" + id);
        refreshItem();
        // 音频图片
//        mAlbumArtImg.setImageBitmap(MusicLibrary.getAlbumBitmap(
//                MainActivity.this,
//                mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));

        String path = mediaMetadata.getString(MusicMetadataConstant.CUSTOM_METADATA_TRACK_SOURCE);
        File lyricFile = new File(path.substring(0, path.lastIndexOf(".")) + ".lrc");
        if (lyricFile.exists()) {
            mLyricView.reset();
            mLyricView.setLyricFile(lyricFile, Utils.getCharset(lyricFile.getName()));
        } else {
            mLyricView.reset();
        }
        // 进度条
        final int max = mediaMetadata != null
                ? (int) mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                : 0;
        song_duration_time.setText(Utils.formatTime(max));
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

        @Override
        public void onComplete() {
            Log.d("zt", "--播放完毕--" + seekBar.getProgress());
        }
    };


}
