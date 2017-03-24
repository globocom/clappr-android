package io.clappr.player.app

import org.junit.Before
import org.junit.Rule
import org.junit.Test

import org.junit.runner.RunWith
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PlayerActivityTest {
    lateinit private var invalidURL: String

    @Rule @JvmField
    val mActivityRule = ActivityTestRule<PlayerActivity>(PlayerActivity::class.java)

    @Before
    fun initValidString() {
        invalidURL = "Espresso"
    }

    @Test
    fun invalidURL() {
        onView(withId(R.id.url_text))
                .perform(clearText())
                .perform(typeText(invalidURL), closeSoftKeyboard())

        onView(withId(R.id.play_button))
                .perform(click())

        onView(withText(R.string.error_dialog_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(pressBack())

        onView(withId(R.id.url_text))
                .check(matches(withText(invalidURL)))
    }

    @Test
    fun startPlaying() {
        onView(withId(R.id.player_state))
                .check(matches(withText("None")))

        onView(withId(R.id.play_button))
                .perform(click())

        onView(withId(R.id.player_state))
                .check(matches(withText("Playing")))
    }
}