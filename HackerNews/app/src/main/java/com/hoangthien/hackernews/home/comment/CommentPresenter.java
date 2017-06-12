package com.hoangthien.hackernews.home.comment;

import android.view.View;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.BasePresenter;
import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.data.reponsitory.comment.CommentReponsitory;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thien on 4/25/17.
 */

public class CommentPresenter extends BasePresenter<CommentView> {

    //TODO handle large screen (bigger page size)
    private static final int PAGE_SIZE = 10;

    private CommentReponsitory mReponsitory;
    private ArrayList<Comment> mComments = new ArrayList<>();
    private ArrayList<Long> mIds;
    private int mCurrentIndex = 0;
    private boolean mCanLoadmore = true;
    private int mNextPageSize;


    public CommentPresenter(CommentView view, CommentReponsitory homeReponsitory, ArrayList<Long> ids) {
        super(view);
        mReponsitory = homeReponsitory;
        mIds = ids;
    }


    public void getData() {
        getView().showLoading();
        mNextPageSize = PAGE_SIZE;
        if (mCurrentIndex + mNextPageSize >= mIds.size()) {
            mNextPageSize = mIds.size() - mCurrentIndex;
            mCanLoadmore = false;
        }
        mReponsitory.getDataList(mIds.subList(mCurrentIndex, mCurrentIndex + mNextPageSize), mFirstPageDataCallback);
    }


    public void loadNextPage() {
        if (mIds.size() > mCurrentIndex) {
            mNextPageSize = PAGE_SIZE;
            if (mCurrentIndex + mNextPageSize >= mIds.size()) {
                mNextPageSize = mIds.size() - mCurrentIndex;
                mCanLoadmore = false;
            }

            loadData(mIds.subList(mCurrentIndex, mCurrentIndex + mNextPageSize));
        }
    }

    public void loadData(List<Long> ids) {
        getView().showLoadmore();
        mReponsitory.getDataList(ids, mDataCallback);
    }

    private TAsyncCallback<List<Comment>> mFirstPageDataCallback = new TAsyncCallback<List<Comment>>() {
        @Override
        public void onSuccess(List<Comment> responseData) {
            if (getView() != null) {
                getView().hideLoading();
                mComments.clear();
                mComments.addAll(responseData);
                mCurrentIndex += mNextPageSize;
                getView().showData(mComments, mCanLoadmore);
            }
        }

        @Override
        public void onError(TError error) {
            if (getView() != null) {
                getView().hideLoading();
                getView().showError(error, R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getData();
                    }
                });
            }
        }
    };

    private TAsyncCallback<List<Comment>> mDataCallback = new TAsyncCallback<List<Comment>>() {
        @Override
        public void onSuccess(List<Comment> responseData) {
            if (getView() != null) {
                getView().hideLoadmore();
                mComments.addAll(responseData);
                mCurrentIndex += mNextPageSize;
                getView().showData(mComments, mCanLoadmore);
            }
        }

        @Override
        public void onError(TError error) {
            if (getView() != null) {
                getView().hideLoadmore();
                getView().showError(error, R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getData();
                    }
                });
            }
        }
    };


}
