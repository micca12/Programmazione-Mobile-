package com.micca.taskmanager.data.remote.models.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteUser(
    @Json(name = "id") var id: String? = null,
    @Json(name = "email") var email: String? = null,
)
