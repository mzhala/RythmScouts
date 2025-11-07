package com.example.rythmscouts

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable local persistence so data is cached and available offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
