package com.mediremind.app.data.remote

import com.mediremind.app.data.remote.models.OpenFdaResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenFdaApiService {

    /**
     * Busca fármacos en la base de datos nacional NDC (National Drug Code) de la FDA.
     * Permite consultar por nombre comercial, nombre genérico o principio activo.
     */
    @GET("drug/ndc.json")
    suspend fun searchNdc(
        @Query("search") query: String,
        @Query("limit") limit: Int = 15
    ): OpenFdaResponse

    companion object {
        private const val BASE_URL = "https://api.fda.gov/"

        fun create(): OpenFdaApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenFdaApiService::class.java)
        }
    }
}
