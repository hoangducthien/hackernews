package com.hoangthien.hackernews.comment;

import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.data.reponsitory.comment.CommentApiService;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thien on 6/12/17.
 */

public class TestCommentApiServiceImpl implements CommentApiService {


    @Override
    public void getDataList(List<Long> ids, TAsyncCallback<List<Comment>> result) {
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment());

        result.onSuccess(comments);
    }

}
