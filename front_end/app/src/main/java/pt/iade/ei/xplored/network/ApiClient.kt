package pt.iade.ei.xplored.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pt.iade.ei.xplored.XploredApplication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // --- CRITICAL CONFIGURATION FIXES ---
    // 1. USE 10.0.2.2 for Android Emulator. (Use your PC's IP, e.g., 192.168.1.5, for a real phone)
    // 2. PORT is 9000 (matches application.properties)
    // 3. Removed "/api/" because your Spring Controllers don't have it.
    private const val BASE_URL = "http://10.0.2.2:9000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // FIX: Use the safely initialized instance
    private val authInterceptor by lazy {
        AuthInterceptor(XploredApplication.instance)
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            // Added timeouts to prevent infinite hanging
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}