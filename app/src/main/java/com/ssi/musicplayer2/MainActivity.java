package com.ssi.musicplayer2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.ssi.musicplayer2.adapter.MainActivityFragmentAdapter;
import com.ssi.musicplayer2.btFragment.BtFragment;
import com.ssi.musicplayer2.usbFragment.UsbFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private TextView tv_usb_title;
    private TextView tv_bt_title;
    private ViewPager viewPager;
    private List<Fragment> fragmentList;
    private MainActivityFragmentAdapter adapter;
    private ImageView iv_usbState;
    private ImageView iv_btState;
    private int currentPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        iv_usbState = findViewById(R.id.img_usb_music_state);
        iv_btState = findViewById(R.id.img_bt_music_state);
        tv_usb_title = findViewById(R.id.usb_pager_title);
        tv_bt_title = findViewById(R.id.bt_pager_title);
        viewPager = findViewById(R.id.viewpager);
        fragmentList = new ArrayList<>();
        fragmentList.add(new UsbFragment());
        fragmentList.add(new BtFragment());
        adapter = new MainActivityFragmentAdapter(getSupportFragmentManager(),fragmentList);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(0,true);
        tv_usb_title.setSelected(true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
            currentPos=position;
            tv_usb_title.setSelected(position==0);
            tv_bt_title.setSelected(position==1);
            iv_usbState.setVisibility(position==0?View.VISIBLE:View.INVISIBLE);
            iv_btState.setVisibility(position==1?View.VISIBLE:View.INVISIBLE);

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}