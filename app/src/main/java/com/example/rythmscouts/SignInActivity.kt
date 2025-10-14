package com.example.rythmscouts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.rythmscouts.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearErrors()
            }
        }

        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
    }

    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener {
            if (validateForm()) {
                performSignIn()
            }
        }

        binding.googleSignInButton.setOnClickListener {
            performGoogleSignIn()
        }

        binding.forgotPasswordText.setOnClickListener {
            navigateToResetPassword()
        }

        binding.signUpText.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun clearErrors() {
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val email = binding.emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            binding.emailInputLayout.error = "Please enter a valid email"
            isValid = false
        }

        val password = binding.passwordEditText.text.toString()
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    private fun performSignIn() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        binding.signInButton.isEnabled = false
        binding.signInButton.text = "Signing in..."

        // Simulate API call
        binding.root.postDelayed({
            binding.signInButton.isEnabled = true
            binding.signInButton.text = "Sign In"

            // Show success message
            Toast.makeText(this, "Signed in successfully!", Toast.LENGTH_LONG).show()

            // Navigate to Main Activity
            navigateToMainActivity()
        }, 2000)
    }

    private fun performGoogleSignIn() {
        Toast.makeText(this, "Google Sign In clicked", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToResetPassword() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
        // Don't finish() here so user can come back to sign in
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    // NEW: Navigate to Main Activity after successful sign in
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}