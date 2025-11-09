package com.example.rythmscouts

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "unknown-user"

    private val homeFragment = HomeFragment().apply {
        arguments = Bundle().apply { putString("USER_EMAIL", currentUserEmail) }
    }
    private val exploreFragment = ExploreFragment().apply {
        arguments = Bundle().apply { putString("USER_EMAIL", currentUserEmail) }
    }
    private val myEventsFragment = MyEventsFragment().apply {
        arguments = Bundle().apply { putString("USER_EMAIL", currentUserEmail) }
    }
    private val settingsFragment = SettingsFragment().apply {
        arguments = Bundle().apply { putString("USER_EMAIL", currentUserEmail) }
    }

    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvTitleText: TextView


    companion object {
        const val PREFS_NAME = "AppSettings"
        const val LAST_FRAGMENT_KEY = "last_fragment"
        const val TAG_HOME = "tag_home"
        const val TAG_EXPLORE = "tag_explore"
        const val TAG_MY_EVENTS = "tag_my_events"
        const val TAG_SETTINGS = "tag_settings"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize header views first
        tvTitle = findViewById(R.id.title)
        tvTitleText = findViewById(R.id.title_text)
        ivProfilePicture = findViewById(R.id.profile_picture)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Only add fragments if this is the first creation
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, homeFragment, TAG_HOME).hide(homeFragment)
                add(R.id.fragment_container, exploreFragment, TAG_EXPLORE).hide(exploreFragment)
                add(R.id.fragment_container, myEventsFragment, TAG_MY_EVENTS).hide(myEventsFragment)
                add(R.id.fragment_container, settingsFragment, TAG_SETTINGS).hide(settingsFragment)
            }.commit()
        }

        // Restore last fragment after adding fragments
        val lastTag = getLastFragmentTag()
        supportFragmentManager.executePendingTransactions() // ensure fragments are added
        switchFragment(lastTag, updateNav = true)

        // Bottom navigation listener
        bottomNavigation.setOnItemSelectedListener { item ->
            val tag = when (item.itemId) {
                R.id.navigation_home -> TAG_HOME
                R.id.navigation_explore -> TAG_EXPLORE
                R.id.navigation_my_events -> TAG_MY_EVENTS
                R.id.navigation_settings -> TAG_SETTINGS
                else -> TAG_HOME
            }
            switchFragment(tag, updateNav = true)
            true
        }
    }

    // Switch fragment safely
    private fun switchFragment(tag: String, updateNav: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragments = listOf(TAG_HOME, TAG_EXPLORE, TAG_MY_EVENTS, TAG_SETTINGS)

        fragments.forEach { t ->
            supportFragmentManager.findFragmentByTag(t)?.let { transaction.hide(it) }
        }

        supportFragmentManager.findFragmentByTag(tag)?.let {
            transaction.show(it).commit()
        }

        saveCurrentFragmentTag(tag)

        if (updateNav) updateBottomNavigationSelection(tag)
        updateHeaderForSelectedTag(tag)
    }

    // Update header text based on fragment
    private fun updateHeaderForSelectedTag(tag: String) {
        when (tag) {
            TAG_HOME -> {
                tvTitle.text = getString(R.string.title_home)
                tvTitleText.text = getString(R.string.subtitle_home)
            }
            TAG_EXPLORE -> {
                tvTitle.text = getString(R.string.title_explore)
                tvTitleText.text = getString(R.string.subtitle_explore)
            }
            TAG_MY_EVENTS -> {
                tvTitle.text = getString(R.string.title_my_events)
                tvTitleText.text = getString(R.string.subtitle_my_events)
            }
            TAG_SETTINGS -> {
                tvTitle.text = getString(R.string.title_settings)
                tvTitleText.text = getString(R.string.subtitle_settings)
            }
        }
    }

    // Highlight the correct BottomNavigation item
    private fun updateBottomNavigationSelection(tag: String) {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Temporarily remove listener to avoid recursion
        bottomNavigation.setOnItemSelectedListener(null)

        val itemId = when (tag) {
            TAG_HOME -> R.id.navigation_home
            TAG_EXPLORE -> R.id.navigation_explore
            TAG_MY_EVENTS -> R.id.navigation_my_events
            TAG_SETTINGS -> R.id.navigation_settings
            else -> R.id.navigation_home
        }

        bottomNavigation.selectedItemId = itemId

        // Reattach listener
        bottomNavigation.setOnItemSelectedListener { item ->
            val newTag = when (item.itemId) {
                R.id.navigation_home -> TAG_HOME
                R.id.navigation_explore -> TAG_EXPLORE
                R.id.navigation_my_events -> TAG_MY_EVENTS
                R.id.navigation_settings -> TAG_SETTINGS
                else -> TAG_HOME
            }
            switchFragment(newTag, updateNav = true)
            true
        }
    }


    private fun saveCurrentFragmentTag(tag: String) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(LAST_FRAGMENT_KEY, tag)
            .apply()
    }

    private fun getLastFragmentTag(): String {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LAST_FRAGMENT_KEY, TAG_HOME) ?: TAG_HOME
    }
}
