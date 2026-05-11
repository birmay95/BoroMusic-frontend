package com.example.musicplatform.api

import com.example.musicplatform.model.ExcludedTracksRequest
import com.example.musicplatform.model.TrackRecommendation
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface RecommendationApiService {

    @POST("/api/v1/recommendations/{trackId}")
    suspend fun getRecommendations(
        @Path("trackId") trackId: UUID,
        @Body request: ExcludedTracksRequest
    ): List<TrackRecommendation>

    @POST("/api/v1/recommendations/personal")
    fun getPersonalRecommendations(
        @Body request: ExcludedTracksRequest
    ): Call<List<TrackRecommendation>>
}