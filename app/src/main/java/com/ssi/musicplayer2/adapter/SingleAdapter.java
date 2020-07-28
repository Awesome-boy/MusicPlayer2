package com.ssi.musicplayer2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.javabean.MusicInfo;

import java.util.List;

public class SingleAdapter extends RecyclerView.Adapter<SingleAdapter.ViewHolder> {

    private Context context;
    private List<MusicInfo> musicInfoList;
    private LayoutInflater layoutInflater;
    public SingleAdapter(Context mContext, List<MusicInfo> musicInfoList) {
        this.context=mContext;
        this.musicInfoList=musicInfoList;
        layoutInflater=LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=layoutInflater.inflate(R.layout.single_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MusicInfo info=musicInfoList.get(position);
        holder.textView.setText(position+1+"、"+info.getName());

    }

    @Override
    public int getItemCount() {
        return musicInfoList==null?0:musicInfoList.size();
    }

    public void updateMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList.clear();
        this.musicInfoList.addAll(musicInfoList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_state);
            textView = itemView.findViewById(R.id.tv_name);
        }
    }
}
