package com.ssi.musicplayer2.usbFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.adapter.SingleAdapter;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.intf.OnCommonAdapterItemClick;
import com.ssi.musicplayer2.javabean.MusicInfo;
import com.ssi.musicplayer2.service.MessageEvent;
import com.ssi.musicplayer2.view.MusicLibraryRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SinleFragment extends Fragment  {


    private View view;
    private MusicLibraryRecyclerView recyclerView;
    private Context mContext;
    private List<MusicInfo> musicInfoList = new ArrayList<>();
    private DBManager dbManager;
    private SingleAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private int currentItem=-1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext=context;
        dbManager = DBManager.getInstance(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        musicInfoList = dbManager.getAllMusicFromMusicTable();
        Collections.sort(musicInfoList);
        adapter.updateMusicInfoList(musicInfoList);
        if (currentItem!=-1){
            adapter.updateSelectItem(currentItem);
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(MessageEvent messageEvent) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_single,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        RelativeLayout rl_title = view.findViewById(R.id.ll_lable);
        rl_title.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.single_list);
        linearLayoutManager = new LinearLayoutManager(mContext);
        adapter = new SingleAdapter(mContext,musicInfoList,"single");
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnCommonAdapterItemClick(new OnCommonAdapterItemClick() {
            @Override
            public void onItemClickListener(View v, int pos,String type) {
                if (type.equals("single")){
                    MessageEvent event=new MessageEvent();
                    event.setType(type);
                    event.setMusicInfoList(musicInfoList);
                    event.setPos(pos);
                    EventBus.getDefault().post(event);
                }

            }
        });

    }


    public void refreshItem() {
        if (adapter!=null){
            Log.d("zt","adpter"+"----");
            adapter.notifyDataSetChanged();
        }

    }
}
