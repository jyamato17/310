package com.example.myapplication.ui.loading_screen;

import android.util.TypedValue;
import android.view.View;

import androidx.test.rule.ActivityTestRule;

import com.example.myapplication.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class OnBoardingTest {

    @Rule
    public ActivityTestRule<OnBoarding> oActivityTestRule = new ActivityTestRule<OnBoarding>(OnBoarding.class);

    private OnBoarding oActivity = null;

    @Before
    public void setUp() throws Exception {
        oActivity = oActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch() {
        assertNotNull("OnBoarding is not available", oActivity);
//        View view = oActivity.findViewById(R.id.);
//        assertNotNull(view);
        TypedValue outValue = new TypedValue();
        oActivity.getTheme().resolveAttribute(R.attr.splashThemeName, outValue, true);
        assertEquals("SplashThemeName", outValue.string);
    }

    @After
    public void tearDown() throws Exception {
        oActivity = null;
    }
}