package com.example.efarm.ui.forum.detail

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.eFarm.R
import com.example.efarm.ui.forum.CustomActionOnItemAtPositionAction
import com.example.efarm.ui.loginsignup.LoginSignupActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class DetailForumPostActivityTest{
    @get:Rule
    val rule = ActivityScenarioRule(LoginSignupActivity::class.java)
    @Before
    fun setUp() {
        Intents.init()
        ActivityScenario.launch(LoginSignupActivity::class.java)
        val email = "test@test.com"
        val pass = "123456"
        email.forEach { char ->
            onView(withId(R.id.et_masuk_email)).perform(typeText(char.toString()))
            Thread.sleep(50)
        }

        pass.forEach { char ->
            onView(withId(R.id.et_masuk_password)).perform(typeText(char.toString()))
            Thread.sleep(50)
        }
        onView(withId(R.id.et_masuk_password)).perform(closeSoftKeyboard())
        onView(withId(R.id.bt_masuk)).perform(click())
        Thread.sleep(4000)
    }

    @After
    fun tearDown() {
        Intents.release()
    }
    @Test
    fun writeComment(){
        Thread.sleep(3000)
        onView(withId(R.id.rv_forum_post)).perform(
            CustomActionOnItemAtPositionAction(1, click())
        )
        Thread.sleep(3000)
        val comment = "nice"
        comment.forEach { char ->
            onView(withId(R.id.et_komentar)).perform(typeText(char.toString()))
            Thread.sleep(100)
        }
        onView(withId(R.id.et_komentar)).perform(closeSoftKeyboard())
        Thread.sleep(1000)
        onView(withId(R.id.btn_send))
            .check(matches(isDisplayed()))
        onView(withId(R.id.btn_send)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.rv_komentar)).perform(
            CustomActionOnItemAtPositionAction(0, scrollTo())
        )
        onView(withText("nice")).check(matches(isDisplayed()))
    }
    @Test
    fun loadDetail() {
        Thread.sleep(3000)
        onView(withId(R.id.rv_forum_post)).perform(
            CustomActionOnItemAtPositionAction(1, click())
        )
        Thread.sleep(3000)
        Intents.intended(IntentMatchers.hasComponent(DetailForumPostActivity::class.java.name))
        onView(withId(R.id.tv_content_post))
            .check(matches(isDisplayed()))
        onView(withId(R.id.tv_post_title))
            .check(matches(isDisplayed()))
        onView(withId(R.id.tv_label_topic))
            .check(matches(isDisplayed()))
    }
}