package com.example.efarm.ui.forum


import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.contrib.RecyclerViewActions

import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
//import androidx.test.espresso.contrib.RecyclerViewActions
import com.example.eFarm.R
import com.example.efarm.ui.forum.detail.DetailForumPostActivity
import com.example.efarm.ui.loginsignup.LoginSignupActivity
import com.example.efarm.ui.loginsignup.LoginSignupActivityTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeForumActivityTest {

    @get:Rule
    val rule = ActivityScenarioRule(LoginSignupActivity::class.java)
    @Before
    fun setUp() {
        Intents.init()
        ActivityScenario.launch(LoginSignupActivity::class.java)
        val email = "test@test.com"
        val pass = "123456"
        email.forEach { char ->
            onView(withId(R.id.et_masuk_email)).perform(ViewActions.typeText(char.toString()))
            Thread.sleep(50)
        }

        pass.forEach { char ->
            onView(withId(R.id.et_masuk_password)).perform(ViewActions.typeText(char.toString()))
            Thread.sleep(50)
        }
        onView(withId(R.id.et_masuk_password)).perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.bt_masuk)).perform(click())
        Thread.sleep(4000)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun loadData() {
        onView(withId(R.id.rv_forum_post))
            .check(matches(isDisplayed()))
        Thread.sleep(3000)
        onView(withId(R.id.rv_forum_post)).perform(
            CustomActionOnItemAtPositionAction(1, scrollTo())
        )
    }
    @Test
    fun filterData(){
        Thread.sleep(3000)
        onView(withId(R.id.btn_topik_post_forum))
            .check(matches(isDisplayed()))
        onView(withId(R.id.btn_topik_post_forum)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.rv_commodities_topic)).perform(
            CustomActionOnItemAtPositionAction(1, click())
        )
        Thread.sleep(3000)
        onView(withText("Jagung")).check(matches(isDisplayed()))
    }
}
