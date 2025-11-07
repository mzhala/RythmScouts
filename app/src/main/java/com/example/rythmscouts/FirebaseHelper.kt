package com.example.rythmscouts

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    private val database = FirebaseDatabase.getInstance().apply {
        // Enable persistence only once globally
        setPersistenceEnabled(true)
    }

    val usersRef: DatabaseReference = database.getReference("users").apply {
        keepSynced(true)
    }

    val eventsRef: DatabaseReference = database.getReference("events").apply {
        keepSynced(true)
    }

    // Function to get a reference to saved events for a specific user
    fun savedEventsRef(username: String): DatabaseReference {
        val ref = database.getReference("savedEvents").child(username)
        ref.keepSynced(true)
        return ref
    }
}
