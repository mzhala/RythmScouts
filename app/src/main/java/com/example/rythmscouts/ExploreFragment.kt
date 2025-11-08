package com.example.rythmscouts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rythmscouts.adapter.EventAdapter
import com.example.rythmscouts.network.Event
import com.example.rythmscouts.network.EventVenueEmbedded
import com.example.rythmscouts.network.RetrofitClient
import com.example.rythmscouts.network.TicketmasterApi
import com.example.rythmscouts.network.TicketmasterResponse
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreFragment : BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var searchView: SearchView
    private lateinit var citySpinner: Spinner
    private lateinit var progressBar: ProgressBar

    private val apiKey = "1A1i0hOsTyaaIstcAYpvNAmKjd84Kq3o"
    private val cities = listOf(
        "All Cities", "Johannesburg", "Cape Town", "Durban", "Pretoria", "Port Elizabeth",
        "Polokwane", "Mbombela", "Nelspruit", "Bloemfontein", "Kimberley", "East London"
    )

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userEmail = arguments?.getString("USER_EMAIL")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        citySpinner = view.findViewById(R.id.citySpinner)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val safeEmail = userEmail?.replace(".", ",") ?: "unknown-user"
        adapter = EventAdapter(emptyList(), username = safeEmail)
        recyclerView.adapter = adapter

        setupSpinner()
        setupSearch()
        fetchEvents("", null)

        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { handler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    val searchQuery = newText ?: ""
                    val selectedCity = if (citySpinner.selectedItemPosition == 0) null
                    else cities[citySpinner.selectedItemPosition]
                    fetchEvents(searchQuery, selectedCity)
                }
                handler.postDelayed(searchRunnable!!, 300)
                return true
            }
        })
    }

    private fun setupSpinner() {
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cities)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = adapterSpinner

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCity = if (position == 0) null else cities[position]
                val query = searchView.query.toString()
                fetchEvents(query, selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchEvents(query: String, city: String? = null) {
        progressBar.visibility = View.VISIBLE

        val api = RetrofitClient.instance.create(TicketmasterApi::class.java)
        api.searchEvents(apiKey, query, null).enqueue(object : Callback<TicketmasterResponse> {
            override fun onResponse(call: Call<TicketmasterResponse>, response: Response<TicketmasterResponse>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val events = response.body()?._embedded?.events ?: emptyList()

                    val filteredEvents = events.filter { event ->
                        val matchesQuery = query.isBlank() || event.name.contains(query, ignoreCase = true)
                        val matchesCity = city?.let { selectedCity ->
                            (event._embedded as? EventVenueEmbedded)
                                ?.venues?.any { it.city.name.equals(selectedCity, ignoreCase = true) } ?: false
                        } ?: true
                        matchesQuery && matchesCity
                    }

                    val safeEmail = userEmail?.replace(".", ",") ?: "unknown-user"

                    // Saved events reference
                    val savedRef = FirebaseDatabase.getInstance()
                        .getReference("saved_events")
                        .child(safeEmail)

                    savedRef.get().addOnSuccessListener { snapshot ->
                        val savedEventIds = snapshot.children.mapNotNull { it.key }
                        adapter.savedEventIds = savedEventIds

                        // Update adapter with API results, fallback to cache if empty
                        val cacheRef = FirebaseDatabase.getInstance()
                            .getReference("cached_events")
                            .child(safeEmail)

                        cacheRef.setValue(filteredEvents)
                            .addOnSuccessListener {
                                Log.d("ExploreFragment", "Events cached locally for offline use")
                            }
                            .addOnFailureListener { e ->
                                Log.e("ExploreFragment", "Failed to cache events: ${e.message}")
                            }

                        // Fetch cached events if API returns empty
                        cacheRef.get().addOnSuccessListener { cacheSnapshot ->
                            val cachedEvents = cacheSnapshot.children.mapNotNull {
                                it.getValue(Event::class.java)
                            }
                            val finalList = if (filteredEvents.isNotEmpty()) filteredEvents else cachedEvents
                            adapter.updateData(finalList)
                        }
                    }

                } else {
                    Log.e("ExploreFragment", "API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TicketmasterResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                t.printStackTrace()

                val safeEmail = userEmail?.replace(".", ",") ?: "unknown-user"
                val cacheRef = FirebaseDatabase.getInstance()
                    .getReference("cached_events")
                    .child(safeEmail)

                cacheRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val cachedEvents = snapshot.children.mapNotNull { it.getValue(Event::class.java) }
                        adapter.updateData(cachedEvents)
                    }
                }
            }
        })
    }
}
