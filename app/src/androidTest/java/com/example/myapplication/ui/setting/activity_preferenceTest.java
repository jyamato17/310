package com.example.myapplication.ui.setting;

import android.preference.CheckBoxPreference;

import androidx.test.rule.ActivityTestRule;

import static org.hamcrest.MatcherAssert.assertThat;
import com.example.myapplication.R;
import static org.hamcrest.Matchers.is;
import static com.google.android.apps.common.testing.ui.espresso.matcher.PreferenceMatchers.withKey;
import static com.google.android.apps.common.testing.ui.espresso.matcher.PreferenceMatchers.withSummary;
import static com.google.android.apps.common.testing.ui.espresso.matcher.PreferenceMatchers.withSummaryText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.PreferenceMatchers.withTitle;
import static com.google.android.apps.common.testing.ui.espresso.matcher.PreferenceMatchers.withTitleText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.PreferenceMatchers.isEnabled;
import static org.hamcrest.Matchers.not;
import com.google.android.apps.common.testing.ui.testapp.test.R;
import android.test.InstrumentationTestCase;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;


import org.junit.Rule;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class activity_preferenceTest extends InstrumentationTestCase{
    @Rule
    public ActivityTestRule<activity_preference> pActivityTestRule
            = new ActivityTestRule<>(activity_preference.class);

    public void testWithSummary() {
        CheckBoxPreference pref = new CheckBoxPreference(getInstrumentation().getContext());
        pref.setSummary("Night mode is activated");
        assertThat(pref, withSummary("Night mode is activated");
        assertThat(pref, not(withSummary("Night Mode")));

        assertThat(pref, withSummaryText("Hello World"));
        assertThat(pref, not(withSummaryText(("Hello Mars"))));
        assertThat(pref, withSummaryText(is("Hello World")));
    }

    public void testWithTitle() {
        CheckBoxPreference pref = new CheckBoxPreference(getInstrumentation().getContext());
        pref.setTitle("Night Mode");
        assertThat(pref, withTitle(R.string.other_string));
        assertThat(pref, not(withTitle(R.string.something)));
        assertThat(pref, withTitleText("Goodbye!!"));
        assertThat(pref, not(withTitleText(("Hello Mars"))));
        assertThat(pref, withTitleText(is("Goodbye!!")));
    }
    public void testIsEnabled() {
        CheckBoxPreference pref = new CheckBoxPreference(getInstrumentation().getContext());
        pref.setEnabled(true);
        assertThat(pref, isEnabled());
        pref.setEnabled(false);
        assertThat(pref, not(isEnabled()));
        EditTextPreference pref2 = new EditTextPreference(getInstrumentation().getContext());
        pref2.setEnabled(true);
        assertThat(pref2, isEnabled());
        pref2.setEnabled(false);
        assertThat(pref2, not(isEnabled()));
    }
    public void testWithKey() {
        CheckBoxPreference pref = new CheckBoxPreference(getInstrumentation().getContext());
        pref.setKey("foo");
        assertThat(pref, withKey("foo"));
        assertThat(pref, not(withKey("bar")));
        assertThat(pref, withKey(is("foo")));
    }
}