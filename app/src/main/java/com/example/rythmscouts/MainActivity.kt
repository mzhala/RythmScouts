package com.example.rythmscouts

import com.example.rythmscouts.network.Event
import com.example.rythmscouts.adapter.FirebaseEvent
import com.example.rythmscouts.models.NotificationItem
import com.example.rythmscouts.adapter.NotificationsAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.widget.LinearLayout
import java.time.temporal.ChronoUnit

class MainActivity : BaseActivity() {

    private val homeFragment = HomeFragment()
    private val exploreFragment = ExploreFragment()
    private val myEventsFragment = MyEventsFragment()
    private val settingsFragment = SettingsFragment()
    private lateinit var auth: FirebaseAuth

    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvTitleText: TextView
    private var userEmail: String? = null

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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Restore last fragment
        val lastTag = getLastFragmentTag()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, homeFragment, TAG_HOME).hide(homeFragment)
                add(R.id.fragment_container, exploreFragment, TAG_EXPLORE).hide(exploreFragment)
                add(R.id.fragment_container, myEventsFragment, TAG_MY_EVENTS).hide(myEventsFragment)
                add(R.id.fragment_container, settingsFragment, TAG_SETTINGS).hide(settingsFragment)

                val fragmentToShow = when (lastTag) {
                    TAG_EXPLORE -> exploreFragment
                    TAG_MY_EVENTS -> myEventsFragment
                    TAG_SETTINGS -> settingsFragment
                    else -> homeFragment
                }
                show(fragmentToShow)
            }.commit()
        }

        // Bottom navigation setup
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            val tag = when (item.itemId) {
                R.id.navigation_home -> TAG_HOME
                R.id.navigation_explore -> TAG_EXPLORE
                R.id.navigation_my_events -> TAG_MY_EVENTS
                R.id.navigation_settings -> TAG_SETTINGS
                else -> TAG_HOME
            }
            switchFragment(tag)
            true
        }
    }

    // Efficient fragment switching without recreating
    private fun switchFragment(tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragments = listOf(TAG_HOME, TAG_EXPLORE, TAG_MY_EVENTS, TAG_SETTINGS)
        fragments.forEach { t ->
            supportFragmentManager.findFragmentByTag(t)?.let { transaction.hide(it) }
        }
        supportFragmentManager.findFragmentByTag(tag)?.let {
            transaction.show(it).commit()
            saveCurrentFragmentTag(tag)
        }
    }



    private fun updateHeaderForSelectedPage(menuItemId: Int) {
        when (menuItemId) {
            R.id.navigation_home -> {
                tvTitle.text = getString(R.string.title_home)
                tvTitleText.text = getString(R.string.subtitle_home)
            }
            R.id.navigation_explore -> {
                tvTitle.text = getString(R.string.title_explore)
                tvTitleText.text = getString(R.string.subtitle_explore)
            }
            R.id.navigation_my_events -> {
                tvTitle.text = getString(R.string.title_my_events)
                tvTitleText.text = getString(R.string.subtitle_my_events)
            }
            R.id.navigation_settings -> {
                tvTitle.text = getString(R.string.title_settings)
                tvTitleText.text = getString(R.string.subtitle_settings)
            }
        }
    }

    fun getUserEmail(): String? = userEmail

    // Save current fragment tag
    fun saveCurrentFragmentTag(tag: String) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(LAST_FRAGMENT_KEY, tag)
            .apply()
    }

    // Get last saved fragment tag
    private fun getLastFragmentTag(): String {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LAST_FRAGMENT_KEY, TAG_HOME) ?: TAG_HOME
    }

    // (Optional) Notification popup - unchanged
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationsPopup() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.popup_notifications, null)
        bottomSheetDialog.setContentView(view)

        val container = view.findViewById<LinearLayout>(R.id.notificationsContainer)
        val emptyMessage = view.findViewById<TextView>(R.id.emptyMessage)
        container.removeAllViews()

        val safeEmail = userEmail?.replace(".", ",") ?: "unknown-user"
        val dbRef = FirebaseDatabase.getInstance().getReference("saved_events").child(safeEmail)

        dbRef.get().addOnSuccessListener { snapshot ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val today = LocalDate.now()

            val events = snapshot.children.mapNotNull { it.getValue(FirebaseEvent::class.java) }
                .filter { !it.date_raw.isNullOrEmpty() }

            if (events.isEmpty()) {
                container.visibility = View.GONE
                emptyMessage.text = "Add events to get notifications!"
                emptyMessage.visibility = View.VISIBLE
                bottomSheetDialog.show()
                return@addOnSuccessListener
            }

            val grouped = events.groupBy { event ->
                val eventDate = LocalDate.parse(event.date_raw, formatter)
                val weeksDiff = ChronoUnit.WEEKS.between(today, eventDate)
                val monthsDiff = ChronoUnit.MONTHS.between(today, eventDate)

                when {
                    eventDate.isBefore(today) -> "Past"
                    weeksDiff < 1 -> "This Week"
                    monthsDiff < 1 -> "Next Month"
                    monthsDiff < 4 -> "In Few Months"
                    else -> "Later"
                }
            }

            val sortedKeys = listOf("This Week", "Next Month", "In Few Months", "Later", "Past")
            sortedKeys.forEach { key ->
                grouped[key]?.let { eventsForTimeFrame ->
                    val header = TextView(this).apply {
                        text = key
                        setTypeface(null, Typeface.BOLD)
                        setPadding(16, 16, 16, 8)
                    }
                    container.addView(header)

                    eventsForTimeFrame.forEach { event ->
                        val itemView = layoutInflater.inflate(R.layout.item_notification, container, false)
                        val titleView = itemView.findViewById<TextView>(R.id.notificationTitle)
                        val subtitleView = itemView.findViewById<TextView>(R.id.notificationSubtitle)

                        titleView.text = event.name
                        subtitleView.text = "${event.date_raw} ${event.time_raw ?: ""} @ ${event.venue ?: ""}"
                        container.addView(itemView)
                    }
                }
            }

            container.visibility = View.VISIBLE
            emptyMessage.visibility = View.GONE
            bottomSheetDialog.show()
        }.addOnFailureListener {
            container.visibility = View.GONE
            emptyMessage.text = "Failed to load notifications."
            emptyMessage.visibility = View.VISIBLE
            bottomSheetDialog.show()
        }
    }
}
