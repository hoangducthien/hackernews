package com.hoangthien.hackernews.home.comment;

import com.hoangthien.hackernews.base.baseview.ListLoadingView;
import com.hoangthien.hackernews.data.model.Comment;

import java.util.List;

/**
 * Created by thien on 4/25/17.
 */

public interface CommentView extends ListLoadingView {

    void showData(List<Comment> comments, boolean canLoadmore);

}
