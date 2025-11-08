package com.example.rythmscouts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.rythmscouts.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashTimeOut: Long = 5000 // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wait for 5 seconds then navigate to onboarding
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToOnboarding()
        }, splashTimeOut)
    }

    private fun navigateToOnboarding() {
        //val intent = Intent(this, OnboardingActivity::class.java)
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish() // Close splash activity so user can't go back
    }
}