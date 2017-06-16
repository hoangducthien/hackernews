package com.hoangthien.hackernews;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hoangthien.hackernews.data.model.News;
import com.hoangthien.hackernews.home.comment.CommentActivity;
import com.hoangthien.hackernews.home.home.HomeActivity;
import com.hoangthien.hackernews.home.home.HomeListAdapter;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by thien on 6/14/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeActivityTest {

    @Rule
    public ActivityTestRule<HomeActivity> mHomeActivityActivityTestRule =
            new ActivityTestRule<>(HomeActivity.class);


    @Test
    public void listItemHasCommentClick() {

        //click on item has comments => open comment activity
        Matcher<RecyclerView.ViewHolder> hasComment = hasComment();
        onView(withId(R.id.recycler_view)).perform(scrollToHolder(hasComment), actionOnHolderItem(hasComment, click()));
        boolean b = getActivity() instanceof CommentActivity;
        assertTrue(b);

    }

    @Test
    public void listItemHasNoCommentClick() {

        //click on item has no comments => show no comment toast
        //ignore case all items has comments
        Matcher<RecyclerView.ViewHolder> hasNoComment = hasNoComment();
        onView(withId(R.id.recycler_view)).perform(scrollToHolder(hasNoComment), actionOnHolderItem(hasNoComment, click()));

        HomeActivity activity = mHomeActivityActivityTestRule.getActivity();
        onView(withText(R.string.no_comment)).
                inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).
                check(matches(isDisplayed()));
    }

    @Test
    public void rotationChange() {
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()));

        final HomeActivity activity = mHomeActivityActivityTestRule.getActivity();

        onView(withId(R.id.recycler_view)).perform(scrollToPosition(20));

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

        rotateScreen(activity);

        RecyclerView newRecyclerView = (RecyclerView) activity.findViewById(R.id.recycler_view);
        int newPosition = ((LinearLayoutManager) newRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
        assertEquals(position, 20);
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

    public static AppCompatActivity getActivity() {
        final AppCompatActivity[] activities = new AppCompatActivity[1];
        getInstrumentation().runOnMainSync(new Runnable() {
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

    public static Matcher<RecyclerView.ViewHolder> hasComment() {
        return new BoundedMatcher<RecyclerView.ViewHolder, HomeListAdapter.HomeViewHolder>(HomeListAdapter.HomeViewHolder.class) {
            boolean isFirst = true;
            News mNews;

            @Override
            public void describeTo(Description description) {
                description.appendText("No ViewHolder found");
            }

            @Override
            protected boolean matchesSafely(HomeListAdapter.HomeViewHolder item) {
                News news = (News) item.parent.getTag();
                if (news == mNews) {
                    return true;
                }
                if (news.getKids().size() == 0 || !isFirst) {
                    return false;
                }
                mNews = news;
                isFirst = false;
                return true;
            }
        };
    }

    public static Matcher<RecyclerView.ViewHolder> hasNoComment() {
        return new BoundedMatcher<RecyclerView.ViewHolder, HomeListAdapter.HomeViewHolder>(HomeListAdapter.HomeViewHolder.class) {
            boolean isFirst = true;
            News mNews;

            @Override
            public void describeTo(Description description) {
                description.appendText("No ViewHolder found");
            }

            @Override
            protected boolean matchesSafely(HomeListAdapter.HomeViewHolder item) {
                News news = (News) item.parent.getTag();
                if (news == mNews) {
                    return true;
                }
                news.getKids().clear();
                if (news.getKids().size() > 0 || !isFirst) {
                    return false;
                }
                mNews = news;
                isFirst = false;
                return true;
            }
        };
    }


}
