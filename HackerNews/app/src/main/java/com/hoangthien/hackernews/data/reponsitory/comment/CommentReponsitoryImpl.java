package com.hoangthien.hackernews.data.reponsitory.comment;

import android.content.Context;

import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.AsyncTaskManager;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.ArrayList;

/**
 * Created by thien on 4/28/17.
 */

public class CommentReponsitoryImpl implements CommentReponsitory {

    private Context mContext;
    private AsyncTaskManager mAsyncTaskManager;
    private CommentApiService mCommentApiService;

    public CommentReponsitoryImpl(Context context, CommentApiService commentApiService) {
        mContext = context.getApplicationContext();
        mAsyncTaskManager = AsyncTaskManager.getInstance(context);
        mCommentApiService = commentApiService;
    }

    @Override
    public void getDataList(final ArrayList<Long> ids, final TAsyncCallback<ArrayList<Comment>> result) {
        mAsyncTaskManager.execute(new Runnable() {
            @Override
            public void run() {
                mCommentApiService.getDataList(ids, new TAsyncCallback<ArrayList<Comment>>() {
                    @Override
                    public void onSuccess(ArrayList<Comment> responseData) {
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
