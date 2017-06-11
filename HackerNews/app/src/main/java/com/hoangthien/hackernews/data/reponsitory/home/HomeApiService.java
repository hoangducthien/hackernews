package com.hoangthien.hackernews.data.reponsitory.home;

import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.List;

/**
 * Created by thien on 5/11/17.
 */

public interface HomeApiService {

    void getIdList(TAsyncCallback<List<Long>> result);

    void getDataList(List<Long> ids, TAsyncCallback<List<News>> result);


}
