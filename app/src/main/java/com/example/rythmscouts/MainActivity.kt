package com.example.rythmscouts

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.rythmscouts.models.NotificationItem
import com.example.rythmscouts.models.NotificationListItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MainActivity : BaseActivity() {
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "unknown-user"
    private lateinit var ivNotification: ImageView
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

    // --- Timestamp helpers ---
    fun Long.isThisWeek(): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = this@isThisWeek }
        val now = Calendar.getInstance()
        return cal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR) &&
                cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }

    fun Long.isThisMonth(): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = this@isThisMonth }
        val now = Calendar.getInstance()
        return cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
    }

    fun Long.isNextMonth(): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = this@isNextMonth }
        val now = Calendar.getInstance()
        val nextMonth = (now.get(Calendar.MONTH) + 1) % 12
        val year = if (now.get(Calendar.MONTH) == 11) now.get(Calendar.YEAR) + 1 else now.get(Calendar.YEAR)
        return cal.get(Calendar.MONTH) == nextMonth && cal.get(Calendar.YEAR) == year
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTitle = findViewById(R.id.title)
        tvTitleText = findViewById(R.id.title_text)
        ivProfilePicture = findViewById(R.id.profile_picture)

        ivNotification = findViewById(R.id.notification)
        ivNotification.setOnClickListener { showNotificationPopup() }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, homeFragment, TAG_HOME).hide(homeFragment)
                add(R.id.fragment_container, exploreFragment, TAG_EXPLORE).hide(exploreFragment)
                add(R.id.fragment_container, myEventsFragment, TAG_MY_EVENTS).hide(myEventsFragment)
                add(R.id.fragment_container, settingsFragment, TAG_SETTINGS).hide(settingsFragment)
            }.commit()
        }

        val lastTag = getLastFragmentTag()
        supportFragmentManager.executePendingTransactions()
        switchFragment(lastTag, updateNav = true)

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

    // --- Fragment switching ---
    private fun switchFragment(tag: String, updateNav: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
        listOf(TAG_HOME, TAG_EXPLORE, TAG_MY_EVENTS, TAG_SETTINGS).forEach { t ->
            supportFragmentManager.findFragmentByTag(t)?.let { transaction.hide(it) }
        }
        supportFragmentManager.findFragmentByTag(tag)?.let { transaction.show(it).commit() }
        saveCurrentFragmentTag(tag)
        if (updateNav) updateBottomNavigationSelection(tag)
        updateHeaderForSelectedTag(tag)
    }

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

    private fun updateBottomNavigationSelection(tag: String) {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener(null)
        val itemId = when (tag) {
            TAG_HOME -> R.id.navigation_home
            TAG_EXPLORE -> R.id.navigation_explore
            TAG_MY_EVENTS -> R.id.navigation_my_events
            TAG_SETTINGS -> R.id.navigation_settings
            else -> R.id.navigation_home
        }
        bottomNavigation.selectedItemId = itemId
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

    // --- Notification popup ---
    // --- Notification popup ---
    private fun showNotificationPopup() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_notifications, null)
        val rvEvents = popupView.findViewById<RecyclerView>(R.id.rvEvents)
        val tvEmpty = popupView.findViewById<TextView>(R.id.tvEmpty)

        // Event retrieval setup (using the corrected path from the previous interaction)
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val safeEmail = userEmail.replace(".", ",")
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("saved_events")
            .child(safeEmail)

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        dbRef.get().addOnSuccessListener { snapshot ->
            val eventList = mutableListOf<NotificationItem>()

            snapshot.children.forEach { child ->
                val title = child.child("name").getValue(String::class.java) ?: "Event Title"
                val subtitle = child.child("venue").getValue(String::class.java) ?: "Unknown Venue"
                val dateRaw = child.child("date_raw").getValue(String::class.java) ?: ""
                val timeRaw = child.child("time_raw").getValue(String::class.java) ?: "00:00:00"

                val eventTime = try {
                    sdf.parse("$dateRaw $timeRaw")?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }

                // Filtering: only add upcoming events
                if (eventTime >= System.currentTimeMillis()) {
                    eventList.add(NotificationItem(title, subtitle, eventTime))
                }
            }

            if (eventList.isEmpty()) {
                rvEvents.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Add events to get notifications"
            } else {
                rvEvents.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                rvEvents.layoutManager = LinearLayoutManager(this)
                val groupedItems = getGroupedNotificationItems(eventList)
                rvEvents.adapter = NotificationsGroupedAdapter(groupedItems)
            }
        }.addOnFailureListener {
            rvEvents.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Failed to load notifications"
        }

        // 🌟 FIX: RE-INTRODUCE POPUP WINDOW SETUP AND DISPLAY
        val displayMetrics = resources.displayMetrics
        val marginPx = (10 * displayMetrics.density).toInt()
        val popupWidth = displayMetrics.widthPixels - 2 * marginPx

        val popup = PopupWindow(popupView, popupWidth, LinearLayout.LayoutParams.WRAP_CONTENT, true)
        popup.elevation = 10f
        popup.isOutsideTouchable = true

        // Calculate offset to position the popup correctly next to the notification icon
        val xOffset = -(popupWidth - ivNotification.width - marginPx)
        val yOffset = 10

        // Check if the view is attached to a window before trying to display the popup
        if (ivNotification.isShown) {
            popup.showAsDropDown(ivNotification, xOffset, yOffset)
        }
    }



    private fun getUpcomingEvents(): List<NotificationListItem.EventItem> {
        val now = System.currentTimeMillis()
        return listOf(
            NotificationListItem.EventItem("Soccer Match", "2025-11-15, Stadium A", now + 2_000_000),
            NotificationListItem.EventItem("Music Concert", "2025-11-20, Hall B", now + 5_000_000)
        )
    }

    // Group events into headers
    private fun getGroupedNotificationItems(events: List<NotificationItem>): List<NotificationListItem> {
        val list = mutableListOf<NotificationListItem>()

        val thisWeek = events.filter { it.timestamp.isThisWeek() }
        val thisMonth = events.filter { it.timestamp.isThisMonth() && !it.timestamp.isThisWeek() }
        val nextMonth = events.filter { it.timestamp.isNextMonth() }

        if (thisWeek.isNotEmpty()) {
            list.add(NotificationListItem.HeaderItem("This Week"))
            thisWeek.forEach { list.add(NotificationListItem.EventItem(it.title, it.subtitle, it.timestamp)) }
        }
        if (thisMonth.isNotEmpty()) {
            list.add(NotificationListItem.HeaderItem("This Month"))
            thisMonth.forEach { list.add(NotificationListItem.EventItem(it.title, it.subtitle, it.timestamp)) }
        }
        if (nextMonth.isNotEmpty()) {
            list.add(NotificationListItem.HeaderItem("Next Month"))
            nextMonth.forEach { list.add(NotificationListItem.EventItem(it.title, it.subtitle, it.timestamp)) }
        }

        return list
    }


    inner class NotificationsGroupedAdapter(private val items: List<NotificationListItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_HEADER = 0
        private val TYPE_EVENT = 1

        inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvHeader: TextView = view.findViewById(R.id.notificationTitle)
        }

        inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.notificationTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.notificationSubtitle)
        }

        override fun getItemViewType(position: Int) = when (items[position]) {
            is NotificationListItem.HeaderItem -> TYPE_HEADER
            is NotificationListItem.EventItem -> TYPE_EVENT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_HEADER) {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification, parent, false)
                HeaderViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification, parent, false)
                EventViewHolder(view)
            }
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            if (holder is HeaderViewHolder && item is NotificationListItem.HeaderItem) {
                holder.tvHeader.text = item.title
            } else if (holder is EventViewHolder && item is NotificationListItem.EventItem) {
                holder.tvTitle.text = item.title
                holder.tvSubtitle.text = item.subtitle
            }
        }
    }
    // --- RecyclerView adapter for grouped notifications ---
    inner class GroupedNotificationAdapter(private val items: List<NotificationListItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val TYPE_HEADER = 0
        private val TYPE_EVENT = 1

        override fun getItemViewType(position: Int) = when (items[position]) {
            is NotificationListItem.HeaderItem -> TYPE_HEADER
            is NotificationListItem.EventItem -> TYPE_EVENT
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == TYPE_HEADER) {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_header, parent, false)
                HeaderViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification, parent, false)
                EventViewHolder(view)
            }
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is NotificationListItem.HeaderItem -> (holder as HeaderViewHolder).tvHeader.text = item.title
                is NotificationListItem.EventItem -> {
                    (holder as EventViewHolder).tvTitle.text = item.title
                    holder.tvSubtitle.text = item.subtitle
                }
            }
        }

        inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvHeader: TextView = view.findViewById(R.id.tvHeader)
        }

        inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.notificationTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.notificationSubtitle)
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
