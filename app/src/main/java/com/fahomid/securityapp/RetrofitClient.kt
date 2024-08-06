package com.fahomid.securityapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton class to manage Retrofit instance
class RetrofitClient private constructor(baseUrl: String) {

    // Retrofit instance initialized with base URL and Gson converter
    val instance: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    companion object {
        @Volatile
        private var INSTANCE: RetrofitClient? = null  // Volatile instance to ensure visibility across threads

        // Get the singleton instance of RetrofitClient
        fun getInstance(baseUrl: String): RetrofitClient =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RetrofitClient(baseUrl).also { INSTANCE = it }  // Initialize instance if null
            }
    }
}
