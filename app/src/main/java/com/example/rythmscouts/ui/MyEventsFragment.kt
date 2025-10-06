package com.example.rythmscouts.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rythmscouts.R
import com.example.rythmscouts.databinding.FragmentMyEventsBinding

// MyEventsFragment.kt
class MyEventsFragment : Fragment() {

    private var _binding: FragmentMyEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventAdapter: EventAdapter
    private var eventsState = EventsState()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToggleButtons()
        loadSampleData()
        updateUI()
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { event ->
            onEventClicked(event)
        }
        binding.eventsRecyclerView.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }

    private fun setupToggleButtons() {
        // Set initial selection
        binding.toggleGroup.check(R.id.savedEventsButton)

        binding.toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.savedEventsButton -> {
                        eventsState = eventsState.copy(currentView = EventViewType.SAVED)
                        updateUI()
                    }
                    R.id.pastEventsButton -> {
                        eventsState = eventsState.copy(currentView = EventViewType.PAST)
                        updateUI()
                    }
                }
            }
        }
    }

    private fun loadSampleData() {
        val savedEvents = listOf(
            Event(
                id = 1,
                title = "Summer Music Festival",
                artist = "Various Artists",
                dateTime = "Aug 15, 21h00",
                location = "Central Park"
            ),
            Event(
                id = 2,
                title = "Summer Music Festival",
                artist = "Various Artists",
                dateTime = "Aug 17, 19h00",
                location = "Central Park"
            )
        )

        val pastEvents = listOf(
            Event(
                id = 3,
                title = "Spring Jazz Night",
                artist = "Jazz Quartet",
                dateTime = "May 20, 20h00",
                location = "City Hall",
                isPastEvent = true
            ),
            Event(
                id = 4,
                title = "Winter Classical Concert",
                artist = "Symphony Orchestra",
                dateTime = "Dec 15, 19h30",
                location = "Concert Hall",
                isPastEvent = true
            )
        )

        eventsState = eventsState.copy(
            savedEvents = savedEvents,
            pastEvents = pastEvents
        )
    }

    private fun updateUI() {
        when (eventsState.currentView) {
            EventViewType.SAVED -> {
                eventAdapter.updateEvents(eventsState.savedEvents)
                binding.eventsCountTextView.text = "${eventsState.savedEvents.size} events"
                binding.subtitleTextView.text = "Your saved events"
            }
            EventViewType.PAST -> {
                eventAdapter.updateEvents(eventsState.pastEvents)
                binding.eventsCountTextView.text = "${eventsState.pastEvents.size} events"
                binding.subtitleTextView.text = "Your past events"
            }
        }
    }

    private fun onEventClicked(event: Event) {
        // Handle event click - navigate to event details
        Toast.makeText(
            requireContext(),
            "Clicked: ${event.title}",
            Toast.LENGTH_SHORT
        ).show()

        // You can navigate to event details fragment here
        // findNavController().navigate(R.id.action_myEvents_to_eventDetails)
    }

    // Public method to update events from outside
    fun updateEvents(savedEvents: List<Event>, pastEvents: List<Event>) {
        eventsState = eventsState.copy(
            savedEvents = savedEvents,
            pastEvents = pastEvents
        )
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}