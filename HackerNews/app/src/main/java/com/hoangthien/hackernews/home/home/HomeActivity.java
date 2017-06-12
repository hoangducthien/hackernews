package com.hoangthien.hackernews.home.home;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.baseactivity.ListLoadingActivity;
import com.hoangthien.hackernews.customview.RecyclerDivider;
import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.data.reponsitory.home.HomeApiServiceImpl;
import com.hoangthien.hackernews.data.reponsitory.home.HomeReponsitoryImpl;
import com.hoangthien.hackernews.home.comment.CommentActivity;
import com.hoangthien.hackernews.utils.AsyncTaskManager;
import com.hoangthien.hackernews.utils.TConstants;

import java.util.List;

public class HomeActivity extends ListLoadingActivity implements HomeView {

    private HomePresenter mHomePresenter;
    private RecyclerView mRecyclerView;
    private HomeListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mLoading;
    private int mDataLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initToolbar(getTitle().toString());

        initView();

        mHomePresenter = new HomePresenter(this, new HomeReponsitoryImpl(AsyncTaskManager.getInstance(this), new HomeApiServiceImpl()));
        mHomePresenter.getData();

    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addItemDecoration(new RecyclerDivider(getResources().getDimensionPixelOffset(R.dimen.dimen_3_6), -1));
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHomePresenter.reloadData();
                mRecyclerView.addOnScrollListener(mOnScrollListener);
            }
        });

    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!mLoading && ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition() > mDataLength - 4) {
                mLoading = true;
                mHomePresenter.loadNextPage();
            }
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void showData(final List<News> newses, boolean canLoadmore) {
        mDataLength = newses.size();
        mSwipeRefreshLayout.setRefreshing(false);
        mLoading = false;
        if (mAdapter == null) {
            mAdapter = new HomeListAdapter(newses);
            mAdapter.setOnClickButton1(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    News news = (News) v.getTag();
                    if (news.getKids().size() == 0){
                        Toast.makeText(getApplicationContext(), R.string.no_comment, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(HomeActivity.this, CommentActivity.class);
                        intent.putExtra(TConstants.EXTRA_DATA, news.getKids());
                        startActivity(intent);
                    }
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        if (!canLoadmore) {
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHomePresenter.detachView();
    }
}
