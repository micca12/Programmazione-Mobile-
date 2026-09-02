package com.micca.taskmanager.data.remote.api

import com.micca.taskmanager.data.remote.models.auth.AuthResponse
import com.micca.taskmanager.data.remote.models.auth.CredentialsRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Endpoint di autenticazione GoTrue (Supabase).
 * Questi endpoint NON vogliono il Bearer dell'utente: viaggiano sul client
 * "solo apikey" di RetrofitClient.
 */
interface SupabaseAuthApi {

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body body: CredentialsRequest,
    ): AuthResponse

    /** Login: grant_type=password. 400 = credenziali errate. */
    @POST("auth/v1/token")
    suspend fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body body: CredentialsRequest,
    ): AuthResponse
}
