package com.hoangthien.hackernews.data.reponsitory.home;

import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.localdatastorage.DataCache;
import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.utils.AsyncTaskManager;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thien on 4/28/17.
 */

public class HomeReponsitoryImpl implements HomeReponsitory {

    private AsyncTaskManager mAsyncTaskManager;
    private HomeApiService mHomeApiService;
    private DataCache mDataCache;

    public HomeReponsitoryImpl(AsyncTaskManager asyncTaskManager, HomeApiService homeApiService) {
        mAsyncTaskManager = asyncTaskManager;
        mHomeApiService = homeApiService;
        mDataCache = DataCache.getInstance();
    }


    @Override
    public void getIdList(final TAsyncCallback<List<Long>> result) {
        mAsyncTaskManager.execute(new Runnable() {
            @Override
            public void run() {
                mHomeApiService.getIdList(new TAsyncCallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> responseData) {
                        mDataCache.setNewsIds(new ArrayList<>(responseData));
                        mDataCache.setNewsList(null);
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
    public void getDataList(final List<Long> ids, final TAsyncCallback<List<News>> result) {
        mAsyncTaskManager.execute(new Runnable() {
            @Override
            public void run() {
                mHomeApiService.getDataList(ids, new TAsyncCallback<List<News>>() {
                    @Override
                    public void onSuccess(List<News> responseData) {
                        mDataCache.addToNewsList(responseData);
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
    public ArrayList<News> getNewsListFromCache() {
        return mDataCache.getNewsList();
    }

    @Override
    public ArrayList<Long> getIdListFromCache() {
        return mDataCache.getNewsIds();
    }
}

