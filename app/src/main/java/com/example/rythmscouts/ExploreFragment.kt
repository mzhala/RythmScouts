package com.example.rythmscouts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rythmscouts.adapter.EventAdapter
import com.example.rythmscouts.network.RetrofitClient
import com.example.rythmscouts.network.TicketmasterApi
import com.example.rythmscouts.network.TicketmasterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExploreFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var searchView: SearchView
    private lateinit var citySpinner: Spinner

    private val apiKey = "1A1i0hOsTyaaIstcAYpvNAmKjd84Kq3o"
    private val cities = listOf("All Cities", "Johannesburg", "Cape Town", "Durban", "Pretoria", "Port Elizabeth")

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        citySpinner = view.findViewById(R.id.citySpinner)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventAdapter(emptyList(), username = "testing-user")
        recyclerView.adapter = adapter

        setupSpinner()
        setupSearch()

        fetchEvents("", null) // Initial load with all events
        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                // Debounce search input
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
        val api = RetrofitClient.instance.create(TicketmasterApi::class.java)
        api.searchEvents(apiKey, query, null).enqueue(object : Callback<TicketmasterResponse> {
            override fun onResponse(call: Call<TicketmasterResponse>, response: Response<TicketmasterResponse>) {
                if (response.isSuccessful) {
                    val events = response.body()?._embedded?.events ?: emptyList()

                    // Local filtering for partial search and city
                    val filteredEvents = events.filter { event ->
                        // Partial match on event name
                        val matchesQuery = query.isBlank() || event.name.contains(query, ignoreCase = true)

                        // City filter only applied if a city is selected
                        val matchesCity = city?.let { selectedCity ->
                            event._embedded?.venues?.any { it.city.name.equals(selectedCity, ignoreCase = true) } ?: false
                        } ?: true // if city == null, include all

                        matchesQuery && matchesCity
                    }

                    adapter.updateData(filteredEvents)
                } else {
                    Log.e("ExploreFragment", "API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TicketmasterResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}
