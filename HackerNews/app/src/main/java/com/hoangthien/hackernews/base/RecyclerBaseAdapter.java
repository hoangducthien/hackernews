package com.hoangthien.hackernews.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by hoangthien on 8/2/16.
 */
public abstract class RecyclerBaseAdapter extends RecyclerView.Adapter {

    protected View.OnClickListener mCustomEvent1;

    public void setOnClickButton1(View.OnClickListener onClickButton){
        mCustomEvent1 = onClickButton;
    }

    protected View.OnClickListener mCustomEvent2;

    public void setOnClickButton2(View.OnClickListener onClickButton){
        mCustomEvent2 = onClickButton;
    }

}
