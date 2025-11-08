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
    private var isDarkModeEnabled = false
    private var areNotificationsEnabled = true

    private lateinit var languageDropdown: AutoCompleteTextView
    private val languageCodes = arrayOf("en", "zu", "tn")

    private val languageNames: Array<String>
        get() = when (getPersistedLanguage()) {
            "zu" -> arrayOf("isiNgisi", "isiZulu", "Xitsonga")
            "tn" -> arrayOf("Xinghezi", "isiZulu", "Xitsonga")
            else -> arrayOf("English", "isiZulu", "Xitsonga")
        }

    private val CHANNEL_ID = "notifications_channel"

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
        setupSwitchListeners()
        setupClickListeners()
        loadCurrentSettings()
        loadUserData()
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""

                // Populate editable fields
                rootView?.findViewById<TextInputEditText>(R.id.firstNameEditText)?.setText(firstName)
                rootView?.findViewById<TextInputEditText>(R.id.lastNameEditText)?.setText(lastName)
                rootView?.findViewById<TextInputEditText>(R.id.usernameEditText)?.setText(username)
                rootView?.findViewById<TextInputEditText>(R.id.emailEditText)?.setText(email)

                // Determine what to display in the profile header
                val displayName = when {
                    firstName.isNotEmpty() || lastName.isNotEmpty() -> "$firstName $lastName".trim()
                    username.isNotEmpty() -> username
                    else -> "User"
                }

                // Update profile header
                rootView?.findViewById<TextView>(R.id.userName)?.text = displayName
                rootView?.findViewById<TextView>(R.id.userEmail)?.text = email
            } else {
                showSnackbar("User data not found")
            }
        }.addOnFailureListener {
            showSnackbar("Failed to load user data")
        }
    }

    private fun updateProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        val firstName = rootView?.findViewById<TextInputEditText>(R.id.firstNameEditText)?.text.toString().trim()
        val lastName = rootView?.findViewById<TextInputEditText>(R.id.lastNameEditText)?.text.toString().trim()
        val username = rootView?.findViewById<TextInputEditText>(R.id.usernameEditText)?.text.toString().trim()

        if (username.isEmpty()) {
            showSnackbar("Please enter a username")
            return
        }

        val updates = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username
        )

        dbRef.updateChildren(updates).addOnSuccessListener {
            // Decide what to show in the header
            val displayName = when {
                firstName.isNotEmpty() || lastName.isNotEmpty() -> "$firstName $lastName".trim()
                username.isNotEmpty() -> username
                else -> "User"
            }

            rootView?.findViewById<TextView>(R.id.userName)?.text = displayName

            val message = when (getPersistedLanguage()) {
                "zu" -> "Iphrofayili ibuyekezwe ngempumelelo"
                "tn" -> "Profayili yi antswuxiwile hi ku humelela"
                else -> "Profile updated successfully"
            }
            showPopup(message)
        }.addOnFailureListener {
            val message = when (getPersistedLanguage()) {
                "zu" -> "Kwehlulekile ukubuyekeza iphrofayili"
                "tn" -> "Ku tsandzekile ku antswuxa profayili"
                else -> "Failed to update profile"
            }
            showPopup(message)
        }
    }

    private fun initLanguageDropdown() {
        languageDropdown = rootView?.findViewById(R.id.languageDropdown) ?: return
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languageNames)
        languageDropdown.setAdapter(adapter)

        val currentLanguage = getPersistedLanguage()
        val currentPosition = languageCodes.indexOf(currentLanguage)
        if (currentPosition != -1) {
            languageDropdown.setText(languageNames[currentPosition], false)
        }

        languageDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguageCode = languageCodes[position]
            val currentLanguageCode = getPersistedLanguage()
            if (selectedLanguageCode != currentLanguageCode) {
                showLanguageChangeConfirmation(selectedLanguageCode)
            }
        }
    }

    private fun showLanguageChangeConfirmation(languageCode: String) {
        val currentLanguage = getPersistedLanguage()
        val (message, positiveButton, negativeButton) = when (currentLanguage) {
            "zu" -> Triple("Uqinisekile ukuthi ufuna ukushintsha ulimi? Uhlelo lizovulwa kabusha.", "Shintsha", "Khansela")
            "tn" -> Triple("U na xiximi lexi u lava ku cinca ririmi? App yi ta sungula nakambe.", "Cinca", "Kansela")
            else -> Triple("Are you sure you want to change the language? The app will restart.", "Change", "Cancel")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_language))
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ ->
                changeAppLanguage(languageCode)
            }
            .setNegativeButton(negativeButton, null)
            .show()
    }

    private fun changeAppLanguage(languageCode: String) {
        sharedPreferences.edit { putString("selected_language", languageCode) }

        val message = when (languageCode) {
            "zu" -> "Indlela yolimi ishintshiwe"
            "tn" -> "Ririmi ri cincile hi ku humelela"
            else -> "Language changed successfully"
        }
        showSnackbar(message)

        Handler(Looper.getMainLooper()).postDelayed({
            requireActivity().recreate()
        }, 1000)
    }

    private fun getPersistedLanguage(): String {
        return sharedPreferences.getString("selected_language", "en") ?: "en"
    }

    private fun setupSwitchListeners() {
        rootView?.findViewById<SwitchCompat>(R.id.darkModeSwitch)
            ?.setOnCheckedChangeListener { _, isChecked ->
                isDarkModeEnabled = isChecked
                sharedPreferences.edit { putBoolean("darkMode", isChecked) }
                toggleDarkMode(isChecked)
                val msg = if (isChecked) "Dark mode enabled" else "Dark mode disabled"
                showSnackbar(msg)
            }

        rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)
            ?.setOnCheckedChangeListener { _, isChecked ->
                areNotificationsEnabled = isChecked
                sharedPreferences.edit { putBoolean("notificationsEnabled", isChecked) }
                if (isChecked) enableNotifications() else showSnackbar("Notifications disabled")
            }
    }

    private fun toggleDarkMode(isDarkMode: Boolean) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode)
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "App Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        showSnackbar("Notifications enabled")
    }

    private fun setupClickListeners() {
        rootView?.findViewById<View>(R.id.signOutButton)?.setOnClickListener { showSignOutConfirmation() }
        rootView?.findViewById<View>(R.id.updateProfileButton)?.setOnClickListener { updateProfile() }
        rootView?.findViewById<View>(R.id.changePasswordButton)?.setOnClickListener { changePassword() }
    }

    private fun showSignOutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ -> performSignOut() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performSignOut() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Clear locally cached user info
        requireContext().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        showSnackbar("Signed out successfully")

        // Navigate to SignInActivity and clear back stack
        val intent = android.content.Intent(requireContext(), SignInActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    private fun changePassword() {
        showPopup("Password changed successfully")
    }

    private fun loadCurrentSettings() {
        isDarkModeEnabled = sharedPreferences.getBoolean("darkMode", false)
        areNotificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)
        rootView?.findViewById<SwitchCompat>(R.id.darkModeSwitch)?.isChecked = isDarkModeEnabled
        rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.isChecked = areNotificationsEnabled
        toggleDarkMode(isDarkModeEnabled)
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    private fun showPopup(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            enableNotifications()
        } else {
            showSnackbar("Notification permission denied")
            rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.isChecked = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}
