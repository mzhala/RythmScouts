package com.example.rythmscouts.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketmasterApi {
    @GET("discovery/v2/events")
    fun searchEvents(
        @Query("apikey") apiKey: String,
        @Query("keyword") keyword: String,
        @Query("city") city: String?,
        @Query("countryCode") countryCode: String = "ZA"
    ): Call<TicketmasterResponse>

}
