package com.example.efarm.ui.loginsignup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityLoginSignupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginSignupActivity : AppCompatActivity() {
    lateinit var binding:ActivityLoginSignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}