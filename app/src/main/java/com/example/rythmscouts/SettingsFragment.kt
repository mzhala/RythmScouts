package com.example.rythmscouts

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class SettingsFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var currentPasswordEditText: TextInputEditText
    private lateinit var newPasswordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText

    private var isDarkModeEnabled = false
    private var areNotificationsEnabled = true

    private val CHANNEL_ID = "notifications_channel"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        return rootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Initialize UI elements
        firstNameEditText = view.findViewById(R.id.firstNameEditText)
        lastNameEditText = view.findViewById(R.id.lastNameEditText)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)

        setupSwitchListeners()
        setupClickListeners()
        loadCurrentSettings()
        loadUserInfo()
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
    }

    private fun setupSwitchListeners() {
        rootView?.findViewById<SwitchCompat>(R.id.darkModeSwitch)
            ?.setOnCheckedChangeListener { _, isChecked ->
                isDarkModeEnabled = isChecked
                sharedPreferences.edit { putBoolean("darkMode", isChecked) }
                showSnackbar("Dark mode ${if (isChecked) "enabled" else "disabled"}")
            }

        rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)
            ?.setOnCheckedChangeListener { _, isChecked ->
                areNotificationsEnabled = isChecked
                sharedPreferences.edit { putBoolean("notificationsEnabled", isChecked) }

                if (isChecked) {
                    enableNotifications()
                } else {
                    showSnackbar("Notifications disabled")
                }
            }
    }

    private fun loadCurrentSettings() {
        isDarkModeEnabled = sharedPreferences.getBoolean("darkMode", false)
        areNotificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)

        rootView?.findViewById<SwitchCompat>(R.id.darkModeSwitch)?.isChecked = isDarkModeEnabled
        rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.isChecked = areNotificationsEnabled
    }

    private fun loadUserInfo() {
        firstNameEditText.setText(sharedPreferences.getString("firstName", ""))
        lastNameEditText.setText(sharedPreferences.getString("lastName", ""))
        usernameEditText.setText(sharedPreferences.getString("username", ""))
    }

    private fun updateProfile() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()) {
            showSnackbar("All fields are required.")
            return
        }

        sharedPreferences.edit {
            putString("firstName", firstName)
            putString("lastName", lastName)
            putString("username", username)
        }

        showSnackbar("Profile updated successfully!")
    }

    private fun changePassword() {
        val currentPassword = currentPasswordEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        val savedPassword = sharedPreferences.getString("password", "")

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showSnackbar("All fields are required.")
            return
        }

        if (currentPassword != savedPassword) {
            showSnackbar("Current password is incorrect.")
            return
        }

        if (newPassword != confirmPassword) {
            showSnackbar("New passwords do not match.")
            return
        }

        sharedPreferences.edit { putString("password", newPassword) }
        showSnackbar("Password changed successfully!")
        clearPasswordFields()
    }

    private fun clearPasswordFields() {
        currentPasswordEditText.text?.clear()
        newPasswordEditText.text?.clear()
        confirmPasswordEditText.text?.clear()
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

        createNotificationChannel()
        showSnackbar("Notifications enabled")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Notifications"
            val descriptionText = "General notifications for RhythmScout"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSignOutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                performSignOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performSignOut() {
        showSnackbar("Signed out successfully")
        // Add sign-out logic here (e.g., clear tokens or navigate to login)
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
