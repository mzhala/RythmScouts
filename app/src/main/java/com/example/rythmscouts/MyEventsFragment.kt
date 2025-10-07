package com.example.rythmscouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rythmscouts.databinding.FragmentMyEventsBinding

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
        // Handle event click - navigate to event details, show dialog, etc.
        when (event.id) {
            1 -> {
                // Action for event 1
            }
            2 -> {
                // Action for event 2
            }
            3 -> {
                // Action for event 3
            }
            4 -> {
                // Action for event 4
            }
        }

        // You can also use a more generic approach:
        // findNavController().navigate(MyEventsFragmentDirections.actionToEventDetails(event.id))
    }

    // Update events dynamically from outside
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

// Event Adapter
class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<Event> = emptyList()

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<android.widget.TextView>(R.id.eventTitle)
        private val artist = itemView.findViewById<android.widget.TextView>(R.id.eventArtist)
        private val dateTime = itemView.findViewById<android.widget.TextView>(R.id.eventDateTime)
        private val location = itemView.findViewById<android.widget.TextView>(R.id.eventLocation)

        fun bind(event: Event) {
            title.text = event.title
            artist.text = event.artist
            dateTime.text = event.dateTime
            location.text = event.location

            itemView.setOnClickListener {
                onEventClick(event)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_old, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}

// Data class for events
data class Event(
    val id: Int,
    val title: String,
    val artist: String,
    val dateTime: String,
    val location: String,
    val isPastEvent: Boolean = false
)

// Events State
data class EventsState(
    val savedEvents: List<Event> = emptyList(),
    val pastEvents: List<Event> = emptyList(),
    val currentView: EventViewType = EventViewType.SAVED
)

enum class EventViewType {
    SAVED, PAST
}