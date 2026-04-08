package com.example.musicplatform.api

import com.example.musicplatform.model.TrackRecommendation
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.UUID

interface RecommendationApiService {

    @GET("/api/v1/recommendations/{trackId}")
    suspend fun getRecommendations(@Path("trackId") trackId: UUID): List<TrackRecommendation>
}