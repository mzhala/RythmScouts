package com.example.rythmscouts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.rythmscouts.databinding.ActivitySignUpBinding
import com.example.rythmscouts.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Google Sign-In result launcher
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
                binding.googleSignUpButton.isEnabled = true
            }
        } else {
            // User canceled the Google Sign-In
            binding.googleSignUpButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getWebClientId())
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupTextWatchers()
        setupClickListeners()
    }

    private fun getWebClientId(): String {
        // Fixed: Removed extra 'y' and newline character
        return "132190124105-4vb44hee2egk8lqb256j0jm2lsisnuis.apps.googleusercontent.com"
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
                            binding.signUpButton.isEnabled = true
                            binding.signUpButton.text = "Sign Up"
                        }
                } else {
                    Toast.makeText(this, "Sign Up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    binding.signUpButton.isEnabled = true
                    binding.signUpButton.text = "Sign Up"
                }
            }
    }

    private fun performGoogleSignUp() {
        binding.googleSignUpButton.isEnabled = false
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignIn(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Check if user exists in database
                        database.child(user.uid).get().addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                val userData = dbTask.result?.getValue(User::class.java)
                                if (userData == null) {
                                    // User doesn't exist in database, create new user
                                    createGoogleUserInDatabase(user)
                                } else {
                                    // User exists, proceed to main activity
                                    Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_LONG).show()
                                    navigateToMainActivity(user.email ?: "")
                                }
                            } else {
                                // Database error, still create user
                                createGoogleUserInDatabase(user)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    binding.googleSignUpButton.isEnabled = true
                }
            }
    }

    private fun createGoogleUserInDatabase(user: com.google.firebase.auth.FirebaseUser) {
        val googleUser = User(
            username = user.displayName ?: "Google User",
            email = user.email ?: "",
            firstName = null,
            lastName = null,
            profilePictureUrl = user.photoUrl?.toString()
        )

        database.child(user.uid).setValue(googleUser)
            .addOnSuccessListener {
                Toast.makeText(this, "Google Sign-In successful!", Toast.LENGTH_LONG).show()
                navigateToMainActivity(user.email ?: "")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Google Sign-In successful! (Database save failed: ${e.message})", Toast.LENGTH_LONG).show()
                navigateToMainActivity(user.email ?: "")
            }
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