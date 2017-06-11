package com.hoangthien.hackernews.ui.videolist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.RecyclerBaseAdapter;
import com.hoangthien.hackernews.data.model.Comment;

import java.util.List;

/**
 * thienhd
 */
public class VideoListAdapter extends RecyclerBaseAdapter {

    private List<Comment> mItems;

    public VideoListAdapter(List<Comment> items) {
        mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        v.setOnClickListener(mCustomEvent1);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int i) {
        if (vh instanceof VideoViewHolder) {
            VideoViewHolder viewHolder = (VideoViewHolder) vh;
            Comment video = mItems.get(i);

//            viewHolder.title.setText(video.getTitle());
//            viewHolder.subTitle.setText(DateFormater.getDateDDMMMYYYY(video.getUploadDate()));


            viewHolder.parent.setTag(i);
        }
    }


    private class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView subTitle;
        View parent;

        public VideoViewHolder(View view) {
            super(view);
            parent = view;
            title = (TextView) view.findViewById(R.id.title);
            subTitle = (TextView) view.findViewById(R.id.sub_title);
        }
    }


}
