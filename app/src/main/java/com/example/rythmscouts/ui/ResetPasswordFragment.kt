package com.example.rythmscouts.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rythmscouts.databinding.FragmentResetPasswordBinding
import com.google.android.material.snackbar.Snackbar

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
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
        simulateResetPasswordApiCall(usernameEmail, newPassword)
    }

    private fun simulateResetPasswordApiCall(usernameEmail: String, newPassword: String) {
        // Simulate network delay
        binding.root.postDelayed({
            // Reset button state
            binding.resetPasswordButton.isEnabled = true
            binding.resetPasswordButton.text = "Reset Password"

            // Show success message
            Snackbar.make(binding.root, "Password reset successfully!", Snackbar.LENGTH_LONG).show()

            // TODO: Navigate to login screen or home screen
            // findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
        }, 2000)
    }

    private fun performGoogleSignUp() {
        // TODO: Implement Google Sign-Up
        Snackbar.make(binding.root, "Google Sign Up clicked", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToSignIn() {
        // TODO: Navigate to sign in screen
        // findNavController().navigate(R.id.action_resetPasswordFragment_to_signInFragment)

        // For now, just show a message
        Snackbar.make(binding.root, "Navigate to Sign In", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}