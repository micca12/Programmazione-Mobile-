package com.micca.taskmanager.data.remote.api

import com.micca.taskmanager.data.BuildConfig
import com.micca.taskmanager.data.local.SessionManager
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Client di rete verso Supabase, con due client OkHttp:
 *  - authOkHttp: solo apikey, per gli endpoint di auth/v1
 *  - restOkHttp: apikey + Bearer dell'utente
 *
 * Sulle chiamate rest/v1 il Bearer deve essere il token dell'utente e non
 * la anon key: con la anon key la RLS risponde 200 [] e sembra "nessun task".
 */
class RetrofitClient(
    private val sessionManager: SessionManager,
) {

    // baseUrl deve finire con "/". Se le chiavi mancano in local.properties
    // usiamo un host palesemente finto: l'app parte comunque e le chiamate
    // falliscono con ErrorState.Network invece di crashare all'avvio.
    private val baseUrl: String =
        BuildConfig.SUPABASE_URL.trim().trimEnd('/').ifBlank { "https://config-mancante.invalid" } + "/"

    private val moshi = Moshi.Builder().build()

    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .build()
        chain.proceed(request)
    }

    private val bearerInterceptor = Interceptor { chain ->
        val token = sessionManager.getAccessToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val authOkHttp = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .build()

    private val restOkHttp = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(bearerInterceptor)
        .build()

    // Moshi di default salta i campi null: una PATCH senza due_date non lo
    // azzererebbe. Con withNullSerialization il null viene inviato davvero.
    private val converterFactory = MoshiConverterFactory.create(moshi).withNullSerialization()

    private val authRetrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(authOkHttp)
        .addConverterFactory(converterFactory)
        .build()

    private val restRetrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(restOkHttp)
        .addConverterFactory(converterFactory)
        .build()

    val authApi: SupabaseAuthApi by lazy { authRetrofit.create(SupabaseAuthApi::class.java) }

    val restApi: SupabaseRestApi by lazy { restRetrofit.create(SupabaseRestApi::class.java) }
}
