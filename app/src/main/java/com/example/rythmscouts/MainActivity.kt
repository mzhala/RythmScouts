package com.example.rythmscouts

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val exploreFragment = ExploreFragment()
    private val myEventsFragment = MyEventsFragment()
    private val settingsFragment = SettingsFragment()
    private lateinit var auth: FirebaseAuth

    // Header views
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvTitleText: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Initialize header views
        ivProfilePicture = findViewById(R.id.profile_picture)
        tvTitle = findViewById(R.id.title)
        tvTitleText = findViewById(R.id.title_text)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set default fragment and header
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
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Update header based on selected navigation
    private fun updateHeaderForSelectedPage(menuItemId: Int) {
        when (menuItemId) {
            R.id.navigation_home -> {
                tvTitle.text = "Rhythm Scout"
                tvTitleText.text = "Discover amazing music events near you"
                // optionally update profile picture here if needed
            }
            R.id.navigation_explore -> {
                tvTitle.text = "Explore Events"
                tvTitleText.text = "Find events by genre, date, or location"
            }
            R.id.navigation_my_events -> {
                tvTitle.text = "My Events"
                tvTitleText.text = "Your saved events"
            }
            R.id.navigation_settings -> {
                tvTitle.text = "Settings"
                tvTitleText.text = "Manage your account and preferences"
            }
        }
    }

    // Optional: If you still want to fetch user info for profile picture
    private fun fetchUserDetails() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)

                // Example: set profile picture using a placeholder if you have it
                // ivProfilePicture.setImageResource(R.drawable.profile_placeholder)
                // You could also load an image URL with Glide/Picasso
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
