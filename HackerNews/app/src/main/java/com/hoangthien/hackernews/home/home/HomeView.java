package com.hoangthien.hackernews.home.home;

import com.hoangthien.hackernews.base.baseview.ListLoadingView;
import com.hoangthien.hackernews.data.model.News;

import java.util.List;

/**
 * Created by thien on 4/25/17.
 */

public interface HomeView extends ListLoadingView {

    void showData(List<News> newses, boolean canLoadmore);

    void setListPosition(int position);

}
