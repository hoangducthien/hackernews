package com.hoangthien.hackernews.home.comment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.baseactivity.ListLoadingActivity;
import com.hoangthien.hackernews.customview.RecyclerDivider;
import com.hoangthien.hackernews.data.model.Comment;
import com.hoangthien.hackernews.data.reponsitory.comment.CommentApiServiceImpl;
import com.hoangthien.hackernews.data.reponsitory.comment.CommentReponsitoryImpl;
import com.hoangthien.hackernews.utils.AsyncTaskManager;
import com.hoangthien.hackernews.utils.TConstants;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends ListLoadingActivity implements CommentView {

    private CommentPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private CommentListAdapter mAdapter;
    private boolean mLoading;
    private int mDataLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.comment_list_layout);

        initToolbar(getTitle().toString());
        showBackBtn();
        initView();

        ArrayList<Long> ids = (ArrayList<Long>) getIntent().getSerializableExtra(TConstants.EXTRA_DATA);

        mPresenter = new CommentPresenter(this, new CommentReponsitoryImpl(AsyncTaskManager.getInstance(this), new CommentApiServiceImpl()), ids);
        mPresenter.getData();

    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addItemDecoration(new RecyclerDivider(getResources().getDimensionPixelOffset(R.dimen.dimen_3_6), -1));
        mRecyclerView.addOnScrollListener(mOnScrollListener);

    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (!mLoading && ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition() > mDataLength - 4) {
                mLoading = true;
                mPresenter.loadNextPage();
            }
        }
    };

    @Override
    public void showData(final List<Comment> comments, boolean canLoadmore) {
        mDataLength = comments.size();
        mLoading = false;
        if (mAdapter == null) {
            mAdapter = new CommentListAdapter(comments, this);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
        if (!canLoadmore) {
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
