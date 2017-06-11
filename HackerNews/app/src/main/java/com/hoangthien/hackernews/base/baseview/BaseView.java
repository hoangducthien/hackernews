package com.hoangthien.hackernews.base.baseview;

import android.view.View;

import com.hoangthien.hackernews.base.TError;

/**
 * Created by thien on 4/25/17.
 */

public interface BaseView {

    void showError(TError error);

    void showError(TError error, int action, View.OnClickListener listener);

}
