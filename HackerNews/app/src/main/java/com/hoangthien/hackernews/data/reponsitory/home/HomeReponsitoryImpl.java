package com.hoangthien.hackernews.data.reponsitory.home;

import android.content.Context;

import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.utils.AsyncTaskManager;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.List;

/**
 * Created by thien on 4/28/17.
 */

public class HomeReponsitoryImpl implements HomeReponsitory {

    private Context mContext;
    private AsyncTaskManager mAsyncTaskManager;
    private HomeApiService mHomeApiService;

    public HomeReponsitoryImpl(Context context, HomeApiService homeApiService) {
        mContext = context.getApplicationContext();
        mAsyncTaskManager = AsyncTaskManager.getInstance(context);
        mHomeApiService = homeApiService;
    }


    @Override
    public void getIdList(final TAsyncCallback<List<Long>> result) {
        mAsyncTaskManager.execute(new Runnable() {
            @Override
            public void run() {
                mHomeApiService.getIdList(new TAsyncCallback<List<Long>>() {
                    @Override
                    public void onSuccess(List<Long> responseData) {
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
