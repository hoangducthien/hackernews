package com.hoangthien.hackernews.ui.videolist;

import java.util.ArrayList;

import com.hoangthien.hackernews.base.baseview.ListLoadingView;
import com.hoangthien.hackernews.data.model.Comment;

/**
 * Created by thien on 4/25/17.
 */

public interface VideoListView extends ListLoadingView {

    void showData(ArrayList<Comment> videos, boolean canLoadMore);

    void startPlayerActivity(int position, Class activity);

}
