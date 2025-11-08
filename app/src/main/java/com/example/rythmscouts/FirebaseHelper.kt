package com.example.rythmscouts

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    private val database = FirebaseDatabase.getInstance() // already persisted in MyApp

    val usersRef: DatabaseReference = database.getReference("users").apply {
        keepSynced(true) // global offline cache
    }

    val eventsRef: DatabaseReference = database.getReference("events").apply {
        keepSynced(true) // global offline cache
    }

    private val syncedUsers = mutableSetOf<String>()

    fun savedEventsRef(username: String): DatabaseReference {
        val safeUsername = username.replace(".", ",")
        val ref = database.getReference("saved_events").child(safeUsername)
        if (syncedUsers.add(safeUsername)) {
            ref.keepSynced(true) // enable offline only once per user
        }
        return ref
    }
}
