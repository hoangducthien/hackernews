package com.hoangthien.hackernews.home.comment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.RecyclerBaseAdapter;
import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.DateFormater;

import java.util.List;

/**
 * thienhd
 */
public class CommentListAdapter extends RecyclerBaseAdapter {

    private List<Comment> mItems;
    private int mMarginLeft;

    public CommentListAdapter(List<Comment> items, Context context) {
        mItems = items;
        mMarginLeft = context.getResources().getDimensionPixelOffset(R.dimen.dimen_25_50);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new HomeViewHolder(v);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int i) {
        HomeViewHolder viewHolder = (HomeViewHolder) vh;

        Comment comment = mItems.get(i);

        viewHolder.title.setText(Html.fromHtml(comment.getText()));

        StringBuilder s = new StringBuilder();
        s.append(comment.getBy());
        s.append(" ").append(DateFormater.convertToTimeAgo(System.currentTimeMillis() / 1000 - comment.getTime()));

        viewHolder.subTitle.setText(s.toString());

        if (TextUtils.isEmpty(comment.getReplyOf())) {
            ((RecyclerView.LayoutParams) viewHolder.parent.getLayoutParams()).setMargins(0, 0, 0, 0);
        } else {
            ((RecyclerView.LayoutParams) viewHolder.parent.getLayoutParams()).setMargins(mMarginLeft, 0, 0, 0);
        }
    }


    private class HomeViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView subTitle;
        View parent;

        public HomeViewHolder(View view) {
            super(view);
            parent = view;
            title = (TextView) view.findViewById(R.id.title);
            subTitle = (TextView) view.findViewById(R.id.subtitle);
        }
    }


}
