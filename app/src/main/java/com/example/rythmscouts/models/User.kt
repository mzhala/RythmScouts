package com.example.rythmscouts.models

data class User(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePictureUrl: String? = null
)
