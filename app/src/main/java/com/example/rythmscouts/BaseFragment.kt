package com.example.rythmscouts

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    override fun onAttach(context: Context) {
        val languageCode = LocaleHelper.getPersistedLanguage(context)
        val updatedContext = LocaleHelper.setLocale(context, languageCode)
        super.onAttach(updatedContext)
    }

    override fun getContext(): Context? {
        val context = super.getContext()
        context?.let {
            val languageCode = LocaleHelper.getPersistedLanguage(it)
            return LocaleHelper.setLocale(it, languageCode)
        }
        return context
    }

    protected fun updateFragmentContext() {
        val context = context ?: return
        val languageCode = LocaleHelper.getPersistedLanguage(context)
        val updatedContext = LocaleHelper.setLocale(context, languageCode)

        val resources = updatedContext.resources
        val configuration = resources.configuration
        val displayMetrics = resources.displayMetrics

        resources.updateConfiguration(configuration, displayMetrics)
    }

    protected fun refreshFragment() {
        // Recreate the fragment
        parentFragmentManager.beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }
}