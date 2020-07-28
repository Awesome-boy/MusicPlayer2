package com.ssi.musicplayer2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ssi.musicplayer2.R;
import com.ssi.musicplayer2.database.DBManager;
import com.ssi.musicplayer2.javabean.SingerInfo;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {
    private static final String TAG = ArtistAdapter.class.getName();
    private List<SingerInfo> singerInfoList;
    private Context context;
    private DBManager dbManager;
    private OnItemClickListener onItemClickListener;

    public ArtistAdapter(Context context, List<SingerInfo> singerInfoList) {
        this.context = context;
        this.singerInfoList = singerInfoList;
        this.dbManager = DBManager.getInstance(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout contentLl;
        ImageView singerIv;
        TextView singelName;
        TextView count;

        public ViewHolder(View itemView) {
            super(itemView);
            this.contentLl = itemView.findViewById(R.id.model_music_item_ll);
            this.singerIv = itemView.findViewById(R.id.model_head_iv);
            this.singelName = itemView.findViewById(R.id.model_item_name);
            this.count = itemView.findViewById(R.id.model_music_count);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_model_rv_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d("aaaa", "onBindViewHolder: position = " + position);
        if (singerInfoList==null||singerInfoList.size()==0){
            return;
        }
        SingerInfo singer = singerInfoList.get(position);
        holder.singelName.setText(position + 1 + "、" + singer.getName());
        holder.singerIv.setVisibility(View.INVISIBLE);
        holder.count.setText(singer.getCount() + "首");
        holder.count.setVisibility(View.GONE);
        holder.contentLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener!=null){
                    onItemClickListener.onContentClick(holder.contentLl, position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return singerInfoList==null?0:singerInfoList.size();
    }

    public void update(List<SingerInfo> singerInfoList) {
        this.singerInfoList.clear();
        if (singerInfoList!=null){
            this.singerInfoList.addAll(singerInfoList);
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onContentClick(View content, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
