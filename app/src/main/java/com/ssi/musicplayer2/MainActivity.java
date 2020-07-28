package com.ssi.musicplayer2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.ssi.musicplayer2.adapter.MainActivityFragmentAdapter;
import com.ssi.musicplayer2.btFragment.BtFragment;
import com.ssi.musicplayer2.usbFragment.UsbFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private TextView tv_usb_title;
    private TextView tv_bt_title;
    private ViewPager viewPager;
    private List<Fragment> fragmentList;
    private MainActivityFragmentAdapter adapter;
    private ImageView iv_usbState;
    private ImageView iv_btState;
    private int currentPos;
    private Button btn_back;
    private ImageView music_lyric;
    private ImageView music_dir;
    private ImageView music_artist;
    private ImageView music_album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        initPermission();
        initView();

    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    checkSkip();
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
        fragmentList.add(new BtFragment());
        adapter = new MainActivityFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(0, true);
        tv_usb_title.setSelected(true);
        music_lyric = findViewById(R.id.music_lyric);
        music_dir = findViewById(R.id.music_dir);
        music_artist = findViewById(R.id.music_artist);
        music_album = findViewById(R.id.music_album);
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

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                finish();
                break;
        }
    }
}