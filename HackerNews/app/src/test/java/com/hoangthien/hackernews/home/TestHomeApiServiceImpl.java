package com.hoangthien.hackernews.home;

import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.data.reponsitory.home.HomeApiService;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thien on 6/12/17.
 */

public class TestHomeApiServiceImpl implements HomeApiService {


    @Override
    public void getIdList(TAsyncCallback<List<Long>> result) {
        List<Long> ids = new ArrayList<>();
        ids.add(123l);
        ids.add(124l);
        result.onSuccess(ids);
    }

    @Override
    public void getDataList(List<Long> ids, TAsyncCallback<List<News>> result) {
        List<News> newses = new ArrayList<>();
        newses.add(new News());
        newses.add(new News());

        result.onSuccess(newses);
    }
}
