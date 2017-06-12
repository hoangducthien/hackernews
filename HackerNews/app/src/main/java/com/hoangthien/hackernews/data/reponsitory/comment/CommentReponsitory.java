package com.hoangthien.hackernews.data.reponsitory.comment;

import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.List;

/**
 * Created by thien on 4/28/17.
 */

public interface CommentReponsitory {

    void getDataList(List<Long> ids, TAsyncCallback<List<Comment>> result);

}
