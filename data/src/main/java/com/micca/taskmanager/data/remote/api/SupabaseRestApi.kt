package com.micca.taskmanager.data.remote.api

import com.micca.taskmanager.data.remote.models.tasks.PriorityRemote
import com.micca.taskmanager.data.remote.models.tasks.StatusRemote
import com.micca.taskmanager.data.remote.models.tasks.TagRemote
import com.micca.taskmanager.data.remote.models.tasks.TaskBody
import com.micca.taskmanager.data.remote.models.tasks.TaskRemote
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Endpoint PostgREST (rest/v1).
 * - Prefer: return=representation -> POST e PATCH tornano un array anche per
 *   una riga, per questo il tipo e' List<TaskRemote> e poi faccio .first().
 * - PATCH e DELETE VOGLIONO il filtro id=eq.{uuid}: senza, agirebbero su tutte
 *   le mie righe. Quindi idFilter e' obbligatorio.
 * - DELETE torna 204 vuoto -> Unit.
 * I GET non filtrano per user_id: ci pensa gia' la RLS.
 */
interface SupabaseRestApi {

    @GET("rest/v1/tasks")
    suspend fun getTasks(
        @Query("select") select: String = "*",
        @Query("order") order: String = "sort_order.asc.nullslast,id.asc",
    ): List<TaskRemote>

    @Headers("Prefer: return=representation")
    @POST("rest/v1/tasks")
    suspend fun createTask(
        @Body body: TaskBody,
    ): List<TaskRemote>

    /** @param idFilter nel formato "eq.{uuid}" — vedi eqId() */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/tasks")
    suspend fun updateTask(
        @Query("id") idFilter: String,
        @Body body: TaskBody,
    ): List<TaskRemote>

    /** @param idFilter nel formato "eq.{uuid}" — vedi eqId() */
    @DELETE("rest/v1/tasks")
    suspend fun deleteTask(
        @Query("id") idFilter: String,
    )

    @GET("rest/v1/tags")
    suspend fun getTags(
        @Query("select") select: String = "*",
    ): List<TagRemote>

    @GET("rest/v1/priorities")
    suspend fun getPriorities(
        @Query("select") select: String = "*",
    ): List<PriorityRemote>

    @GET("rest/v1/statuses")
    suspend fun getStatuses(
        @Query("select") select: String = "*",
    ): List<StatusRemote>
}

/** Costruisce il filtro PostgREST per una riga: "eq.{uuid}". */
fun eqId(id: String): String = "eq.$id"
