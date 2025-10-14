package com.example.rythmscouts

data class User(
    val username: String = "",
    val email: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePictureUrl: String? = null
)
