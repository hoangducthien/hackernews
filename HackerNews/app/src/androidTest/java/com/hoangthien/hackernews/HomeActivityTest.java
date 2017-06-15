package com.hoangthien.hackernews;

import android.content.ComponentName;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hoangthien.hackernews.home.comment.CommentActivity;
import com.hoangthien.hackernews.home.home.HomeActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

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
        onView(withId(R.id.recycler_view)).perform(actionOnItemAtPosition(0, click()));
        intended(hasComponent(new ComponentName(getTargetContext(), CommentActivity.class)));
    }


}
