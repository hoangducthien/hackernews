package com.hoangthien.hackernews.data.reponsitory.comment;

import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.AsyncTaskManager;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.List;

/**
 * Created by thien on 4/28/17.
 */

public class CommentReponsitoryImpl implements CommentReponsitory {

    private AsyncTaskManager mAsyncTaskManager;
    private CommentApiService mCommentApiService;

    public CommentReponsitoryImpl(AsyncTaskManager asyncTaskManager, CommentApiService commentApiService) {
        mAsyncTaskManager = asyncTaskManager;
        mCommentApiService = commentApiService;
    }

    @Override
    public void getDataList(final List<Long> ids, final TAsyncCallback<List<Comment>> result) {
        mAsyncTaskManager.execute(new Runnable() {
            @Override
            public void run() {
                mCommentApiService.getDataList(ids, new TAsyncCallback<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> responseData) {
                        mAsyncTaskManager.successCallbackOnUIThread(result, responseData);
                    }

                    @Override
                    public void onError(TError error) {
                        mAsyncTaskManager.errorCallbackOnUIThread(result, error);
                    }
                });
            }
        });
    }
}
