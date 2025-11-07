package com.example.rythmscouts

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getPersistedLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language before super.onCreate to avoid the IllegalStateException
        LocaleHelper.updateConfiguration(this)
        super.onCreate(savedInstanceState)
    }
}