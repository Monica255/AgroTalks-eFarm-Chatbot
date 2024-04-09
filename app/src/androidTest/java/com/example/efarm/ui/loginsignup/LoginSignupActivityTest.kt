package com.example.efarm.ui.loginsignup

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Test
import com.example.eFarm.R
import com.example.efarm.MyApplication
import com.example.efarm.ui.SplashScreen.SplashActivity
import com.example.efarm.ui.forum.HomeForumActivity
import com.example.efarm.ui.forum.detail.DetailForumPostActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import kotlin.coroutines.coroutineContext


@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginSignupActivityTest {

    @get:Rule
    val rule = ActivityScenarioRule(LoginSignupActivity::class.java)
    @Before
    fun setUp() {
        Intents.init()
        ActivityScenario.launch(LoginSignupActivity::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun doLogin(){
        val email = "test@test.com"
        val pass = "123456"
        email.forEach { char ->
            onView(withId(R.id.et_masuk_email)).perform(typeText(char.toString()))
            Thread.sleep(100)
        }
        pass.forEach { char ->
            onView(withId(R.id.et_masuk_password)).perform(typeText(char.toString()))
            Thread.sleep(100)
        }
        onView(withId(R.id.et_masuk_password)).perform(closeSoftKeyboard())
        onView(withId(R.id.bt_masuk)).perform(click())
        Thread.sleep(4000)
        intended(hasComponent(HomeForumActivity::class.java.name))
    }
    @Test
    fun doSignUp(){
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser!=null){
            onView(withId(R.id.btn_logout)).perform(click())
            onView(withId(R.string.ya)).perform(click())
            Thread.sleep(2000)
        }
        onView(withId(R.id.bt_blm_punya_akun)).perform(click())
        val email = "test3@test.com"
        val pass = "123456"
        val name = "test3"
        val telp = "0812345678"
        email.forEach { char ->
            onView(withId(R.id.et_daftar_email)).perform(typeText(char.toString()))
            Thread.sleep(100)
        }
        name.forEach { char ->
            onView(withId(R.id.et_daftar_nama)).perform(typeText(char.toString()))
            Thread.sleep(100)
        }
        telp.forEach { char ->
            onView(withId(R.id.et_daftar_telepon)).perform(typeText(char.toString()))
            Thread.sleep(100)
        }
        pass.forEach { char ->
            onView(withId(R.id.et_daftar_password)).perform(typeText(char.toString()))
            Thread.sleep(100)
        }
        pass.forEach { char ->
            onView(withId(R.id.et_daftar_cpassword)).perform(typeText(char.toString()))
            Thread.sleep(100)
        }
        onView(withId(R.id.et_daftar_cpassword)).perform(closeSoftKeyboard())
        onView(withId(R.id.bt_daftar)).perform(click())
        Thread.sleep(4000)
        intended(hasComponent(HomeForumActivity::class.java.name))
    }


}
