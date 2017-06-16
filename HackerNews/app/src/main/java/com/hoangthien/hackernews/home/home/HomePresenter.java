package com.hoangthien.hackernews.home.home;

import android.os.Bundle;
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

    public static final String CURRENT_DATA_INDEX = "aaa";
    public static final String CAN_LOAD_MORE = "aab";
    public static final String NEXT_PAGE_SIZE = "aac";

    //TODO handle large screen (bigger page size)
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

    public void checkCache(Bundle bundle) {
        int oldListPosition = bundle.getInt(HomeActivity.CURRENT_POSITION, -1);
        if (oldListPosition > -1) {
            mIds = mHomeReponsitory.getIdListFromCache();
            if (mIds != null && !mIds.isEmpty()) {
                mNewses = mHomeReponsitory.getNewsListFromCache();
                if (mNewses != null && !mNewses.isEmpty()) {
                    mCurrentIndex = bundle.getInt(CURRENT_DATA_INDEX);
                    mNextPageSize = bundle.getInt(NEXT_PAGE_SIZE);
                    mCanLoadmore = bundle.getBoolean(CAN_LOAD_MORE);
                    getView().showData(mNewses, mCanLoadmore);
                    return;
                }
            }
        }
        getView().showLoading();
        reloadData();
    }

    public void saveCurrentState(Bundle bundle) {
        bundle.putInt(CURRENT_DATA_INDEX, mCurrentIndex);
        bundle.putInt(NEXT_PAGE_SIZE, mNextPageSize);
        bundle.putBoolean(CAN_LOAD_MORE, mCanLoadmore);
    }

    public void reloadData() {
        mIds = null;
        mCanLoadmore = true;
        mCurrentIndex = 0;
        mHomeReponsitory.getIdList(mIdsCallback);
    }

    private TAsyncCallback<List<Long>> mIdsCallback = new TAsyncCallback<List<Long>>() {
        @Override
        public void onSuccess(List<Long> responseData) {
            mIds = new ArrayList<>(responseData);
            mNextPageSize = PAGE_SIZE;
            if (mCurrentIndex + mNextPageSize >= mIds.size()) {
                mNextPageSize = mIds.size() - mCurrentIndex;
                mCanLoadmore = false;
            }
            mHomeReponsitory.getDataList(mIds.subList(mCurrentIndex, mCurrentIndex + mNextPageSize), mFirstPageDataCallback);
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
                mNextPageSize = mIds.size() - mCurrentIndex;
                mCanLoadmore = false;
            }

            loadData(mIds.subList(mCurrentIndex, mCurrentIndex + mNextPageSize));
        }
    }

    public void loadData(List<Long> ids) {
        getView().showLoadmore();
        mHomeReponsitory.getDataList(ids, mDataCallback);
    }

    private TAsyncCallback<List<News>> mFirstPageDataCallback = new TAsyncCallback<List<News>>() {
        @Override
        public void onSuccess(List<News> responseData) {
            if (getView() != null) {
                getView().hideLoading();
                mNewses.clear();
                mNewses.addAll(responseData);
                mCurrentIndex += mNextPageSize;
                getView().showData(mNewses, mCanLoadmore);
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

    private TAsyncCallback<List<News>> mDataCallback = new TAsyncCallback<List<News>>() {
        @Override
        public void onSuccess(List<News> responseData) {
            if (getView() != null) {
                getView().hideLoadmore();
                mNewses.addAll(responseData);
                mCurrentIndex += mNextPageSize;
                getView().showData(mNewses, mCanLoadmore);
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
