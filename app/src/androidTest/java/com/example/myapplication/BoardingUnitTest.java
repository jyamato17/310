package com.example.myapplication;

import android.content.res.Resources;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import com.example.myapplication.ui.loading_screen.OnBoarding;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BoardingUnitTest {
    private String first_slide_title = "Access to COVID map wherever you are";
    private String first_slide_desc = "Our map shows COVID severity of any place nearby or within the specified city. You can click on a city/neighborhood to display location-related COVID information.";

    @Rule
    public ActivityScenarioRule rule = new ActivityScenarioRule<>(OnBoarding.class);

    @Test
    public void validateTexts() {
        Espresso.onView(ViewMatchers.withId(R.id.slider_heading1)).check(
                ViewAssertions.matches(ViewMatchers.withText(containsString(first_slide_title))));

        Espresso.onView(ViewMatchers.withId(R.id.slider_desc1)).check(
                ViewAssertions.matches(ViewMatchers.withText(containsString(first_slide_desc))));
    }

    @Test
    public void validateButtons() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.skip_btn), ViewMatchers.isClickable()));
        Espresso.onView(ViewMatchers.withId(R.id.skip_btn))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.next_btn), ViewMatchers.isClickable()));
        Espresso.onView(ViewMatchers.withId(R.id.next_btn))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.get_started_btn), ViewMatchers.isClickable()));
        Espresso.onView(ViewMatchers.withId(R.id.get_started_btn))
                .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
    }
}
