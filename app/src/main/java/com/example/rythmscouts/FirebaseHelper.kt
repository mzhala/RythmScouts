package com.example.rythmscouts

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    private val database = FirebaseDatabase.getInstance().apply {
        setPersistenceEnabled(true) // globally enable offline persistence
    }

    val usersRef: DatabaseReference = database.getReference("users").apply {
        keepSynced(true)
    }

    val eventsRef: DatabaseReference = database.getReference("events").apply {
        keepSynced(true)
    }

    // Keep track of which user refs are already synced
    private val syncedUsers = mutableSetOf<String>()

    fun savedEventsRef(username: String): DatabaseReference {
        val safeUsername = username.replace(".", ",")
        val ref = database.getReference("saved_events").child(safeUsername)
        if (syncedUsers.add(safeUsername)) {
            ref.keepSynced(true) // only call once per user
        }
        return ref
    }
}

