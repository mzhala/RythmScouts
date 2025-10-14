package com.example.rythmscouts

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rythmscouts.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupTextWatcher()
        setupClickListeners()
    }

    private fun setupTextWatcher() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                clearErrors()
            }
        }

        binding.emailEditText.addTextChangedListener(textWatcher)
    }

    private fun setupClickListeners() {
        binding.resetPasswordButton.setOnClickListener {
            if (validateForm()) {
                performPasswordReset()
            }
        }

        binding.signInText.setOnClickListener {
            navigateToSignIn()
        }
    }

    private fun clearErrors() {
        binding.emailInputLayout.error = null
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

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    private fun performPasswordReset() {
        val email = binding.emailEditText.text.toString().trim()

        binding.resetPasswordButton.isEnabled = false
        binding.resetPasswordButton.text = "Sending reset email..."

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                binding.resetPasswordButton.isEnabled = true
                binding.resetPasswordButton.text = "Reset Password"

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent! Check your inbox.",
                        Toast.LENGTH_LONG
                    ).show()
                    navigateToSignIn()
                } else {
                    val errorMessage = task.exception?.message ?: "Failed to send reset email"
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
