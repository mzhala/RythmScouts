package com.example.rythmscouts.adapter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rythmscouts.FirebaseHelper
import com.example.rythmscouts.R

data class FirebaseEvent(
    val id: String? = null,
    val name: String? = null,
    val date: String? = null,
    val date_raw: String? = null,
    val time_raw: String? = null,
    val venue: String? = null,
    val imageUrl: String? = null,
    val buyUrl: String? = null
)

class FirebaseEventAdapter(
    private var events: List<FirebaseEvent>,
    username: String = "testing-user",
    var savedEventIds: List<String> = emptyList()
) : RecyclerView.Adapter<FirebaseEventAdapter.EventViewHolder>() {

    private val safeUsername = username.replace(".", ",")

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val date: TextView = view.findViewById(R.id.eventDate)
        val venue: TextView = view.findViewById(R.id.eventVenue)
        val image: ImageView = view.findViewById(R.id.eventImage)
        val saveButton: Button = view.findViewById(R.id.saveButton)
        val buyButton: ImageButton = view.findViewById(R.id.buyTicketsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        EventViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false))

    override fun getItemCount() = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val eventId = event.id ?: return

        holder.title.text = event.name ?: "Unknown Event"
        holder.date.text = event.date ?: "Unknown Date"
        holder.venue.text = event.venue ?: "Unknown Venue"

        event.imageUrl?.let { Glide.with(holder.itemView.context).load(it).into(holder.image) }

        holder.saveButton.text = if (savedEventIds.contains(eventId)) "Unsave" else "Save"

        val dbRef = FirebaseHelper.savedEventsRef(safeUsername)

        holder.saveButton.setOnClickListener {
            val wasSaved = savedEventIds.contains(eventId)
            val eventRef = dbRef.child(eventId)
            val online = isOnline(holder.itemView.context)

            if (wasSaved) {
                // UNSAVE offline-safe
                eventRef.removeValue()
                savedEventIds = savedEventIds - eventId
                holder.saveButton.text = "Save"
                showEventStatusDialog(holder.itemView.context, false, !online)
            } else {
                // SAVE offline-safe
                val eventData = mapOf(
                    "id" to event.id,
                    "name" to (event.name ?: "Unknown Event"),
                    "date_raw" to event.date_raw,
                    "time_raw" to event.time_raw,
                    "date" to holder.date.text.toString(),
                    "venue" to (event.venue ?: "Unknown Venue"),
                    "imageUrl" to (event.imageUrl ?: ""),
                    "buyUrl" to (event.buyUrl ?: "")
                )

                eventRef.setValue(eventData)
                savedEventIds = savedEventIds + eventId
                holder.saveButton.text = "Unsave"
                showEventStatusDialog(holder.itemView.context, true, !online)

                if (online) {
                    sendEventNotification(event.name ?: "New Event", holder.itemView.context)
                }
            }
        }

        // Buy button
        if (!event.buyUrl.isNullOrEmpty()) {
            holder.buyButton.visibility = View.VISIBLE
            holder.buyButton.setOnClickListener {
                holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(event.buyUrl)))
            }
        } else {
            holder.buyButton.visibility = View.GONE
        }
    }

    fun updateData(newEvents: List<FirebaseEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }

    private fun showEventStatusDialog(context: Context, saved: Boolean, offline: Boolean) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_event_status, null)
        val imageView: ImageView = dialogView.findViewById(R.id.statusImage)
        val title: TextView = dialogView.findViewById(R.id.statusTitle)
        val message: TextView = dialogView.findViewById(R.id.statusMessage)
        val closeButton: Button = dialogView.findViewById(R.id.closeButton)

        if (saved) {
            imageView.setImageResource(R.drawable.ic_saved)
            title.text = "Event Saved!"
            message.text = if (offline) {
                "You're offline. This event will be saved once you're back online."
            } else {
                "Can't wait to see you there!"
            }
        } else {
            imageView.setImageResource(R.drawable.ic_unsaved)
            title.text = "Event Unsaved!"
            message.text = if (offline) {
                "You're offline. This event will be removed once you're back online."
            } else {
                "This event has been removed from your saved list."
            }
        }

        val dialog = AlertDialog.Builder(context).setView(dialogView).create()
        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.decorView?.postDelayed({ dialog.dismiss() }, 2000)
    }

    private fun sendEventNotification(eventName: String, context: Context) {
        val channelId = "event_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Event Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Event Saved")
            .setContentText("You saved \"$eventName\"")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}
