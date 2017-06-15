package com.hoangthien.hackernews;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.v7.app.AppCompatActivity;

import com.hoangthien.hackernews.home.comment.CommentActivity;
import com.hoangthien.hackernews.home.home.HomeActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static org.junit.Assert.assertTrue;

/**
 * Created by thien on 6/14/17.
 */

@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    @Rule
    public ActivityTestRule<HomeActivity> mHomeActivityActivityTestRule =
            new ActivityTestRule<>(HomeActivity.class);


    @Test
    public void listItemClick() {

        onView(ViewMatchers.withId(R.id.recycler_view)).perform(actionOnItemAtPosition(1, click()));
        boolean b = getActivity() instanceof CommentActivity;
        assertTrue(b);
    }


    public static AppCompatActivity getActivity() {
        final AppCompatActivity[] activities = new AppCompatActivity[1];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                AppCompatActivity currentActivity = null;
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()) {
                    currentActivity = (AppCompatActivity) resumedActivities.iterator().next();
                    activities[0] = currentActivity;
                }
            }
        });

        return activities[0];
    }


}
