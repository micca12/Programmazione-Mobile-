package com.micca.taskmanager.data.remote.models.auth

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Parsing delle risposte di GoTrue con gli adapter generati da KSP.
 * Verifica anche il caso "signup con conferma email attiva" (nessun token).
 */
class AuthResponseParsingTest {

    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(AuthResponse::class.java)

    @Test
    fun `risposta di login completa produce una Session`() {
        val json = """
            {
              "access_token": "abc123",
              "token_type": "bearer",
              "expires_in": 3600,
              "refresh_token": "def456",
              "user": { "id": "user-uuid-1", "email": "test@example.com", "role": "authenticated" }
            }
        """.trimIndent()

        val response = adapter.fromJson(json)

        assertNotNull(response)
        val session = response!!.toSession()
        assertNotNull(session)
        assertEquals("abc123", session!!.accessToken)
        assertEquals("def456", session.refreshToken)
        assertEquals("user-uuid-1", session.userId)
    }

    @Test
    fun `signup con conferma email attiva non produce una Session`() {
        // GoTrue risponde con il solo user, senza token, finche' l'email
        // non viene confermata
        val json = """
            { "id": "user-uuid-2", "email": "nuovo@example.com" }
        """.trimIndent()

        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNull(response!!.accessToken)
        assertNull(response.toSession())
    }

    @Test
    fun `campi sconosciuti nel JSON vengono ignorati`() {
        val json = """
            {
              "access_token": "t",
              "refresh_token": "r",
              "expires_in": 100,
              "provider_token": "ignorato",
              "user": { "id": "u", "aud": "authenticated", "app_metadata": {} }
            }
        """.trimIndent()

        val response = adapter.fromJson(json)

        assertNotNull(response!!.toSession())
    }
}
