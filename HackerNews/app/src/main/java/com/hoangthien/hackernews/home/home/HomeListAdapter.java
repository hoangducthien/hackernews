package com.hoangthien.hackernews.home.home;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.RecyclerBaseAdapter;
import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.utils.DateFormater;

import java.util.List;

/**
 * thienhd
 */
public class HomeListAdapter extends RecyclerBaseAdapter {

    private List<News> mItems;

    public HomeListAdapter(List<News> items) {
        mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item, parent, false);
        return new HomeViewHolder(v);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int i) {
        HomeViewHolder viewHolder = (HomeViewHolder) vh;

        News news = mItems.get(i);

        viewHolder.title.setText(news.getTitle());
        Uri uri = Uri.parse(news.getUrl());
        StringBuilder s = new StringBuilder();
        s.append("(").append(uri.getHost()).append(") ");
        s.append(news.getScore()).append(" points by ");
        s.append(news.getBy());
        s.append(" ").append(DateFormater.convertToTimeAgo(System.currentTimeMillis() / 1000 - news.getTime()));
        s.append(" | ").append(news.getKids().size()).append(" comments");

        viewHolder.subTitle.setText(s.toString());

        viewHolder.parent.setTag(news);
        viewHolder.parent.setOnClickListener(mCustomEvent1);
    }


    public static class HomeViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView subTitle;
        public View parent;

        public HomeViewHolder(View view) {
            super(view);
            parent = view;
            title = (TextView) view.findViewById(R.id.title);
            subTitle = (TextView) view.findViewById(R.id.subtitle);
        }
    }


}
