package com.example.rythmscouts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rythmscouts.adapter.FirebaseEvent
import com.example.rythmscouts.adapter.FirebaseEventAdapter
import com.example.rythmscouts.databinding.FragmentMyEventsBinding
import com.google.firebase.database.FirebaseDatabase
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import com.jakewharton.threetenabp.AndroidThreeTen

class MyEventsFragment : BaseFragment() {

    private var _binding: FragmentMyEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventAdapter: FirebaseEventAdapter
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(requireContext())

        // Get the user email from arguments (sent by MainActivity)
        userEmail = arguments?.getString("USER_EMAIL")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentMyEventsBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToggleButtons()
        fetchSavedEvents(showPast = false)
    }

    private fun setupRecyclerView() {
        val safeEmail = userEmail?.replace(".", ",") ?: "unknown-user"
        eventAdapter = FirebaseEventAdapter(emptyList(), username = safeEmail)

        binding.eventsRecyclerView.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupToggleButtons() {
        binding.toggleGroup.check(R.id.savedEventsButton)
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.savedEventsButton -> fetchSavedEvents(showPast = false)
                    R.id.pastEventsButton -> fetchSavedEvents(showPast = true)
                }
            }
        }
    }

    private fun fetchSavedEvents(showPast: Boolean) {
        val email = userEmail ?: run {
            Log.e("MyEventsFragment", "No user email received")
            return
        }

        val safeEmail = email.replace(".", ",")
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("saved_events")
            .child(safeEmail)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (!isAdded || _binding == null) return@addOnSuccessListener

            // 1️⃣ Collect saved event IDs
            val savedIds = snapshot.children.mapNotNull { it.key }
            eventAdapter.savedEventIds = savedIds // <-- update adapter

            // 2️⃣ Prepare list of events to display
            val now = LocalDate.now()
            val events = mutableListOf<FirebaseEvent>()

            snapshot.children.forEach { child ->
                val id = child.child("id").getValue(String::class.java) ?: return@forEach
                val name = child.child("name").getValue(String::class.java) ?: "Unknown Event"
                val dateRaw = child.child("date_raw").getValue(String::class.java) ?: return@forEach
                val timeRaw = child.child("time_raw").getValue(String::class.java) ?: "00:00:00"
                val venue = child.child("venue").getValue(String::class.java) ?: "Unknown Venue"
                val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                val buyUrl = child.child("buyUrl").getValue(String::class.java) ?: ""
                val formattedDate = child.child("date").getValue(String::class.java)

                try {
                    val eventDate = LocalDate.parse(dateRaw, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val isPast = eventDate.isBefore(now)

                    if ((showPast && isPast) || (!showPast && !isPast)) {
                        events.add(
                            FirebaseEvent(
                                id = id,
                                name = name,
                                date_raw = dateRaw,
                                time_raw = timeRaw,
                                date = formattedDate,
                                venue = venue,
                                imageUrl = imageUrl,
                                buyUrl = buyUrl
                            )
                        )
                    }
                } catch (e: DateTimeParseException) {
                    Log.e("MyEventsFragment", "Failed to parse date: $dateRaw")
                }
            }

            // 3️⃣ Update adapter
            if (isAdded && _binding != null) {
                eventAdapter.updateData(events)
                binding.eventsCountTextView.text = "${events.size} events"
            }

        }.addOnFailureListener { e ->
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
