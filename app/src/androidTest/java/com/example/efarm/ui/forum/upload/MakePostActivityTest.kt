package com.example.efarm.ui.forum.upload

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.eFarm.R
import com.example.efarm.ui.forum.CustomActionOnItemAtPositionAction
import com.example.efarm.ui.forum.HomeForumActivity
import com.example.efarm.ui.forum.HomeForumActivityTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MakePostActivityTest{
    @get:Rule
    val rule = ActivityScenarioRule(HomeForumActivity::class.java)
    @Before
    fun setUp() {
        Intents.init()
        ActivityScenario.launch(HomeForumActivity::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun uploadThread(){
        Thread.sleep(1000)
        onView(withId(R.id.fab_add))
            .perform(click())
        val title = "title"
        val thread = "thread"
        title.forEach { char ->
            onView(withId(R.id.et_title))
                .perform(ViewActions.typeText(char.toString()))
            Thread.sleep(100)
        }
        onView(withId(R.id.et_thread))
            .perform(click())
        thread.forEach { char ->
            onView(withId(R.id.et_thread))
                .perform(ViewActions.typeText(char.toString()))
            Thread.sleep(100)
        }
        onView(withId(R.id.btn_close))
            .perform(click())
        onView(withId(R.id.btn_pilih_topik))
            .perform(click())
        onView(withId(R.id.rv_commodities_topic)).perform(
            CustomActionOnItemAtPositionAction(1, click())
        )
        onView(withId(R.id.btn_close))
            .perform(click())
        onView(withId(R.id.btn_send))
            .perform(click())
        Thread.sleep(4000)
        onView(withId(R.id.rv_forum_post))
            .check(matches(isDisplayed()))
    }
}

