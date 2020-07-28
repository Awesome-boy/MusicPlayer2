package com.ssi.musicplayer2.usbFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.adapter.AlbumAdapter;
import com.ssi.musicplayer2.adapter.FolderAdapter;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.javabean.AlbumInfo;
import com.ssi.musicplayer2.utils.MyMusicUtil;
import com.ssi.musicplayer2.view.MusicLibraryRecyclerView;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {

    private DBManager dbManager;
    private View view;
    private MusicLibraryRecyclerView recyclerView;
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<AlbumInfo> albumInfoList = new ArrayList<>();
    private AlbumAdapter adapter;

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
        recyclerView = view.findViewById(R.id.single_list);
        linearLayoutManager = new LinearLayoutManager(mContext);
        adapter = new AlbumAdapter(mContext,albumInfoList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        albumInfoList.clear();
        albumInfoList.addAll(MyMusicUtil.groupByAlbum((ArrayList)dbManager.getAllMusicFromMusicTable()));
        Log.d("zt", "onResume: albumInfoList.size() = "+albumInfoList.size());
        adapter.notifyDataSetChanged();
    }
}
