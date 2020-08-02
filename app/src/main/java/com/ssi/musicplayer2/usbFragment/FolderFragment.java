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
import com.ssi.musicplayer2.database.DBNewManager;
import com.ssi.musicplayer2.intf.OnCommonAdapterItemClick;
import com.ssi.musicplayer2.javabean.FolderInfo;
import com.ssi.musicplayer2.javabean.MusicInfo;
import com.ssi.musicplayer2.service.MessageEvent;
import com.ssi.musicplayer2.utils.MyMusicUtil;
import com.ssi.musicplayer2.view.MusicLibraryRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends Fragment {

    private View view;
    private MusicLibraryRecyclerView recyclerView;
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;
    private FolderAdapter adapter;
    private List<FolderInfo> folderInfoList = new ArrayList<>();
    private DBNewManager dbManager;
    private TextView tv_titlle;
    private ImageView iv_back;
    private ArrayList<FolderInfo> dbList;
    private RelativeLayout relativeLayout;
    private SingleAdapter singleAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_single,container,false);
        dbManager = DBNewManager.getInstance(getContext());
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext=context;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(MessageEvent messageEvent) {

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
                List<MusicInfoBean> listByFolder=dbManager.getMusicListByFolder(path);
                adapter.update(null);
                singleAdapter = new SingleAdapter(mContext,listByFolder,"folder");
                recyclerView.setAdapter(singleAdapter);
                relativeLayout.setVisibility(View.VISIBLE);
                iv_back.setVisibility(View.VISIBLE);
                singleAdapter.setOnCommonAdapterItemClick(new OnCommonAdapterItemClick() {
                    @Override
                    public void onItemClickListener(View v, int pos, String type) {
                        if (type.equals("folder")) {
                            MessageEvent event = new MessageEvent();
                            event.setType(type);
                            event.setMusicInfoList(listByFolder);
                            event.setPos(pos);
                            EventBus.getDefault().post(event);
                            Log.d("zt","-folderId--"+listByFolder.get(pos).getMediaId());
                        }
                    }
                });
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
        Log.d("zt","--folder---"+dbList.size());
        folderInfoList.addAll(dbList);
        adapter.notifyDataSetChanged();
    }

    public void refreshItem() {
        if (singleAdapter!=null){
            singleAdapter.notifyDataSetChanged();
        }

    }
}
