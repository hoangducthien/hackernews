package com.hoangthien.hackernews.data.reponsitory.home;

import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.utils.TAsyncCallback;

import java.util.List;

/**
 * Created by thien on 4/28/17.
 */

public interface HomeReponsitory {

    void getIdList(TAsyncCallback<List<Long>> result);

    void getDataList(List<Long> ids, TAsyncCallback<List<News>> result);

}
