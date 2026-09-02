package com.micca.taskmanager.data.remote.models.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Risposta degli endpoint auth/v1. Campi nullable: con "Confirm email"
 * attivo il signup risponde SENZA access_token.
 */
@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token") var accessToken: String? = null,
    @Json(name = "refresh_token") var refreshToken: String? = null,
    @Json(name = "expires_in") var expiresIn: Long? = null,
    @Json(name = "user") var user: RemoteUser? = null,
)
