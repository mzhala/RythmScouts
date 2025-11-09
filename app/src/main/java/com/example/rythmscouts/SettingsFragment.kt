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
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingsFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var sharedPreferences: android.content.SharedPreferences
    private var isDarkModeEnabled = false
    private var areNotificationsEnabled = true

    private lateinit var languageDropdown: AutoCompleteTextView
    private val CHANNEL_ID = "notifications_channel"
    private lateinit var profileImageView: ImageView

    // --- Language ---
    private val languageCodes = arrayOf("en", "zu", "tn")
    private val languageNames = arrayOf("English", "isiZulu", "Setswana")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        return rootView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        profileImageView = rootView?.findViewById(R.id.profileImage)!!

        rootView?.findViewById<Button>(R.id.changePhotoButton)?.setOnClickListener { openProfilePicker() }

        loadCurrentSettings()
        initLanguageDropdown()
        setupSwitchListeners()
        setupClickListeners()
        loadUserData()
        loadUserProfileImage()
    }

    // --- User Data ---
    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                val username = snapshot.child("username").getValue(String::class.java) ?: ""
                val email = snapshot.child("email").getValue(String::class.java) ?: ""

                rootView?.findViewById<TextInputEditText>(R.id.firstNameEditText)?.setText(firstName)
                rootView?.findViewById<TextInputEditText>(R.id.lastNameEditText)?.setText(lastName)
                rootView?.findViewById<TextInputEditText>(R.id.usernameEditText)?.setText(username)
                rootView?.findViewById<TextInputEditText>(R.id.emailEditText)?.setText(email)

                val displayName = when {
                    firstName.isNotEmpty() || lastName.isNotEmpty() -> "$firstName $lastName".trim()
                    username.isNotEmpty() -> username
                    else -> getString(R.string.user)
                }

                rootView?.findViewById<TextView>(R.id.userName)?.text = displayName
                rootView?.findViewById<TextView>(R.id.userEmail)?.text = email
            } else {
                showPopup(getString(R.string.user_data_not_found))
            }
        }.addOnFailureListener {
            showPopup(getString(R.string.failed_load_user_data))
        }
    }

    private fun updateProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        val firstName = rootView?.findViewById<TextInputEditText>(R.id.firstNameEditText)?.text.toString().trim()
        val lastName = rootView?.findViewById<TextInputEditText>(R.id.lastNameEditText)?.text.toString().trim()
        val username = rootView?.findViewById<TextInputEditText>(R.id.usernameEditText)?.text.toString().trim()

        if (username.isEmpty()) {
            showPopup(getString(R.string.enter_username))
            return
        }

        val updates = mapOf("firstName" to firstName, "lastName" to lastName, "username" to username)
        dbRef.updateChildren(updates).addOnSuccessListener {
            val displayName = when {
                firstName.isNotEmpty() || lastName.isNotEmpty() -> "$firstName $lastName".trim()
                username.isNotEmpty() -> username
                else -> getString(R.string.user)
            }
            rootView?.findViewById<TextView>(R.id.userName)?.text = displayName

            val message = when (getPersistedLanguage()) {
                "zu" -> getString(R.string.profile_updated_zu)
                "tn" -> getString(R.string.profile_updated_tn)
                else -> getString(R.string.profile_updated_en)
            }
            showPopup(message)
        }.addOnFailureListener {
            val message = when (getPersistedLanguage()) {
                "zu" -> getString(R.string.profile_update_failed_zu)
                "tn" -> getString(R.string.profile_update_failed_tn)
                else -> getString(R.string.profile_update_failed_en)
            }
            showPopup(message)
        }
    }

    // --- Language ---
    // --- Language ---
    private fun initLanguageDropdown() {
        languageDropdown = rootView?.findViewById(R.id.languageDropdown) ?: return

        // Fixed adapter with all three languages
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languageNames)
        languageDropdown.setAdapter(adapter)

        // 💡 **CRITICAL FIX 1: Prevent filtering for dropdown use**
        // By setting the threshold very high, no text will ever reach it,
        // and the dropdown will show all items when clicked.
        languageDropdown.threshold = 100

        // Set dropdown text to currently stored language
        val currentIndex = languageCodes.indexOf(getPersistedLanguage())
        if (currentIndex != -1) {
            // Set text without applying a filter
            languageDropdown.setText(languageNames[currentIndex], false)
        }

        languageDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedCode = languageCodes[position]
            if (selectedCode != getPersistedLanguage()) {
                showLanguageChangeConfirmation(selectedCode)
            }
        }
    }


    private fun showLanguageChangeConfirmation(languageCode: String) {
        val currentLanguage = getPersistedLanguage()
        val (message, positiveButton, negativeButton) = when (currentLanguage) {
            "zu" -> Triple(getString(R.string.language_change_confirm_zu), getString(R.string.change_zu), getString(R.string.cancel_zu))
            "tn" -> Triple(getString(R.string.language_change_confirm_tn), getString(R.string.change_tn), getString(R.string.cancel_tn))
            else -> Triple(getString(R.string.language_change_confirm_en), getString(R.string.change_en), getString(R.string.cancel_en))
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_language))
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ -> changeAppLanguage(languageCode) }
            .setNegativeButton(negativeButton) { _, _ ->
                // Reset dropdown text to currently stored language
                val resetIndex = languageCodes.indexOf(getPersistedLanguage())
                if (resetIndex != -1) {
                    languageDropdown.setText(languageNames[resetIndex], false)
                }
            }
            .show()
    }

    private fun changeAppLanguage(languageCode: String) {
        sharedPreferences.edit {
            putString("selected_language", languageCode)
            putString(MainActivity.LAST_FRAGMENT_KEY, MainActivity.TAG_SETTINGS)
        }
        showPopup(getString(R.string.language_changed)) {
            requireActivity().recreate()
        }
    }

    private fun getPersistedLanguage(): String = sharedPreferences.getString("selected_language", "en") ?: "en"

    // --- Dark mode & notifications ---
    private fun loadCurrentSettings() {
        isDarkModeEnabled = sharedPreferences.getBoolean("darkMode", false)
        areNotificationsEnabled = sharedPreferences.getBoolean("notificationsEnabled", true)

        rootView?.findViewById<SwitchCompat>(R.id.darkModeSwitch)?.apply {
            setOnCheckedChangeListener(null)
            isChecked = isDarkModeEnabled
        }

        rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.apply {
            setOnCheckedChangeListener(null)
            isChecked = areNotificationsEnabled
        }
    }

    private fun setupSwitchListeners() {
        rootView?.findViewById<SwitchCompat>(R.id.darkModeSwitch)?.setOnCheckedChangeListener { _, isChecked ->
            if (isDarkModeEnabled != isChecked) {
                isDarkModeEnabled = isChecked
                sharedPreferences.edit { putBoolean("darkMode", isChecked) }
                toggleDarkMode(isChecked)
            }
        }

        rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.setOnCheckedChangeListener { _, isChecked ->
            areNotificationsEnabled = isChecked
            sharedPreferences.edit { putBoolean("notificationsEnabled", isChecked) }
            if (isChecked) enableNotifications() else showPopup(getString(R.string.notifications_disabled))
        }
    }

    private fun toggleDarkMode(isDarkMode: Boolean) {
        val mode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "App Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        showPopup(getString(R.string.notifications_enabled))
    }

    // --- Buttons ---
    private fun setupClickListeners() {
        rootView?.findViewById<View>(R.id.signOutButton)?.setOnClickListener { showSignOutConfirmation() }
        rootView?.findViewById<View>(R.id.updateProfileButton)?.setOnClickListener { updateProfile() }
        rootView?.findViewById<View>(R.id.changePasswordButton)?.setOnClickListener { changePassword() }
    }

    private fun showSignOutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sign_out))
            .setMessage(getString(R.string.sign_out_confirm))
            .setPositiveButton(getString(R.string.sign_out)) { _, _ -> performSignOut() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun performSignOut() {
        FirebaseAuth.getInstance().signOut()
        requireContext().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE).edit().clear().apply()
        showPopup(getString(R.string.signed_out)) {
            val intent = android.content.Intent(requireContext(), SignInActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun changePassword() {
        showPopup(getString(R.string.password_changed))
    }

    // --- Avatar picker ---
    private fun openProfilePicker() {
        val profileImages = arrayOf(
            R.drawable.profile_1, R.drawable.profile_2, R.drawable.profile_3,
            R.drawable.profile_4, R.drawable.profile_5, R.drawable.profile_6
        )

        val gridView = GridView(requireContext()).apply {
            numColumns = 3
            adapter = object : BaseAdapter() {
                override fun getCount() = profileImages.size
                override fun getItem(position: Int) = profileImages[position]
                override fun getItemId(position: Int) = position.toLong()
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val imageView = convertView as? ImageView ?: LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false) as ImageView
                    imageView.setImageResource(profileImages[position])
                    return imageView
                }
            }
            setPadding(16, 16, 16, 16)
            horizontalSpacing = 16
            verticalSpacing = 16
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_profile_picture))
            .setView(gridView)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
        dialog.show()

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedImageRes = profileImages[position]
            profileImageView.setImageResource(selectedImageRes)
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnItemClickListener
            FirebaseDatabase.getInstance().getReference("users").child(userId).child("profileImageResId").setValue(selectedImageRes)
            dialog.dismiss()
        }
    }

    private fun loadUserProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("profileImageResId").get().addOnSuccessListener { snapshot ->
            val resId = snapshot.getValue(Int::class.java)
            profileImageView.setImageResource(resId ?: R.drawable.ic_default_profile_picture)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            enableNotifications()
        } else {
            showPopup(getString(R.string.notification_permission_denied))
            rootView?.findViewById<SwitchCompat>(R.id.notificationsSwitch)?.isChecked = false
        }
    }

    private fun showPopup(message: String, onOk: (() -> Unit)? = null) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> onOk?.invoke() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rootView = null
    }
}
