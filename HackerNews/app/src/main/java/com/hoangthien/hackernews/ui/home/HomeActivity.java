package com.hoangthien.hackernews.ui.home;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.baseactivity.ListLoadingActivity;
import com.hoangthien.hackernews.customview.RecyclerDivider;
import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.data.reponsitory.home.HomeApiServiceImpl;
import com.hoangthien.hackernews.data.reponsitory.home.HomeReponsitoryImpl;

import java.util.List;

public class HomeActivity extends ListLoadingActivity implements HomeView {

    private HomePresenter mHomePresenter;
    private RecyclerView mRecyclerView;
    private HomeListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initToolbar(getTitle().toString());

        initView();

        mHomePresenter = new HomePresenter(this, new HomeReponsitoryImpl(this, new HomeApiServiceImpl()));
        mHomePresenter.getData();

    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addItemDecoration(new RecyclerDivider(getResources().getDimensionPixelOffset(R.dimen.dimen_3_6), -1));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHomePresenter.reloadData();
            }
        });

    }

    @Override
    public void showData(final List<News> categories) {
        mAdapter = new HomeListAdapter(categories);
        mAdapter.setOnClickButton1(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                News category = (News) v.getTag();
//                Intent intent = new Intent(HomeActivity.this, VideoListActivity.class);
//                intent.putExtra(TConstants.EXTRA_DATA, category.getType());
//                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHomePresenter.detachView();
    }
}
