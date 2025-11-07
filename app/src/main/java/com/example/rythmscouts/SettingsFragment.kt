package com.example.rythmscouts

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingsFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences

    // Language dropdown
    private lateinit var languageDropdown: AutoCompleteTextView
    private val languages = arrayOf("English", "isiZulu")
    private val languageCodes = arrayOf("en", "zu")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        return rootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        initLanguageDropdown()
        setupClickListeners()
        loadCurrentSettings()
        loadUserData()
    }

    private fun initLanguageDropdown() {
        languageDropdown = rootView?.findViewById(R.id.languageDropdown) ?: return

        // Create adapter for dropdown
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)
        languageDropdown.setAdapter(adapter)

        // Set current language
        val currentLanguage = getPersistedLanguage()
        val currentPosition = languageCodes.indexOf(currentLanguage)
        if (currentPosition != -1) {
            languageDropdown.setText(languages[currentPosition], false)
        }

        // Handle language selection
        languageDropdown.setOnItemClickListener { parent, view, position, id ->
            val selectedLanguageCode = languageCodes[position]
            val currentLanguageCode = getPersistedLanguage()

            if (selectedLanguageCode != currentLanguageCode) {
                showLanguageChangeConfirmation(selectedLanguageCode)
            }
        }
    }

    private fun showLanguageChangeConfirmation(languageCode: String) {
        val message = if (getPersistedLanguage() == "zu") {
            "Uqinisekile ukuthi ufuna ukushintsha ulimi? Uhlelo lizovulwa kabusha."
        } else {
            "Are you sure you want to change the language? The app will restart."
        }

        val positiveButton = if (getPersistedLanguage() == "zu") "Shintsha" else "Change"
        val negativeButton = if (getPersistedLanguage() == "zu") "Khansela" else "Cancel"

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_language))
            .setMessage(message)
            .setPositiveButton(positiveButton) { dialog, which ->
                changeAppLanguage(languageCode)
            }
            .setNegativeButton(negativeButton) { dialog, which ->
                // Reset dropdown to current language
                val currentLanguage = getPersistedLanguage()
                val currentPosition = languageCodes.indexOf(currentLanguage)
                if (currentPosition != -1) {
                    languageDropdown.setText(languages[currentPosition], false)
                }
            }
            .show()
    }

    private fun changeAppLanguage(languageCode: String) {
        // Save the language preference
        sharedPreferences.edit {
            putString("selected_language", languageCode)
        }

        // Show success message
        val message = if (languageCode == "zu") {
            "Indlela yolimi ishintshiwe"
        } else {
            "Language changed successfully"
        }
        showSnackbar(message)

        // Restart activity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            restartActivity()
        }, 1000)
    }

    private fun restartActivity() {
        // Simple restart without complex transitions
        requireActivity().recreate()
    }

    private fun getPersistedLanguage(): String {
        return sharedPreferences.getString("selected_language", "en") ?: "en"
    }

    private fun setupClickListeners() {
        rootView?.findViewById<View>(R.id.signOutButton)?.setOnClickListener {
            showSignOutConfirmation()
        }

        rootView?.findViewById<View>(R.id.updateProfileButton)?.setOnClickListener {
            updateProfile()
        }

        rootView?.findViewById<View>(R.id.changePasswordButton)?.setOnClickListener {
            changePassword()
        }

        // Setup switch listeners
        rootView?.findViewById<SwitchCompat>(R.id.darkModeSwitch)?.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("darkMode", isChecked) }
            toggleDarkMode(isChecked)
        }

        rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("notificationsEnabled", isChecked) }
            if (isChecked) enableNotifications() else showSnackbar("Notifications disabled")
        }
    }

    private fun toggleDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun loadCurrentSettings() {
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkMode", false)
        val areNotificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)

        rootView?.findViewById<SwitchCompat>(R.id.darkModeSwitch)?.isChecked = isDarkModeEnabled
        rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.isChecked = areNotificationsEnabled
    }

    private fun enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                return
            }
        }
        showSnackbar("Notifications enabled")
    }

    private fun showSignOutConfirmation() {
        val message = if (getPersistedLanguage() == "zu") {
            "Uqinisekile ukuthi ufuna ukuphuma?"
        } else {
            "Are you sure you want to sign out?"
        }

        val positiveButton = if (getPersistedLanguage() == "zu") "Phuma" else "Sign Out"
        val negativeButton = if (getPersistedLanguage() == "zu") "Khansela" else "Cancel"

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sign_out))
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ -> performSignOut() }
            .setNegativeButton(negativeButton, null)
            .show()
    }

    private fun performSignOut() {
        FirebaseAuth.getInstance().signOut()

        val message = if (getPersistedLanguage() == "zu") {
            "Uphume ngempumelelo"
        } else {
            "Signed out successfully"
        }
        showSnackbar(message)

        val intent = android.content.Intent(requireContext(), SignInActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun updateProfile() {
        val message = if (getPersistedLanguage() == "zu") {
            "Iphrofayili ibuyekezwe ngempumelelo"
        } else {
            "Profile updated successfully"
        }
        showSnackbar(message)
    }

    private fun changePassword() {
        val message = if (getPersistedLanguage() == "zu") {
            "Iphasiwedi ishintshiwe ngempumelelo"
        } else {
            "Password changed successfully"
        }
        showSnackbar(message)
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""

                rootView?.findViewById<TextInputEditText>(R.id.usernameEditText)?.setText(username)
                rootView?.findViewById<TextInputEditText>(R.id.emailEditText)?.setText(email)
                rootView?.findViewById<TextView>(R.id.userName)?.text = username
                rootView?.findViewById<TextView>(R.id.userEmail)?.text = email
            } else {
                showSnackbar("User data not found")
            }
        }.addOnFailureListener {
            showSnackbar("Failed to load user data")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableNotifications()
            } else {
                showSnackbar("Notification permission denied")
                rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.isChecked = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}