package com.example.rythmscouts

import android.os.Bundle
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

    private val cities = listOf(
        "All Cities", "Johannesburg", "Cape Town", "Durban", "Pretoria", "Port Elizabeth"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        citySpinner = view.findViewById(R.id.citySpinner)  // ✅ added

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventAdapter(emptyList(), username = "testing-user")

        recyclerView.adapter = adapter

        setupSpinner()
        setupSearch()

        fetchEvents("music") // initial load
        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val selectedCity = if (citySpinner.selectedItemPosition == 0) null else cities[citySpinner.selectedItemPosition]
                    fetchEvents(it, selectedCity)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
    }

    private fun setupSpinner() {
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cities)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = adapterSpinner

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCity = if (position == 0) null else cities[position]
                fetchEvents(searchView.query.toString(), selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchEvents(query: String, city: String? = "") {
        val api = RetrofitClient.instance.create(TicketmasterApi::class.java)

        api.searchEvents(apiKey, query, city).enqueue(object : Callback<TicketmasterResponse> {
            override fun onResponse(call: Call<TicketmasterResponse>, response: Response<TicketmasterResponse>) {
                if (response.isSuccessful) {
                    val events = response.body()?._embedded?.events ?: emptyList()
                    adapter.updateData(events)
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
