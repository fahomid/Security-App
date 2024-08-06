package com.fahomid.securityapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

// Interface defining the API endpoints for retrieving clips
interface ClipsApi {

    // GET request to retrieve a list of clips from the provided URL
    @GET
    fun getClips(@Url clipsUrl: String): Call<List<String>>
}
