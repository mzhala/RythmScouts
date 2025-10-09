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
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.edit

class SettingsFragment : Fragment() {

    private var _binding: View? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var isDarkModeEnabled = false
    private var areNotificationsEnabled = true

    private val CHANNEL_ID = "notifications_channel"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = inflater.inflate(getLayoutResource(), container, false)
        return binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        initializeViews()
        setupClickListeners()
        setupSwitchListeners()
        loadCurrentSettings()
    }

    private fun getLayoutResource(): Int {
        // Replace with your actual layout ID, e.g., R.layout.fragment_settings
        return android.R.layout.simple_list_item_1
    }

    private fun initializeViews() {
        // Optional initialization logic
    }

    private fun setupClickListeners() {
        view?.findViewById<View>(getId("signOutButton"))?.setOnClickListener {
            showSignOutConfirmation()
        }
    }

    private fun setupSwitchListeners() {
        view?.findViewById<SwitchCompat>(getId("darkModeSwitch"))
            ?.setOnCheckedChangeListener { _, isChecked ->
                isDarkModeEnabled = isChecked
                sharedPreferences.edit { putBoolean("darkMode", isChecked) }
                showSnackbar("Dark mode ${if (isChecked) "enabled" else "disabled"}")
            }

        view?.findViewById<SwitchCompat>(getId("notificationsSwitch"))
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

        setSwitchChecked(getId("darkModeSwitch"), isDarkModeEnabled)
        setSwitchChecked(getId("notificationsSwitch"), areNotificationsEnabled)
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
    }

    private fun setSwitchChecked(viewId: Int, checked: Boolean) {
        view?.findViewById<SwitchCompat>(viewId)?.isChecked = checked
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    private fun getId(resourceName: String): Int {
        return View.generateViewId()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
