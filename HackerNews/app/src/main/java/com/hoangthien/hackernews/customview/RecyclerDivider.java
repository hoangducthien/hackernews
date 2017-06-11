package com.hoangthien.hackernews.customview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by thien on 4/25/17.
 */

public class RecyclerDivider extends RecyclerView.ItemDecoration {

    private int mHeight;
    private int mColor;
    private Rect mRect;
    private Paint mPaint;

    public RecyclerDivider(int height, int color) {
        mHeight = height;
        mColor = color;
        if (mColor != -1) {
            mPaint = new Paint();
            mPaint.setColor(mColor);
            mRect = new Rect();
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) > 0) {
            outRect.top = (int) (parent.getContext().getResources().getDisplayMetrics().density * mHeight);
        }
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mColor != -1) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mHeight;

                mRect.set(left, top, right, bottom);
                c.drawRect(mRect, mPaint);
            }
        }
    }

}
