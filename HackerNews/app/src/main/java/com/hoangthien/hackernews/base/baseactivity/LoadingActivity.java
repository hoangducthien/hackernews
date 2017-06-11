package com.hoangthien.hackernews.base.baseactivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.baseview.LoadingView;
import com.hoangthien.hackernews.utils.TConstants;

/**
 * Created by hoangthien on 5/21/17.
 */

public class LoadingActivity extends BaseActivity implements LoadingView {

    protected View mLoading;

    @Override
    public void showLoading() {
        if (mLoading == null) {
            mLoading = findViewById(R.id.loading);
        }
        if (mLoading == null) {
            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
            mLoading = LayoutInflater.from(this).inflate(R.layout.loading_layout, contentView, false);
            int marginTop = 0;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                marginTop = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
            ((FrameLayout.LayoutParams) mLoading.getLayoutParams()).setMargins(0, marginTop, 0, 0);
            contentView.addView(mLoading);
        }
        if (mLoading != null) {
            mLoading.setVisibility(View.VISIBLE);
            mLoading.setAlpha(1);
        }
    }

    @Override
    public void hideLoading() {
        if (mLoading != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mLoading, "alpha", 0).setDuration(TConstants.DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoading.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
    }

    @Override
    public void showLoadingNoPadding() {
        if (mLoading == null) {
            mLoading = findViewById(R.id.loading);
        }
        if (mLoading == null) {
            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
            mLoading = LayoutInflater.from(this).inflate(R.layout.loading_layout, contentView, false);
            contentView.addView(mLoading);
        }
        if (mLoading != null) {
            mLoading.setVisibility(View.VISIBLE);
            mLoading.setAlpha(1);
        }
    }

}
