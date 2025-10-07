package com.example.rythmscouts.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rythmscouts.R
import com.example.rythmscouts.network.Event
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.app.AlertDialog

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val eventId = event.id ?: ""

        holder.title.text = event.name ?: "Unknown Event"

        // Format date and time
        val dateStr = event.dates.start.localDate
        val timeStr = event.dates.start.localTime

        val formattedDate = try {
            if (!timeStr.isNullOrEmpty()) {
                // Both date and time available
                val inputFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val outputFormat = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
                val dateTime = java.time.LocalDateTime.parse("$dateStr ${timeStr}", inputFormat)
                dateTime.format(outputFormat)
            } else {
                // Only date available
                val inputFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val outputFormat = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                val date = java.time.LocalDate.parse(dateStr, inputFormat)
                date.format(outputFormat)
            }
        } catch (e: Exception) {
            // Fallback if parsing fails
            dateStr
        }

        holder.date.text = formattedDate
        holder.venue.text = event._embedded?.venues?.firstOrNull()?.name ?: "Unknown Venue"

        val imageUrl = event.images.firstOrNull()?.url
        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.image)
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("saved_events").child(username)

        dbRef.child(eventId).get().addOnSuccessListener { snapshot ->
            holder.saveButton.text = if (snapshot.exists()) "Unsave" else "Save"
        }

        holder.saveButton.setOnClickListener {
            dbRef.child(eventId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    dbRef.child(eventId).removeValue()
                    holder.saveButton.text = "Save"
                    showEventStatusDialog(holder.itemView.context, false)
                } else {
                    val eventData = mapOf(
                        "id" to event.id,
                        "name" to (event.name ?: "Unknown Event"),
                        "date" to formattedDate,
                        "venue" to (event._embedded?.venues?.firstOrNull()?.name ?: "Unknown Venue"),
                        "imageUrl" to (event.images.firstOrNull()?.url ?: "")
                    )

                    dbRef.child(eventId).setValue(eventData)
                        .addOnSuccessListener {
                            holder.saveButton.text = "Unsave"
                            showEventStatusDialog(holder.itemView.context, true)
                        }
                        .addOnFailureListener { e -> e.printStackTrace() }
                }
            }
        }
    }



    fun updateData(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    // Popup for event saved/unsaved
    private fun showEventStatusDialog(context: android.content.Context, saved: Boolean) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_event_status, null)

        val imageView: ImageView = dialogView.findViewById(R.id.statusImage)
        val title: TextView = dialogView.findViewById(R.id.statusTitle)
        val message: TextView = dialogView.findViewById(R.id.statusMessage)

        if (saved) {
            imageView.setImageResource(R.drawable.ic_saved)
            title.text = "Event Saved!"
            message.text = "Can't wait to see you there!"
        } else {
            imageView.setImageResource(R.drawable.ic_unsaved)
            title.text = "Event Unsaved!"
            message.text = "This event will be removed from your saved events."
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.show()

        // Automatically dismiss after 2 seconds
        dialog.window?.decorView?.postDelayed({ dialog.dismiss() }, 2000)
    }
}
