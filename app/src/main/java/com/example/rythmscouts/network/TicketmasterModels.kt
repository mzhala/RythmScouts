package com.example.rythmscouts.network

data class TicketmasterResponse(
    val _embedded: Embedded?
)

data class Embedded(
    val events: List<Event>
)

data class Event(
    val name: String,
    val url: String,
    val images: List<Image>,
    val dates: Dates,
    val _embedded: EventVenueEmbedded?,
    val id: String,
    val sales: Sales? // <-- Added this field
)

data class Image(
    val url: String
)

data class Dates(
    val start: Start
)

data class Start(
    val localDate: String,
    val localTime: String?
)

data class Sales(
    val public: PublicSales?
)

data class PublicSales(
    val startDateTime: String?,
    val endDateTime: String?
)

data class EventVenueEmbedded(
    val venues: List<Venue>
)

data class Venue(
    val name: String,
    val city: City,
    val country: Country,
    val location: Location?
)

data class Location(
    val longitude: String?,
    val latitude: String?
)

data class City(val name: String)
data class Country(val name: String)
