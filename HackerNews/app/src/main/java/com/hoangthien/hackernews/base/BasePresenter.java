package com.hoangthien.hackernews.base;

/**
 * Created by thien on 4/25/17.
 */

public abstract class BasePresenter<V> {

    private V mView;

    public BasePresenter() {
    }

    public BasePresenter(V view) {
        mView = view;
    }

    public void atachView(V view) {
        mView = view;
    }

    public void detachView() {
        mView = null;
    }

    public V getView() {
        return mView;
    }
}