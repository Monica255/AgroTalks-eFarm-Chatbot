package com.example.efarm.ui.SplashScreen

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivitySplashBinding
import com.example.efarm.ui.forum.HomeForumActivity
import com.example.efarm.ui.loginsignup.LoginSignupActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        FirebaseAuth.getInstance().signOut()
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val intent: Intent = if (viewModel.currentUser != null) {
                    Intent(this, HomeForumActivity::class.java)
                } else {
                    Intent(this, LoginSignupActivity::class.java)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.d("TAG", e.message.toString())
            }
        }, DELAY_TIME)
    }


    companion object {
        const val DELAY_TIME: Long = 2_000
    }
}