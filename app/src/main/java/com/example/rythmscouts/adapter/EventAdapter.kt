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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rythmscouts.FirebaseHelper
import com.example.rythmscouts.R
import com.example.rythmscouts.network.Event
import com.example.rythmscouts.network.EventVenueEmbedded
import com.google.firebase.database.FirebaseDatabase

class EventAdapter(
    private var events: List<Event>,
    private val username: String = "testing-user",
    private val isHomePage: Boolean = false,
    var savedEventIds: List<String> = emptyList()
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    // Firebase-safe username (replace . with ,)
    private val safeUsername = username.replace(".", ",")

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val date: TextView = view.findViewById(R.id.eventDate)
        val venue: TextView = view.findViewById(R.id.eventVenue)
        val image: ImageView = view.findViewById(R.id.eventImage)
        val saveButton: Button = view.findViewById(R.id.saveButton)
        val buyButton: ImageButton = view.findViewById(R.id.buyTicketsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val layoutRes = if (isHomePage) R.layout.item_event_home else R.layout.item_event
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount() = events.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val eventId = event.id ?: return

        //val dbRef = FirebaseDatabase.getInstance().getReference("saved_events").child(safeUsername)
        val dbRef = FirebaseHelper.savedEventsRef(safeUsername)
        //dbRef.keepSynced(true)

        // Populate views
        holder.title.text = event.name ?: "Unknown Event"
        val dateStr = event.dates.start.localDate
        val timeStr = event.dates.start.localTime

        holder.date.text = try {
            if (!timeStr.isNullOrEmpty()) {
                val inputFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val outputFormat = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
                java.time.LocalDateTime.parse("$dateStr $timeStr", inputFormat).format(outputFormat)
            } else {
                val inputFormat = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val outputFormat = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                java.time.LocalDate.parse(dateStr, inputFormat).format(outputFormat)
            }
        } catch (e: Exception) {
            dateStr
        }

        val venueObj = (event._embedded as? EventVenueEmbedded)?.venues?.firstOrNull()
        holder.venue.text = venueObj?.let { "${it.name}, ${it.city.name}" } ?: "Unknown Venue"

        val imageUrl = event.images.firstOrNull()?.url
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context).load(imageUrl).into(holder.image)
        }

        // Buy button
        if (!event.url.isNullOrEmpty()) {
            holder.buyButton.visibility = View.VISIBLE
            holder.buyButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                holder.itemView.context.startActivity(intent)
            }
        } else holder.buyButton.visibility = View.GONE

        // Set save button text
        holder.saveButton.text = if (savedEventIds.contains(eventId)) "Unsave" else "Save"

        // Handle save/unsave logic
        holder.saveButton.setOnClickListener {
            val wasSaved = savedEventIds.contains(eventId)
            val eventRef = dbRef.child(eventId)

            if (wasSaved) {
                // Remove only from Firebase, keep it in the list
                eventRef.removeValue()
                savedEventIds = savedEventIds - eventId
                holder.saveButton.text = "Save"
                showEventStatusDialog(holder.itemView.context, false, offline = !isOnline(holder.itemView.context))


        } else {
                // Add event to Firebase (offline-safe)
                val salesStart = event.sales?.public?.startDateTime ?: ""
                val salesEnd = event.sales?.public?.endDateTime ?: ""
                val venueObj = (event._embedded as? EventVenueEmbedded)?.venues?.firstOrNull()
                val eventData = mapOf(
                    "id" to event.id,
                    "name" to (event.name ?: "Unknown Event"),
                    "date_raw" to dateStr,
                    "time_raw" to timeStr,
                    "date" to holder.date.text.toString(),
                    "venue" to (venueObj?.name ?: "Unknown Venue"),
                    "city" to (venueObj?.city?.name ?: "Unknown City"),
                    "imageUrl" to (event.images.firstOrNull()?.url ?: ""),
                    "buyUrl" to (event.url ?: ""),
                    "latitude" to (venueObj?.location?.latitude ?: ""),
                    "longitude" to (venueObj?.location?.longitude ?: ""),
                    "salesStart" to salesStart,
                    "salesEnd" to salesEnd
                )

                eventRef.setValue(eventData)
                savedEventIds = savedEventIds + eventId
                holder.saveButton.text = "Unsave"
                showEventStatusDialog(holder.itemView.context, true, offline = !isOnline(holder.itemView.context))

                // Trigger notification only if online
                if (isOnline(holder.itemView.context)) {
                    sendEventNotification(event.name ?: "New Event", holder.itemView.context)
                }
            }
        }




    }

    fun updateData(newEvents: List<Event>) {
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
                "You're offline. This event will be saved once you reconnect."
            } else {
                "Can't wait to see you there!"
            }
        } else {
            imageView.setImageResource(R.drawable.ic_unsaved)
            title.text = "Event Unsaved!"
            message.text = if (offline) {
                "You're offline. This event will be removed from your saved events once you reconnect."
            } else {
                "This event will be removed from your saved events."
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(context).setView(dialogView).create()
        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.decorView?.postDelayed({ dialog.dismiss() }, 2000)
    }


    private fun sendEventNotification(eventName: String, context: Context) {
        val channelId = "event_notifications"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
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

    // --- Helper method to check network ---
    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // --- Offline popup ---
    private fun showOfflineSaveDialog(context: Context, wasSaved: Boolean) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_event_status, null)
        val imageView: ImageView = dialogView.findViewById(R.id.statusImage)
        val title: TextView = dialogView.findViewById(R.id.statusTitle)
        val message: TextView = dialogView.findViewById(R.id.statusMessage)
        val closeButton: Button = dialogView.findViewById(R.id.closeButton)

        if (wasSaved) {
            imageView.setImageResource(R.drawable.ic_saved)
            title.text = "Offline"
            message.text = "You're currently offline. This event will be removed from your saved events once you reconnect."
        } else {
            imageView.setImageResource(R.drawable.ic_unsaved)
            title.text = "Offline"
            message.text = "You're currently offline. This event will be saved to your saved events once you reconnect."
        }

        val dialog = AlertDialog.Builder(context).setView(dialogView).create()
        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
