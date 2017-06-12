package com.hoangthien.hackernews.home;

import com.hoangthien.hackernews.data.reponsitory.home.HomeReponsitory;
import com.hoangthien.hackernews.data.reponsitory.home.HomeReponsitoryImpl;
import com.hoangthien.hackernews.home.home.HomePresenter;
import com.hoangthien.hackernews.home.home.HomeView;
import com.hoangthien.hackernews.utils.AsyncTaskManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.mockito.Mockito.verify;

/**
 * Created by thien on 6/12/17.
 */

public class HomePresenterTest {

    @Mock
    private AsyncTaskManager mAsyncTaskManager;

    @Mock
    private HomeView mHomeView;


    private HomePresenter mHomePresenter;

    @Before
    public void setupPresenter() {
        MockitoAnnotations.initMocks(this);
        HomeReponsitory homeReponsitory = new HomeReponsitoryImpl(mAsyncTaskManager, new TestHomeApiServiceImpl());
        mHomePresenter = new HomePresenter(mHomeView, homeReponsitory);
    }

    @Test
    public void getData() {
        mHomePresenter.getData();
        verify(mHomeView).showLoading();
    }

    @Test
    public void loadNextPage() {
        mHomePresenter.loadData(new ArrayList<Long>());
        verify(mHomeView).showLoadmore();
    }

}
