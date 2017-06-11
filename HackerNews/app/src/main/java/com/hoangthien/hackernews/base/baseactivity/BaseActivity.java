package com.hoangthien.hackernews.base.baseactivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hoangthien.hackernews.R;
import com.hoangthien.hackernews.base.TError;
import com.hoangthien.hackernews.base.baseview.BaseView;

/**
 * Created by thien on 4/25/17.
 */

public class BaseActivity extends AppCompatActivity implements BaseView {

    private Snackbar mSnackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void showError(TError error) {
        showError(error, R.string.ok, mDefaultSnackbarListener);
    }

    @Override
    public void showError(TError error, int action, final View.OnClickListener listener) {
        ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
        if (error.getStringId() > 0) {
            mSnackbar = Snackbar.make(contentView, error.getStringId(), Snackbar.LENGTH_INDEFINITE)
                    .setAction(action, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onClick(v);
                            mSnackbar.dismiss();
                        }
                    });
            mSnackbar.show();
        } else {
            mSnackbar = Snackbar.make(contentView, error.getErrorMessage(), Snackbar.LENGTH_INDEFINITE)
                    .setAction(action, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onClick(v);
                            mSnackbar.dismiss();
                        }
                    });
            mSnackbar.show();
        }
    }

    private View.OnClickListener mDefaultSnackbarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSnackbar.dismiss();
        }
    };

    public void initToolbar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (toolbar != null) {
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
        }
    }

    public void showBackBtn() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
