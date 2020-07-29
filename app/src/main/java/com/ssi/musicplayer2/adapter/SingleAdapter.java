package com.ssi.musicplayer2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.intf.OnCommonAdapterItemClick;
import com.ssi.musicplayer2.javabean.MusicInfo;

import java.util.List;

public class SingleAdapter extends RecyclerView.Adapter<SingleAdapter.ViewHolder> {

    private Context context;
    private List<MusicInfo> musicInfoList;
    private LayoutInflater layoutInflater;
    private OnCommonAdapterItemClick onCommonAdapterItemClick;
    private int currentPos=-1;

    public void setOnCommonAdapterItemClick(OnCommonAdapterItemClick onCommonAdapterItemClick) {
        this.onCommonAdapterItemClick = onCommonAdapterItemClick;
    }

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
        if (currentPos==position){
            holder.textView.setTextColor(context.getColor(R.color.context_text_color));
            holder.imageView.setVisibility(View.VISIBLE);
        }else {
            holder.textView.setTextColor(context.getColor(R.color.normal_text_color));
            holder.imageView.setVisibility(View.INVISIBLE);
        }
        holder.ll_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCommonAdapterItemClick!=null){
                    updateSelectItem(position);
                    onCommonAdapterItemClick.onItemClickListener(v,position);
                }
            }
        });


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

    public void updateSelectItem(int pos) {
        currentPos=pos;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView textView;
        private final LinearLayout ll_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_state);
            textView = itemView.findViewById(R.id.tv_name);
            ll_item = itemView.findViewById(R.id.ll_item);
        }
    }


}
