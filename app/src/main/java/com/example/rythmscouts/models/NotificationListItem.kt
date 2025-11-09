package com.example.rythmscouts.models

sealed class NotificationListItem {
    data class EventItem(val title: String, val subtitle: String, val timestamp: Long) : NotificationListItem()
    data class HeaderItem(val title: String) : NotificationListItem()
}
