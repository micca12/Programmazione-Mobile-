package com.micca.taskmanager.data.remote.models.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CredentialsRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
)
