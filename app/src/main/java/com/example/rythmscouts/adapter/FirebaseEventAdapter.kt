package com.example.rythmscouts.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rythmscouts.R
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.appcompat.app.AlertDialog

data class FirebaseEvent(
    val id: String? = null,
    val name: String? = null,
    val date: String? = null,          // formatted date for display
    val date_raw: String? = null,      // yyyy-MM-dd
    val time_raw: String? = null,      // HH:mm:ss
    val venue: String? = null,
    val imageUrl: String? = null,
    val buyUrl: String? = null
)

class FirebaseEventAdapter(
    private var events: List<FirebaseEvent>,
    private val username: String = "testing-user"
) : RecyclerView.Adapter<FirebaseEventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val date: TextView = view.findViewById(R.id.eventDate)
        val venue: TextView = view.findViewById(R.id.eventVenue)
        val image: ImageView = view.findViewById(R.id.eventImage)
        val saveButton: Button = view.findViewById(R.id.saveButton)
        val buyButton: ImageButton = view.findViewById(R.id.buyTicketsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount() = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val dbRef = FirebaseDatabase.getInstance().getReference("saved_events").child(username)

        holder.title.text = event.name ?: "Unknown Event"
        holder.date.text = event.date ?: "Unknown Date"
        holder.venue.text = event.venue ?: "Unknown Venue"

        // Image
        event.imageUrl?.let {
            Glide.with(holder.itemView.context).load(it).into(holder.image)
        }

        // Save/Unsave button
        event.id?.let { eventId ->
            dbRef.child(eventId).get().addOnSuccessListener { snapshot ->
                holder.saveButton.text = if (snapshot.exists()) "Unsave" else "Save"
            }
        }

        // Buy button
        if (!event.buyUrl.isNullOrEmpty()) {
            holder.buyButton.visibility = View.VISIBLE
            holder.buyButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.buyUrl))
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.buyButton.visibility = View.GONE
        }

        // Save/Unsave click
        holder.saveButton.setOnClickListener {
            event.id?.let { eventId ->
                dbRef.child(eventId).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Remove from Firebase
                        dbRef.child(eventId).removeValue().addOnSuccessListener {
                            holder.saveButton.text = "Save"
                            showEventStatusDialog(holder.itemView.context, false)

                            // Remove from adapter's list
                            val position = holder.adapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                events = events.toMutableList().apply { removeAt(position) }
                                notifyItemRemoved(position)
                            }
                        }
                    } else {
                        // Add to Firebase (same as before)
                        val eventData = mapOf(
                            "id" to event.id,
                            "name" to event.name,
                            "date" to event.date,
                            "date_raw" to event.date_raw,
                            "time_raw" to event.time_raw,
                            "venue" to event.venue,
                            "imageUrl" to event.imageUrl,
                            "buyUrl" to event.buyUrl
                        )
                        dbRef.child(eventId).setValue(eventData)
                            .addOnSuccessListener {
                                holder.saveButton.text = "Unsave"
                                showEventStatusDialog(holder.itemView.context, true)
                            }
                    }
                }
            }
        }

    }

    fun updateData(newEvents: List<FirebaseEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }

    private fun showEventStatusDialog(context: android.content.Context, saved: Boolean) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_event_status, null)
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

        val dialog = AlertDialog.Builder(context).setView(dialogView).create()
        dialog.show()
        dialog.window?.decorView?.postDelayed({ dialog.dismiss() }, 2000)
    }
}
