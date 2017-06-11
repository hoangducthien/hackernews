package com.hoangthien.hackernews.base.baseactivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.baseview.ListLoadingView;
import com.hoangthien.hackernews.utils.TConstants;

/**
 * Created by hoangthien on 5/21/17.
 */

public class ListLoadingActivity extends LoadingActivity implements ListLoadingView {

    protected View mLoadmore;
    private int mLoadMoreHeight;

    private void initLoadmoreLayout() {
        if (mLoadmore == null) {
            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
            mLoadmore = LayoutInflater.from(this).inflate(R.layout.load_more_layout, contentView, false);
            contentView.addView(mLoadmore);
            mLoadMoreHeight = getResources().getDimensionPixelSize(R.dimen.dimen_55_110);
            mLoadmore.setTranslationY(mLoadMoreHeight);
        }
    }

    @Override
    public void hideLoadmore() {
        if (mLoadmore != null) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mLoadmore, "translationY", mLoadMoreHeight).setDuration(TConstants.DURATION);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mLoadmore.setVisibility(View.GONE);
                }
            });
            objectAnimator.start();
        }
    }

    @Override
    public void showLoadmore() {
        initLoadmoreLayout();
        ObjectAnimator.ofFloat(mLoadmore, "translationY", 0).setDuration(TConstants.DURATION).start();
        mLoadmore.setVisibility(View.VISIBLE);
    }
}
