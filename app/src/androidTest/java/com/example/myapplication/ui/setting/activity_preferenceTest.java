package com.example.myapplication.ui.setting;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static java.util.EnumSet.allOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
class preference_activityTest extends TestCase {
    @Rule
    public ActivityTestRule<activity_preference> settingFragmentActivityTestRule
            = new ActivityTestRule<activity_preference>(activity_preference.class);

    private activity_preference sFragment =  null;

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void clickListPreference() throws Exception{

        // Check if it is displayed
        Context appContext = InstrumentationRegistry.getTargetContext();

        onData(allOf(
                is(instanceOf(activity_preference.class)),
                withKey(appContext.getResources().getString(R.string.pref_units_key))))
                .check(matches(isDisplayed()));

        // Check if click is working
        onData(allOf(
                is(instanceOf(activity_preference.class)),
                withKey(appContext.getResources().getString(R.string.pref_units_key))))
                .onChildView(withText(appContext.getResources()
                        .getString(R.string.pref_units_label))).perform(click());
    }
    public void tearDown() throws Exception {
    }
}