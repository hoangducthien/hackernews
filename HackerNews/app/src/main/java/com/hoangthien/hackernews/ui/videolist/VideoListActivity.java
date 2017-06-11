package com.hoangthien.hackernews.ui.videolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.baseactivity.ListLoadingActivity;
import com.hoangthien.hackernews.customview.RecyclerDivider;
import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.utils.TConstants;

import java.util.ArrayList;

public class VideoListActivity extends ListLoadingActivity implements VideoListView {

    private VideoListPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private VideoListAdapter mAdapter;
    private int mDataLength;
    private boolean mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_list_layout);

        initToolbar(getIntent().getStringExtra(TConstants.EXTRA_TITLE));
        showBackBtn();
        initView();

//        mPresenter = new VideoListPresenter(this, new CommentReponsitoryImpl(this, new CommentApiServiceImpl()));
        int type = getIntent().getIntExtra(TConstants.EXTRA_TYPE, 0);
        String sourceId = getIntent().getStringExtra(TConstants.EXTRA_ID);
        mPresenter.getData(type, sourceId);

    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.addItemDecoration(new RecyclerDivider((int) getResources().getDisplayMetrics().density, 0xffcccccc));
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!mLoading && ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > mDataLength - 4) {
                mLoading = true;
                mPresenter.loadNextPage();
            }
        }
    };

    @Override
    public void showData(ArrayList<Comment> videos, boolean canLoadMore) {
        mDataLength = videos.size();
        mLoading = false;
        if (mAdapter == null) {
            mAdapter = new VideoListAdapter(videos);
            mAdapter.setOnClickButton1(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPresenter.handleItemSelected((Integer) v.getTag());
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        if (!canLoadMore) {
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
        }
    }

    @Override
    public void startPlayerActivity(int position, Class activity) {
        Intent intent = new Intent(VideoListActivity.this, activity);
        intent.putExtra(TConstants.EXTRA_DATA, position);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
