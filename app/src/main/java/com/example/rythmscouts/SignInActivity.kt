package com.example.rythmscouts

import android.widget.TextView
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.rythmscouts.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    handleGoogleSignIn(idToken)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
                binding.googleSignInButton.isEnabled = true
            }
        } else {
            binding.googleSignInButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // --- Auto-login if already signed in ---
        auth.currentUser?.let { user ->
            // User already logged in
            navigateToMainActivity(user.email ?: "")
            return
        }

        // Check if there is cached user info (offline)
        getCachedUser()?.let { (email, _) ->
            navigateToMainActivity(email)
            return
        }

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getWebClientId())
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun getWebClientId() = "132190124105-4vb44hee2egk8lqb256j0jm2lsisnuis.apps.googleusercontent.com"

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { clearErrors() }
        }

        binding.emailEditText.addTextChangedListener(textWatcher)
        binding.passwordEditText.addTextChangedListener(textWatcher)
    }

    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener { if (validateForm()) performSignIn() }
        binding.googleSignInButton.setOnClickListener { performGoogleSignIn() }
        binding.forgotPasswordText.setOnClickListener { startActivity(Intent(this, ResetPasswordActivity::class.java)) }
        binding.signUpText.setOnClickListener {
            if (isOnline()) {
                startActivity(Intent(this, SignUpActivity::class.java))
                finish()
            } else {
                showOfflineDialog()
            }
        }    }

    private fun clearErrors() {
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        if (email.isEmpty()) { binding.emailInputLayout.error = "Email is required"; isValid = false }
        else if (!email.matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex())) {
            binding.emailInputLayout.error = "Please enter a valid email"; isValid = false
        }

        if (password.isEmpty()) { binding.passwordInputLayout.error = "Password is required"; isValid = false }

        return isValid
    }

    // --- Check for internet connectivity ---
    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun performSignIn() {
        if (!isOnline()) {
            showOfflineDialog()
            return
        }

        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        binding.signInButton.isEnabled = false
        binding.signInButton.text = "Signing in..."

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            binding.signInButton.isEnabled = true
            binding.signInButton.text = "Sign In"

            if (task.isSuccessful) {
                task.result?.user?.let { saveUserLocally(it.email ?: "", it.displayName ?: "User") }
                navigateToMainActivity(email)
            } else {
                Toast.makeText(this, "Sign in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performGoogleSignIn() {
        if (!isOnline()) {
            showOfflineDialog()
            return
        }

        binding.googleSignInButton.isEnabled = false

        // 🌟 THE FIX: Sign out the GoogleSignInClient before launching the intent
        // This forces the account chooser/selection dialog to appear every time.
        googleSignInClient.signOut().addOnCompleteListener(this) {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignIn(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            binding.googleSignInButton.isEnabled = true
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    saveUserLocally(user.email ?: "", user.displayName ?: "Google User")
                    navigateToMainActivity(user.email ?: "")
                }
            } else {
                Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUserLocally(email: String, username: String) {
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        prefs.edit().apply {
            putString("email", email)
            putString("username", username)
            apply()
        }
    }

    private fun getCachedUser(): Pair<String, String>? {
        val prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE)
        val email = prefs.getString("email", null) ?: return null
        val username = prefs.getString("username", "User") ?: "User"
        return email to username
    }

    private fun navigateToMainActivity(email: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_EMAIL", email)
        startActivity(intent)
        finish()
    }

    private fun showOfflineDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Connection Error")
            .setMessage(
                "It looks like your device is not connected to the internet.\n\n" +
                        "Please check your Wi-Fi or mobile data and try again. " +
                        "If the problem persists, try restarting the app."
            )            .setPositiveButton("OK", null)
            .create()

        dialog.setOnShowListener {
            // Set background color
            dialog.window?.setBackgroundDrawableResource(android.R.color.holo_red_dark)

            // Set text color
            val titleId = dialog.context.resources.getIdentifier("alertTitle", "id", "android")
            val messageId = dialog.context.resources.getIdentifier("message", "id", "android")

            val titleTextView = dialog.findViewById<TextView>(titleId)
            titleTextView?.setTextColor(android.graphics.Color.WHITE)

            val messageTextView = dialog.findViewById<TextView>(messageId)
            messageTextView?.setTextColor(android.graphics.Color.WHITE)

            // Set button text color
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.WHITE)
        }

        dialog.show()
    }

    // --- Call this on logout ---
    private fun logout() {
        auth.signOut()
        getSharedPreferences("USER_PREFS", MODE_PRIVATE).edit().clear().apply()
    }
}
