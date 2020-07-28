package com.ssi.musicplayer2.usbFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.adapter.FolderAdapter;
import com.ssi.musicplayer2.adapter.SingleAdapter;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.javabean.FolderInfo;
import com.ssi.musicplayer2.utils.MyMusicUtil;
import com.ssi.musicplayer2.view.MusicLibraryRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends Fragment {

    private View view;
    private MusicLibraryRecyclerView recyclerView;
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;
    private FolderAdapter adapter;
    private List<FolderInfo> folderInfoList = new ArrayList<>();
    private DBManager dbManager;

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
        RelativeLayout rl_title = view.findViewById(R.id.ll_lable);
        rl_title.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.single_list);
        linearLayoutManager = new LinearLayoutManager(mContext);
        adapter = new FolderAdapter(mContext,folderInfoList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        folderInfoList.clear();
        folderInfoList.addAll(MyMusicUtil.groupByFolder((ArrayList)dbManager.getAllMusicFromMusicTable()));
        Log.d("zt", "onResume: folderInfoList.size() = "+folderInfoList.size());
        adapter.notifyDataSetChanged();
    }
}
