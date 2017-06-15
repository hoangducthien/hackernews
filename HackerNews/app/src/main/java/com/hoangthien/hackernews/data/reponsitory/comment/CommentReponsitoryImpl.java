package com.hoangthien.hackernews.data.reponsitory.comment;

import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.localdatastorage.DataCache;
import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.AsyncTaskManager;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thien on 4/28/17.
 */

public class CommentReponsitoryImpl implements CommentReponsitory {

    private AsyncTaskManager mAsyncTaskManager;
    private CommentApiService mCommentApiService;
    private DataCache mDataCache;

    public CommentReponsitoryImpl(AsyncTaskManager asyncTaskManager, CommentApiService commentApiService) {
        mAsyncTaskManager = asyncTaskManager;
        mCommentApiService = commentApiService;
        mDataCache = DataCache.getInstance();
    }

    @Override
    public void getDataList(final List<Long> ids, final TAsyncCallback<List<Comment>> result) {
        mAsyncTaskManager.execute(new Runnable() {
            @Override
            public void run() {
                mCommentApiService.getDataList(ids, new TAsyncCallback<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> responseData) {
                        mDataCache.addComments(responseData);
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

    @Override
    public ArrayList<Comment> getCommentsFromCache() {
        return mDataCache.getComments();
    }

    @Override
    public void clearComments() {
        mDataCache.setComments(null);
    }
}
