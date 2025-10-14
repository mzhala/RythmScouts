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
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class SettingsFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var isDarkModeEnabled = false
    private var areNotificationsEnabled = true

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

        sharedPreferences =
            requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        setupSwitchListeners()
        setupClickListeners()
        loadCurrentSettings()
        loadUserData()
    }

    private fun setupClickListeners() {
        rootView?.findViewById<View>(R.id.signOutButton)?.setOnClickListener {
            showSignOutConfirmation()
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

                if (isChecked) enableNotifications()
                else showSnackbar("Notifications disabled")
            }
    }

    private fun loadCurrentSettings() {
        isDarkModeEnabled = sharedPreferences.getBoolean("darkMode", false)
        areNotificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)

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

        showSnackbar("Notifications enabled")
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
        showSnackbar("Signed out successfully")
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    private fun loadUserData() {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
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


    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}
