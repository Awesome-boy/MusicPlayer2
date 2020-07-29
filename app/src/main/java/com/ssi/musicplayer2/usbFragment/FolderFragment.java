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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.adapter.FolderAdapter;
import com.ssi.musicplayer2.adapter.SingleAdapter;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.javabean.FolderInfo;
import com.ssi.musicplayer2.javabean.MusicInfo;
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
    private TextView tv_titlle;
    private ImageView iv_back;
    private ArrayList<FolderInfo> dbList;
    private RelativeLayout relativeLayout;

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
        relativeLayout = view.findViewById(R.id.ll_lable);
        relativeLayout.setVisibility(View.GONE);
        tv_titlle = view.findViewById(R.id.tv_title);
        tv_titlle.setVisibility(View.GONE);
        iv_back = view.findViewById(R.id.iv_back);
        iv_back.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.single_list);
        linearLayoutManager = new LinearLayoutManager(mContext);
        adapter = new FolderAdapter(mContext,folderInfoList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
            @Override
            public void onContentClick(View content, int position) {
               String path= folderInfoList.get(position).getPath();
                List<MusicInfo> listByFolder=dbManager.getMusicListByFolder(path);
                adapter.update(null);
                recyclerView.setAdapter(new SingleAdapter(mContext,listByFolder,"folder"));
                relativeLayout.setVisibility(View.VISIBLE);
                iv_back.setVisibility(View.VISIBLE);
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout.setVisibility(View.GONE);
                iv_back.setVisibility(View.GONE);
                recyclerView.setAdapter(adapter);
                adapter.update(dbList);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        folderInfoList.clear();
        dbList = MyMusicUtil.groupByFolder((ArrayList) dbManager.getAllMusicFromMusicTable());
        folderInfoList.addAll(dbList);
        Log.d("zt", "onResume: folderInfoList.size() = "+folderInfoList.size());
        adapter.notifyDataSetChanged();
    }
}
