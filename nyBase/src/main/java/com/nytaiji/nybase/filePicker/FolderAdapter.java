package com.nytaiji.nybase.filePicker;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nytaiji.nybase.R;

import java.io.File;
import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private final ArrayList<Folder> items;
    private final ClickListener listener;

    private ThumbnailLoader thumbnailLoader;

    public FolderAdapter(Context context, ArrayList<Folder> items, ClickListener listener) {
        this.items = items;
        this.listener = listener;
        thumbnailLoader = new ThumbnailLoader(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_pick_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Folder item = items.get(position);

        holder.title.setText(item.name);
        holder.size.setText(item.size);

        if (item.isBack) {
            holder.title.setTextColor(Color.RED);
            holder.title.setTextSize(16);
        } else {
            holder.title.setTextColor(Color.BLUE);
            holder.title.setTextSize(14);
        }
        if (item.isDirectory) {
            holder.icon.setImageResource(item.isBack ? R.drawable.back_folder : R.drawable.ic_folder);

            // item.isBack=false;
        } else {
            FileInfo info = new FileInfo(new File(item.path));
            if (info.isVideo()) holder.icon.setImageResource(R.drawable.ic_video);
            else if (info.isAudio()) holder.icon.setImageResource(R.drawable.ic_audio);
            else if (info.isPdf()) holder.icon.setImageResource(R.drawable.ic_pdf);
            else if (info.isImage()) thumbnailLoader.load(info, holder.icon);
            else holder.icon.setImageResource(R.drawable.ic_file);
        }

        holder.holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View holder;
        public TextView title;
        public TextView size;
        public ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            holder = itemView.findViewById(R.id.holder);
            title = (TextView) itemView.findViewById(R.id.item_name);
            size = (TextView) itemView.findViewById(R.id.item_size);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }

    public interface ClickListener {
        void onClick(Folder item);
    }
}
