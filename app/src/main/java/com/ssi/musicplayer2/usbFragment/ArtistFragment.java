package com.ssi.musicplayer2.usbFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.adapter.ArtistAdapter;
import com.ssi.musicplayer2.adapter.SingleAdapter;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.intf.OnCommonAdapterItemClick;
import com.ssi.musicplayer2.javabean.MusicInfo;
import com.ssi.musicplayer2.javabean.SingerInfo;
import com.ssi.musicplayer2.service.MessageEvent;
import com.ssi.musicplayer2.utils.MyMusicUtil;
import com.ssi.musicplayer2.view.MusicLibraryRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ArtistFragment extends Fragment {
    private DBManager dbManager;
    private View view;
    private MusicLibraryRecyclerView recyclerView;
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;
    private ArtistAdapter adapter;
    private List<SingerInfo> singerInfoList = new ArrayList<>();
    private TextView tv_titlle;
    private ImageView iv_back;
    private SingleAdapter singleAdapter;
    private List<SingerInfo> dbList;
    private int currentItem=-1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_single,container,false);
        dbManager = DBManager.getInstance(getContext());
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext=context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        tv_titlle = view.findViewById(R.id.tv_title);
        tv_titlle.setText("艺术家");
        iv_back = view.findViewById(R.id.iv_back);
        iv_back.setVisibility(View.INVISIBLE);
        recyclerView = view.findViewById(R.id.single_list);
        linearLayoutManager = new LinearLayoutManager(mContext);
        adapter = new ArtistAdapter(mContext,singerInfoList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new ArtistAdapter.OnItemClickListener() {



            @Override
            public void onContentClick(View content, int position) {
                String name=singerInfoList.get(position).getName();
                List<MusicInfo> listBySinger= dbManager.getMusicListBySinger(name);
                adapter.update(null);
                singleAdapter = new SingleAdapter(mContext,listBySinger,"singer");
                recyclerView.setAdapter(singleAdapter);
                tv_titlle.setVisibility(View.GONE);
                iv_back.setVisibility(View.VISIBLE);
                singleAdapter.setOnCommonAdapterItemClick(new OnCommonAdapterItemClick() {
                    @Override
                    public void onItemClickListener(View v, int pos, String type) {
                        if (type.equals("singer")) {
                            MessageEvent event = new MessageEvent();
                            event.setType(type);
                            event.setMusicInfoList(listBySinger);
                            event.setPos(pos);
                            EventBus.getDefault().post(event);
                        }
                    }
                });
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setAdapter(adapter);
                adapter.update(dbList);
                tv_titlle.setVisibility(View.VISIBLE);
                iv_back.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        singerInfoList.clear();
        dbList = MyMusicUtil.groupBySinger((ArrayList) dbManager.getAllMusicFromMusicTable());
        singerInfoList.addAll(dbList);
        adapter.notifyDataSetChanged();
    }

    public void refreshItem() {
        if (singleAdapter!=null){
            singleAdapter.notifyDataSetChanged();
        }

    }
}
