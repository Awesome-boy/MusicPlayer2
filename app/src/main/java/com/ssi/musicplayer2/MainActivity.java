package com.ssi.musicplayer2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.dfssi.android.framework.ui.view.TableIndexView;
import com.ssi.musicplayer2.adapter.MainActivityFragmentAdapter;
import com.ssi.musicplayer2.btFragment.BluetoothMainFragment;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.manager.MainStateInfo;
import com.ssi.musicplayer2.manager.MainStateInfoViewModel;
import com.ssi.musicplayer2.usbFragment.UsbFragment;
import com.ssi.musicplayer2.utils.Logger;
import com.ssi.musicplayer2.utils.SPUtils;
import com.ssi.musicplayer2.view.USBStatusDialog;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener, UsbFragment.ChangFragmentListner, Observer<Object> {

    private TextView tv_usb_title;
    private TextView tv_bt_title;
    private ViewPager viewPager;
    private List<Fragment> fragmentList;
    private MainActivityFragmentAdapter adapter;
    private ImageView iv_usbState;
    private ImageView iv_btState;
    private int currentPos = 0;
    private Button btn_back;
    private ImageView music_lyric;
    private ImageView music_dir;
    private ImageView music_artist;
    private ImageView music_album;
    private boolean btConnect;
    private boolean usbConnect;
    private static final String[] permissionGroup =
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS
            };
    private TableIndexView mTableIndexView;
    private boolean usbstate;
    private boolean btstate;
    private MainStateInfoViewModel mMainStateInfoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (isNeedPermission()) {
            requestPermissions(permissionGroup, 1);
        }
        mMainStateInfoViewModel = MainStateInfoViewModel.getInstance();
        mMainStateInfoViewModel.addMainStateInfoListener(this, this);
        mMainStateInfoViewModel.addScanStateListener(this, this);
        EventBus.getDefault().register(this);
        switchState(mMainStateInfoViewModel.getMainStateInfo());
    }


    private boolean isNeedPermission() {
        boolean isGet = false;
        for (int i = 0; i < permissionGroup.length; i++) {
            isGet = checkSelfPermission(permissionGroup[i]) == PackageManager.PERMISSION_GRANTED;
            if (!isGet) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void initView() {
        btn_back = findViewById(R.id.bt_back);
        btn_back.setOnClickListener(this);
        iv_usbState = findViewById(R.id.img_usb_music_state);
        iv_btState = findViewById(R.id.img_bt_music_state);
        tv_usb_title = findViewById(R.id.usb_pager_title);
        tv_bt_title = findViewById(R.id.bt_pager_title);
        viewPager = findViewById(R.id.viewpager);
        fragmentList = new ArrayList<>();
        fragmentList.add(new UsbFragment());
        fragmentList.add(new BluetoothMainFragment());
        adapter = new MainActivityFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(0, true);
        mTableIndexView = findViewById(R.id.index_view);
        mTableIndexView.setTableIndex(0, 2);
        tv_usb_title.setSelected(true);
        music_lyric = findViewById(R.id.music_lyric);
        music_dir = findViewById(R.id.music_dir);
        music_artist = findViewById(R.id.music_artist);
        music_album = findViewById(R.id.music_album);
        tv_usb_title.setOnClickListener(this);
        tv_bt_title.setOnClickListener(this);
        findViewById(R.id.empty_view).setVisibility(View.GONE);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentPos = position;
        tv_usb_title.setSelected(position == 0);
        tv_bt_title.setSelected(position == 1);
        iv_usbState.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
        iv_btState.setVisibility(position == 1 ? View.VISIBLE : View.INVISIBLE);
        music_lyric.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
        music_dir.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
        music_artist.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
        music_album.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
        mTableIndexView.setTableIndex(position, 2);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                moveTaskToBack(true);
                break;
            case R.id.usb_pager_title:
                viewPager.setCurrentItem(0, true);
                break;
            case R.id.bt_pager_title:
                viewPager.setCurrentItem(1, true);
                break;
        }
    }

    private void switchState(MainStateInfo mainStateInfo) {
        if (mainStateInfo == null) {
            return;
        }
        if (mainStateInfo.mConnectedState == 0) {
            btstate = false;
            usbstate = false;
        } else if (mainStateInfo.mConnectedState == 1) {
            btstate = false;
            usbstate = true;
        } else if (mainStateInfo.mConnectedState == 2) {
            btstate = true;
            usbstate = false;
        } else if (mainStateInfo.mConnectedState == 3) {
            btstate = true;
            usbstate = true;
        }
        if (!btstate){
         boolean lastBtState=SPUtils.getInstance(this).getBoolean("bt",false);
         if (lastBtState){
             USBStatusDialog dialog = USBStatusDialog.getInstance(this, getString(R.string.bt_disconnect));
             dialog.show();
         }
        }
        SPUtils.getInstance(this).save("bt",btstate);
        changeFragment(usbstate, btstate);
        EventBus.getDefault().post(mainStateInfo);
        Log.d("zt", "usb连接状态-----" + usbstate + "--蓝牙连接状态---" + btstate);

    }

    private void changeFragment(boolean usb, boolean bluetooth) {
        if (!usb) {
            DBManager.getInstance(this).deleteAllTable();
        }
        if (!usb && bluetooth) {
            viewPager.setCurrentItem(1, true);
        } else if ((usb && !bluetooth) || (bluetooth && usb)) {
            viewPager.setCurrentItem(0, true);
        } else if (!bluetooth && !usb) {
            USBStatusDialog dialog = USBStatusDialog.getInstance(this, getString(R.string.tips_no_usb));
            dialog.show();
            finish();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(Object o) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMainStateInfoViewModel != null) {
            mMainStateInfoViewModel.removeMainStateInfoListener(this);
            mMainStateInfoViewModel.removeScanStateListener(this);
            mMainStateInfoViewModel = null;
        }
        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(this);
            viewPager.clearOnPageChangeListeners();
            viewPager = null;
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void changeFragment(int position) {
        viewPager.setCurrentItem(1, true);
    }

    @Override
    public void onChanged(Object o) {
        if (o instanceof MainStateInfo) {
            MainStateInfo mainStateInfo = (MainStateInfo) o;
            switchState(mainStateInfo);
        }
    }
}