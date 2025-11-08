package com.example.rythmscouts.network

data class TicketmasterResponse(
    val _embedded: Embedded?
)

data class Embedded(
    val events: List<Event>
)

data class Event(
    val name: String = "",
    val url: String = "",
    val images: List<Image> = emptyList(),
    val dates: Dates = Dates(),
    val _embedded: EventVenueEmbedded? = null,
    val id: String = "",
    val sales: Sales? = null
)

data class Image(
    val url: String = ""
)

data class Dates(
    val start: Start = Start()
)

data class Start(
    val localDate: String = "",
    val localTime: String? = null
)

data class Sales(
    val public: PublicSales? = null
)

data class PublicSales(
    val startDateTime: String? = null,
    val endDateTime: String? = null
)

data class EventVenueEmbedded(
    val venues: List<Venue> = emptyList()
)

data class Venue(
    val name: String = "",
    val city: City = City(),
    val country: Country = Country(),
    val location: Location? = null,
)

data class Location(
    val longitude: String? = null,
    val latitude: String? = null
)

data class City(val name: String = "")
data class Country(val name: String = "")
