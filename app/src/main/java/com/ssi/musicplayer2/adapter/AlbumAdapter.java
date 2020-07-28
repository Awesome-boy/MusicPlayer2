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
import com.ssi.musicplayer2.javabean.AlbumInfo;

import java.util.List;

/**
 * Created by lijunyan on 2017/3/11.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>{
    
    private static final String TAG = AlbumAdapter.class.getName();
    private List<AlbumInfo> albumInfoList;
    private Context context;
    private DBManager dbManager;
    private AlbumAdapter.OnItemClickListener onItemClickListener;

    public AlbumAdapter(Context context, List<AlbumInfo> albumInfoList) {
        this.context = context;
        this.albumInfoList = albumInfoList;
        this.dbManager = DBManager.getInstance(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout contentLl;
        ImageView albumIv;
        TextView albumName;
        TextView count;

        public ViewHolder(View itemView) {
            super(itemView);
            this.contentLl = (LinearLayout) itemView.findViewById(R.id.model_music_item_ll);
            this.albumIv = (ImageView) itemView.findViewById(R.id.model_head_iv);
            this.albumName = (TextView) itemView.findViewById(R.id.model_item_name);
            this.count = (TextView) itemView.findViewById(R.id.model_music_count);
        }

    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_model_rv_item,parent,false);
        AlbumAdapter.ViewHolder viewHolder = new AlbumAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final AlbumAdapter.ViewHolder holder, final int position) {
        Log.d("aaaa", "onBindViewHolder: position = "+position);
        AlbumInfo album = albumInfoList.get(position);
        holder.albumIv.setVisibility(View.INVISIBLE);
        holder.albumName.setText(position+1+"、"+album.getName());
        holder.count.setText(album.getCount()+"首 "+album.getSinger());
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
        return albumInfoList.size();
    }

    public void update(List<AlbumInfo> albumInfoList) {
        this.albumInfoList.clear();
        if (albumInfoList!=null){
            this.albumInfoList.addAll(albumInfoList);
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener{
        void onContentClick(View content, int position);
    }

    public void setOnItemClickListener(AlbumAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener ;
    }
}
