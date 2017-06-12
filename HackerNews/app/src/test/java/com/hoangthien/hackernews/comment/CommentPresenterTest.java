package com.hoangthien.hackernews.comment;

import com.hoangthien.hackernews.data.reponsitory.comment.CommentReponsitory;
import com.hoangthien.hackernews.data.reponsitory.comment.CommentReponsitoryImpl;
import com.hoangthien.hackernews.home.comment.CommentPresenter;
import com.hoangthien.hackernews.home.comment.CommentView;
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

public class CommentPresenterTest {

    @Mock
    private AsyncTaskManager mAsyncTaskManager;

    @Mock
    private CommentView mCommentView;


    private CommentPresenter mCommentPresenter;

    @Before
    public void setupPresenter() {
        MockitoAnnotations.initMocks(this);
        CommentReponsitory commentReponsitory = new CommentReponsitoryImpl(mAsyncTaskManager, new TestCommentApiServiceImpl());
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(123l);
        mCommentPresenter = new CommentPresenter(mCommentView, commentReponsitory, ids);
    }

    @Test
    public void getData() {
        mCommentPresenter.getData();
        verify(mCommentView).showLoading();
    }

    @Test
    public void loadNextPage() {
        mCommentPresenter.loadData(new ArrayList<Long>());
        verify(mCommentView).showLoadmore();
    }

}
