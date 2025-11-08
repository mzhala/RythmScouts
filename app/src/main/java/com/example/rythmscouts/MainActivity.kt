package com.example.rythmscouts

import com.example.rythmscouts.network.Event
import com.example.rythmscouts.adapter.FirebaseEvent
import com.example.rythmscouts.models.NotificationItem
import com.example.rythmscouts.adapter.NotificationsAdapter

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : BaseActivity() {

    private val homeFragment = HomeFragment()
    private val exploreFragment = ExploreFragment()
    private val myEventsFragment = MyEventsFragment()
    private val settingsFragment = SettingsFragment()
    private lateinit var auth: FirebaseAuth

    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvTitleText: TextView
    private lateinit var ivNotification: ImageView
    private var userEmail: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        userEmail = intent.getStringExtra("USER_EMAIL")

        ivProfilePicture = findViewById(R.id.profile_picture)
        tvTitle = findViewById(R.id.title)
        tvTitleText = findViewById(R.id.title_text)
        ivNotification = findViewById(R.id.nortification)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        replaceFragment(homeFragment)
        bottomNavigation.menu.findItem(R.id.navigation_home).isChecked = true
        updateHeaderForSelectedPage(R.id.navigation_home)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> replaceFragment(homeFragment)
                R.id.navigation_explore -> replaceFragment(exploreFragment)
                R.id.navigation_my_events -> replaceFragment(myEventsFragment)
                R.id.navigation_settings -> replaceFragment(settingsFragment)
            }
            updateHeaderForSelectedPage(item.itemId)
            true
        }

        fetchUserDetails()

        ivNotification.setOnClickListener {
            showNotificationsPopup()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("USER_EMAIL", userEmail)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
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

    private fun fetchUserDetails() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val username = snapshot.child("username").getValue(String::class.java)
            val email = snapshot.child("email").getValue(String::class.java)
        }
    }

    fun getUserEmail(): String? = userEmail

    /** Show notifications popup safely */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationsPopup() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.popup_notifications, null)
        bottomSheetDialog.setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.notificationsRecyclerView)
        val emptyMessage = view.findViewById<TextView>(R.id.emptyMessage)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val safeEmail = userEmail?.replace(".", ",") ?: "unknown-user"
        val dbRef = FirebaseDatabase.getInstance().getReference("saved_events").child(safeEmail)

        dbRef.get().addOnSuccessListener { snapshot ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val today = LocalDate.now()

            val notifications = snapshot.children.mapNotNull { it.getValue(FirebaseEvent::class.java) }
                .filter { event ->
                    event.date_raw?.let { dateStr ->
                        val eventDate = LocalDate.parse(dateStr, formatter)
                        !eventDate.isBefore(today)
                    } ?: false
                }
                .sortedWith(compareBy({ it.date_raw }, { it.time_raw })) // optional sort by time
                .map { event ->
                    NotificationItem(
                        title = event.name ?: "Unknown Event",
                        subtitle = "${event.date_raw ?: ""} ${event.time_raw ?: ""} @ ${event.venue ?: ""}"
                    )
                }

            if (notifications.isNotEmpty()) {
                recyclerView.adapter = NotificationsAdapter(notifications)
                recyclerView.visibility = View.VISIBLE
                emptyMessage.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                emptyMessage.visibility = View.VISIBLE
                emptyMessage.text = "Add events to get notifications!"
            }
        }.addOnFailureListener {
            recyclerView.visibility = View.GONE
            emptyMessage.visibility = View.VISIBLE
            emptyMessage.text = "Add events to get notifications!"
        }

        bottomSheetDialog.show()
    }


}
