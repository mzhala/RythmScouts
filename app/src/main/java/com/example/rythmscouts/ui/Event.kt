package com.example.rythmscouts.ui

// Event.kt
data class Event(
    val id: Int,
    val title: String,
    val artist: String,
    val dateTime: String,
    val location: String,
    val isPastEvent: Boolean = false
)

// EventsState.kt
data class EventsState(
    val savedEvents: List<Event> = emptyList(),
    val pastEvents: List<Event> = emptyList(),
    val currentView: EventViewType = EventViewType.SAVED
)

enum class EventViewType {
    SAVED, PAST
}