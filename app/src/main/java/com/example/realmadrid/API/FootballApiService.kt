package com.example.realmadrid.API

import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {
    @GET("?action=get_events")
    suspend fun getMatches(
        @Query("timezone") timezone: String,
        @Query("team_id") teamId: Int,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("APIkey") apiKey: String
    ): List<Match>
}