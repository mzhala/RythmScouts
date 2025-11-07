package com.example.rythmscouts

import android.content.Intent
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
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
                binding.googleSignInButton.isEnabled = true
            }
        } else {
            // User canceled the Google Sign-In
            binding.googleSignInButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        // Fixed: Removed the newline character
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

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.signInButton.isEnabled = true
                binding.signInButton.text = "Sign In"

                if (task.isSuccessful) {
                    Toast.makeText(this, "Signed in successfully!", Toast.LENGTH_LONG).show()
                    navigateToMainActivity(email)
                } else {
                    val errorMessage = task.exception?.message ?: "Sign in failed"
                    Toast.makeText(this, "Sign in failed: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun performGoogleSignIn() {
        binding.googleSignInButton.isEnabled = false
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
                    binding.googleSignInButton.isEnabled = true
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

    private fun navigateToResetPassword() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
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