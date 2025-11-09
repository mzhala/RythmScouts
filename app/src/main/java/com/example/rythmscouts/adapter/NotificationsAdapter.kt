package com.example.rythmscouts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rythmscouts.R
import com.example.rythmscouts.models.NotificationListItem

class NotificationsAdapter(private val items: List<NotificationListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_EVENT = 1
    }

    override fun getItemViewType(position: Int) = when(items[position]) {
        is NotificationListItem.HeaderItem -> TYPE_HEADER
        is NotificationListItem.EventItem -> TYPE_EVENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            EventViewHolder(view)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is NotificationListItem.HeaderItem -> (holder as HeaderViewHolder).bind(item)
            is NotificationListItem.EventItem -> (holder as EventViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        fun bind(header: NotificationListItem.HeaderItem) {
            tvHeader.text = header.title
        }
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.notificationTitle)
        private val subtitle: TextView = itemView.findViewById(R.id.notificationSubtitle)
        fun bind(event: NotificationListItem.EventItem) {
            title.text = event.title
            subtitle.text = event.subtitle
        }
    }
}
