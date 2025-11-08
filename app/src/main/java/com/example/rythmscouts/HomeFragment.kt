package com.example.rythmscouts

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.rythmscouts.adapter.EventAdapter
import com.example.rythmscouts.network.Event
import com.example.rythmscouts.network.RetrofitClient
import com.example.rythmscouts.network.TicketmasterApi
import com.example.rythmscouts.network.TicketmasterResponse
import com.google.firebase.database.FirebaseDatabase
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : BaseFragment() {

    private lateinit var todayViewPager: ViewPager2
    private lateinit var endingSoonViewPager: ViewPager2
    private lateinit var todayDots: WormDotsIndicator
    private lateinit var endingSoonDots: WormDotsIndicator
    private lateinit var progressBar: ProgressBar
    private lateinit var todayLabel: TextView
    private lateinit var endingSoonLabel: TextView

    private lateinit var todayAdapter: EventAdapter
    private lateinit var endingSoonAdapter: EventAdapter

    private val apiKey = "1A1i0hOsTyaaIstcAYpvNAmKjd84Kq3o"
    private val userEmail = "testing-user" // replace with dynamic email if needed

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        todayViewPager = view.findViewById(R.id.todayViewPager)
        endingSoonViewPager = view.findViewById(R.id.endingSoonViewPager)
        todayDots = view.findViewById(R.id.todayDots)
        endingSoonDots = view.findViewById(R.id.endingSoonDots)
        progressBar = view.findViewById(R.id.homeProgressBar)
        todayLabel = view.findViewById(R.id.todayLabel)
        endingSoonLabel = view.findViewById(R.id.endingSoonLabel)

        todayAdapter = EventAdapter(emptyList(), username = userEmail, isHomePage = true)
        endingSoonAdapter = EventAdapter(emptyList(), username = userEmail, isHomePage = true)
        todayViewPager.adapter = todayAdapter
        endingSoonViewPager.adapter = endingSoonAdapter

        todayDots.attachTo(todayViewPager)
        endingSoonDots.attachTo(endingSoonViewPager)

        fetchHomeEvents()
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchHomeEvents() {
        progressBar.visibility = View.VISIBLE

        val api = RetrofitClient.instance.create(TicketmasterApi::class.java)
        api.searchEvents(apiKey, "", null).enqueue(object : Callback<TicketmasterResponse> {
            override fun onResponse(call: Call<TicketmasterResponse>, response: Response<TicketmasterResponse>) {
                progressBar.visibility = View.GONE

                val safeEmail = userEmail.replace(".", ",")

                if (response.isSuccessful) {
                    val allEvents = response.body()?._embedded?.events ?: emptyList()
                    val today = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                    val uniqueEvents = allEvents.distinctBy { it.name }

                    val todayOrSoonEvents = uniqueEvents.filter { event ->
                        val eventDate = LocalDate.parse(event.dates.start.localDate, formatter)
                        !eventDate.isBefore(today)
                    }.sortedBy { it.dates.start.localDate }
                        .take(5)

                    val endingSoonEvents = uniqueEvents
                        .filter { LocalDate.parse(it.dates.start.localDate, formatter) > today }
                        .take(2)

                    val filteredToday = todayOrSoonEvents.filterNot { top ->
                        endingSoonEvents.any { bottom -> bottom.name == top.name }
                    }

                    // Save to Firebase cache
                    val cacheRef = FirebaseDatabase.getInstance()
                        .getReference("cached_events")
                        .child(safeEmail)

                    cacheRef.setValue(uniqueEvents)
                        .addOnSuccessListener {
                            // Update adapter with fresh data
                            todayAdapter.updateData(filteredToday)
                            endingSoonAdapter.updateData(endingSoonEvents)
                        }
                        .addOnFailureListener { e ->
                            // fallback even if cache fails
                            todayAdapter.updateData(filteredToday)
                            endingSoonAdapter.updateData(endingSoonEvents)
                        }

                    todayLabel.text = if (todayOrSoonEvents.isNotEmpty()) "Happening Today" else "Upcoming Events"

                } else {
                    // fallback to cache if API fails
                    loadCachedEvents(safeEmail)
                }
            }

            override fun onFailure(call: Call<TicketmasterResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                t.printStackTrace()
                val safeEmail = userEmail.replace(".", ",")
                loadCachedEvents(safeEmail)
            }
        })
    }

    private fun loadCachedEvents(safeEmail: String) {
        val cacheRef = FirebaseDatabase.getInstance().getReference("cached_events").child(safeEmail)
        cacheRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val cachedEvents = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
                todayAdapter.updateData(cachedEvents.take(5)) // show top 5 as today/soon
                endingSoonAdapter.updateData(cachedEvents.takeLast(2)) // last 2 as ending soon
            }
        }
    }
}
