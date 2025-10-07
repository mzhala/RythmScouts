package com.example.rythmscouts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rythmscouts.R
import com.example.rythmscouts.network.Event
import com.google.firebase.database.FirebaseDatabase

class EventAdapter(
    private var events: List<Event>,
    private val username: String = "testing-user"
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val date: TextView = view.findViewById(R.id.eventDate)
        val venue: TextView = view.findViewById(R.id.eventVenue)
        val image: ImageView = view.findViewById(R.id.eventImage)
        val saveButton: Button = view.findViewById(R.id.saveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount() = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val eventId = event.id ?: ""

        holder.title.text = event.name ?: "Unknown Event"
        holder.date.text = event.dates.start.localDate
        holder.venue.text = event._embedded?.venues?.firstOrNull()?.name ?: "Unknown Venue"

        val imageUrl = event.images.firstOrNull()?.url
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.image)
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("saved_events").child(username)

        // Check if event is already saved
        dbRef.child(eventId).get().addOnSuccessListener { snapshot ->
            holder.saveButton.text = if (snapshot.exists()) "Unsave" else "Save"
        }

        // Handle button click
        holder.saveButton.setOnClickListener {
            dbRef.child(eventId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    dbRef.child(eventId).removeValue()
                    holder.saveButton.text = "Save"
                } else {
                    dbRef.child(eventId).setValue(event)
                    holder.saveButton.text = "Unsave"
                }
            }
        }
    }

    fun updateData(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
