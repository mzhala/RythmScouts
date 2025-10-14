package com.example.rythmscouts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.rythmscouts.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
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

        binding.usernameEmailEditText.addTextChangedListener(textWatcher)
        binding.newPasswordEditText.addTextChangedListener(textWatcher)
        binding.confirmPasswordEditText.addTextChangedListener(textWatcher)
    }

    private fun setupClickListeners() {
        binding.resetPasswordButton.setOnClickListener {
            if (validateForm()) {
                performPasswordReset()
            }
        }

        binding.googleSignUpButton.setOnClickListener {
            performGoogleSignUp()
        }

        binding.signInText.setOnClickListener {
            navigateToSignIn()
        }
    }

    private fun clearErrors() {
        binding.usernameEmailInputLayout.error = null
        binding.newPasswordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate username/email
        val usernameEmail = binding.usernameEmailEditText.text.toString().trim()
        if (usernameEmail.isEmpty()) {
            binding.usernameEmailInputLayout.error = "Username or email is required"
            isValid = false
        } else if (usernameEmail.contains("@") && !isValidEmail(usernameEmail)) {
            binding.usernameEmailInputLayout.error = "Please enter a valid email"
            isValid = false
        }

        // Validate new password
        val newPassword = binding.newPasswordEditText.text.toString()
        if (newPassword.isEmpty()) {
            binding.newPasswordInputLayout.error = "New password is required"
            isValid = false
        } else if (newPassword.length < 6) {
            binding.newPasswordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        // Validate confirm password
        val confirmPassword = binding.confirmPasswordEditText.text.toString()
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (newPassword != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    private fun performPasswordReset() {
        val usernameEmail = binding.usernameEmailEditText.text.toString().trim()
        val newPassword = binding.newPasswordEditText.text.toString()

        // Show loading state
        binding.resetPasswordButton.isEnabled = false
        binding.resetPasswordButton.text = "Resetting password..."

        // Simulate API call
        binding.root.postDelayed({
            // Reset button state
            binding.resetPasswordButton.isEnabled = true
            binding.resetPasswordButton.text = "Reset Password"

            // Show success message
            Toast.makeText(this, "Password reset successfully! Please sign in with your new password.", Toast.LENGTH_LONG).show()

            // Navigate to Sign In (user must sign in again)
            navigateToSignIn()
        }, 2000)
    }

    private fun performGoogleSignUp() {
        Toast.makeText(this, "Google Sign Up clicked", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish() // Close ResetPasswordActivity
    }
}