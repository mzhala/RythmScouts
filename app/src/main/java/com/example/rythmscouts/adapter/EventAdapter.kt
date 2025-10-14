package com.example.rythmscouts.adapter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rythmscouts.R
import com.example.rythmscouts.network.Event
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.app.AlertDialog
import com.example.rythmscouts.network.EventVenueEmbedded

class EventAdapter(
    private var events: List<Event>,
    private val username: String = "testing-user",
    var savedEventIds: List<String> = emptyList()
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

        val dateStr = event.dates.start.localDate
        val timeStr = event.dates.start.localTime
        val formattedDate = try {
            if (!timeStr.isNullOrEmpty()) {
                val inputFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val outputFormat = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
                val dateTime = java.time.LocalDateTime.parse("$dateStr $timeStr", inputFormat)
                dateTime.format(outputFormat)
            } else {
                val inputFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val outputFormat = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                val date = java.time.LocalDate.parse(dateStr, inputFormat)
                date.format(outputFormat)
            }
        } catch (e: Exception) { dateStr }

        holder.date.text = formattedDate

        val venue = (event._embedded as? EventVenueEmbedded)?.venues?.firstOrNull()
        holder.venue.text = venue?.let { "${it.name}, ${it.city.name}" } ?: "Unknown Venue"

        val imageUrl = event.images.firstOrNull()?.url
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.image)
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("saved_events").child(username)

        // Set button text based on savedEventIds
        holder.saveButton.text = if (savedEventIds.contains(eventId)) "Unsave" else "Save"

        val buyButton: ImageButton = holder.itemView.findViewById(R.id.buyTicketsButton)
        if (!event.url.isNullOrEmpty()) {
            buyButton.visibility = View.VISIBLE
            buyButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                holder.itemView.context.startActivity(intent)
            }
        } else {
            buyButton.visibility = View.GONE
        }

        // Save/Unsave button
        holder.saveButton.setOnClickListener {
            dbRef.child(eventId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    dbRef.child(eventId).removeValue()
                    holder.saveButton.text = "Save"
                    showEventStatusDialog(holder.itemView.context, false)
                    savedEventIds = savedEventIds - eventId
                } else {
                    val salesStart = event.sales?.public?.startDateTime ?: ""
                    val salesEnd = event.sales?.public?.endDateTime ?: ""

                    val eventData = mapOf(
                        "id" to event.id,
                        "name" to (event.name ?: "Unknown Event"),
                        "date_raw" to dateStr,
                        "time_raw" to timeStr,
                        "date" to formattedDate,
                        "venue" to (venue?.name ?: "Unknown Venue"),
                        "city" to (venue?.city?.name ?: "Unknown City"),
                        "imageUrl" to (event.images.firstOrNull()?.url ?: ""),
                        "buyUrl" to (event.url ?: ""),
                        "latitude" to (venue?.location?.latitude ?: ""),
                        "longitude" to (venue?.location?.longitude ?: ""),
                        "salesStart" to salesStart,
                        "salesEnd" to salesEnd
                    )


                    dbRef.child(eventId).setValue(eventData)
                        .addOnSuccessListener {
                            holder.saveButton.text = "Unsave"
                            savedEventIds = savedEventIds + eventId
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

    // Popup for saved/unsaved events with Close button
    private fun showEventStatusDialog(context: android.content.Context, saved: Boolean) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_event_status, null)

        val imageView: ImageView = dialogView.findViewById(R.id.statusImage)
        val title: TextView = dialogView.findViewById(R.id.statusTitle)
        val message: TextView = dialogView.findViewById(R.id.statusMessage)
        val closeButton: Button = dialogView.findViewById(R.id.closeButton) // make sure your XML has this button

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

        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
