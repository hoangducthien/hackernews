package com.hoangthien.hackernews;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hoangthien.hackernews.home.comment.CommentActivity;
import com.hoangthien.hackernews.utils.TConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * Created by thien on 6/14/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CommentActivityTest {

    @Rule
    public ActivityTestRule<CommentActivity> mCommentActivityActivityTestRule =
            new ActivityTestRule<>(CommentActivity.class, true, false);


    @Test
    public void rotationChange() {

        Intent intent = new Intent();
        try {
            JSONArray jsonArrays = new JSONArray("[14567403,14567365,14567390,14567283,14567425,14567311,14567248,14567421,14567340]");
            int length = jsonArrays.length();
            ArrayList<Long> kids = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                kids.add(jsonArrays.optLong(i));
            }
            intent.putExtra(TConstants.EXTRA_DATA, kids);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mCommentActivityActivityTestRule.launchActivity(intent);

        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));

        CommentActivity activity = mCommentActivityActivityTestRule.getActivity();
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(9));

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Screen rotation failed", e);
        }

        final RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view);
        int position = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
        assertEquals(position, 9);

        rotateScreen(activity);

        RecyclerView newRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view);
        int newPosition = ((LinearLayoutManager) newRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        assertEquals(position, newPosition);

    }

    public static void rotateScreen(AppCompatActivity activity) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int orientation = InstrumentationRegistry.getTargetContext()
                .getResources()
                .getConfiguration()
                .orientation;
        final int newOrientation = (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        activity.setRequestedOrientation(newOrientation);

        getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Screen rotation failed", e);
        }
    }

}
