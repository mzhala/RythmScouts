package com.example.rythmscouts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.rythmscouts.databinding.ActivitySignUpBinding
import com.example.rythmscouts.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

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

        binding.usernameEditText.addTextChangedListener(textWatcher)
        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
        binding.confirmPasswordEditText.addTextChangedListener(textWatcher)
    }

    private fun setupClickListeners() {
        binding.signUpButton.setOnClickListener {
            if (validateForm()) {
                performSignUp()
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
        binding.usernameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (username.isEmpty()) {
            binding.usernameInputLayout.error = "Username is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            binding.emailInputLayout.error = "Please enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }

        if (!binding.termsCheckBox.isChecked) {
            Snackbar.make(binding.root, "Please accept the terms and policy", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        return email.matches(emailPattern.toRegex())
    }

    private fun performSignUp() {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        binding.signUpButton.isEnabled = false
        binding.signUpButton.text = "Creating account..."

        // Use Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userId = firebaseUser?.uid ?: return@addOnCompleteListener

                    // Create user object (without password)
                    val user = User(
                        username = username,
                        email = email,
                        firstName = null,
                        lastName = null,
                        profilePictureUrl = null
                    )

                    // Save user data to Realtime Database
                    database.child(userId).setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
                            navigateToMainActivity(email)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save user: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Sign Up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnCompleteListener {
                binding.signUpButton.isEnabled = true
                binding.signUpButton.text = "Sign Up"
            }
    }

    private fun performGoogleSignUp() {
        Toast.makeText(this, "Google Sign Up clicked", Toast.LENGTH_SHORT).show()
        // Add Google Sign-In logic here later
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity(email: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_EMAIL", email)
        startActivity(intent)
        finish()
    }
}
