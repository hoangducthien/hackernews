package com.hoangthien.hackernews.ui.home;

import android.view.View;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.BasePresenter;
import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.data.reponsitory.home.HomeReponsitory;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thien on 4/25/17.
 */

public class HomePresenter extends BasePresenter<HomeView> {

    private static final int PAGE_SIZE = 25;

    private HomeReponsitory mHomeReponsitory;
    private ArrayList<News> mNewses = new ArrayList<>();
    private ArrayList<Long> mIds;
    private int mCurrentIndex = 0;
    private boolean mCanLoadmore = true;
    private int mNextPageSize;


    public HomePresenter(HomeView view, HomeReponsitory homeReponsitory) {
        super(view);
        mHomeReponsitory = homeReponsitory;
    }


    public void getData() {
        getView().showLoading();
        mHomeReponsitory.getIdList(mIdsCallback);
    }

    public void reloadData() {
        mIds = null;
        mCanLoadmore = true;
        mCurrentIndex = 0;
        mNewses = new ArrayList<>();
        mHomeReponsitory.getIdList(mIdsCallback);
    }

    private TAsyncCallback<List<Long>> mIdsCallback = new TAsyncCallback<List<Long>>() {
        @Override
        public void onSuccess(List<Long> responseData) {
            mIds = new ArrayList<>(responseData);
            loadNextPage();
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


    public void loadNextPage() {
        if (mIds.size() > mCurrentIndex) {
            mNextPageSize = PAGE_SIZE;
            if (mCurrentIndex + mNextPageSize >= mIds.size()) {
                mNextPageSize = mIds.size() - mCurrentIndex - 1;
                mCanLoadmore = false;
            }

            mHomeReponsitory.getDataList(mIds.subList(mCurrentIndex, mCurrentIndex + mNextPageSize), mDataCallback);
        }
    }


    private TAsyncCallback<List<News>> mDataCallback = new TAsyncCallback<List<News>>() {
        @Override
        public void onSuccess(List<News> responseData) {
            if (getView() != null) {
                getView().hideLoading();
                mNewses.addAll(responseData);
                mCurrentIndex += mNextPageSize;
                getView().showData(responseData);
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


}