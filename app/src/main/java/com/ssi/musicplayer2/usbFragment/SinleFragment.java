package com.ssi.musicplayer2.usbFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.adapter.SingleAdapter;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.javabean.MusicInfo;
import com.ssi.musicplayer2.view.MusicLibraryRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SinleFragment extends Fragment {


    private View view;
    private MusicLibraryRecyclerView recyclerView;
    private Context mContext;
    private List<MusicInfo> musicInfoList = new ArrayList<>();
    private DBManager dbManager;
    private SingleAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext=context;
        dbManager = DBManager.getInstance(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        musicInfoList = dbManager.getAllMusicFromMusicTable();
        Collections.sort(musicInfoList);
        adapter.updateMusicInfoList(musicInfoList);
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
        recyclerView = view.findViewById(R.id.single_list);
        linearLayoutManager = new LinearLayoutManager(mContext);
        adapter = new SingleAdapter(mContext,musicInfoList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        Log.d("zt","sss----"+musicInfoList.size());
    }
}
