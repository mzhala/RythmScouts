package com.example.rythmscouts.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rythmscouts.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupTextWatchers() {
        // Clear errors when user starts typing
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

        // Validate username
        if (binding.usernameEditText.text.toString().trim().isEmpty()) {
            binding.usernameInputLayout.error = "Username is required"
            isValid = false
        }

        // Validate email
        val email = binding.emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            binding.emailInputLayout.error = "Please enter a valid email"
            isValid = false
        }

        // Validate password
        val password = binding.passwordEditText.text.toString()
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        // Validate confirm password
        val confirmPassword = binding.confirmPasswordEditText.text.toString()
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }

        // Validate terms
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

        // Show loading state
        binding.signUpButton.isEnabled = false
        binding.signUpButton.text = "Creating account..."

        // Simulate API call
        simulateSignUpApiCall(username, email, password)
    }

    private fun simulateSignUpApiCall(username: String, email: String, password: String) {
        // Simulate network delay
        binding.root.postDelayed({
            // Reset button state
            binding.signUpButton.isEnabled = true
            binding.signUpButton.text = "Sign Up"

            // Show success message
            Snackbar.make(binding.root, "Account created successfully!", Snackbar.LENGTH_LONG).show()

            // TODO: Navigate to next screen
            // findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
        }, 2000)
    }

    private fun performGoogleSignUp() {
        // TODO: Implement Google Sign-In
        Snackbar.make(binding.root, "Google Sign Up clicked", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToSignIn() {
        // TODO: Navigate to sign in screen
        // findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)

        // For now, just show a message
        Snackbar.make(binding.root, "Navigate to Sign In", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}