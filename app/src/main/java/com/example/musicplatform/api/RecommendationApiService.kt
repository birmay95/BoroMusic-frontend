package com.example.musicplatform.api

import com.example.musicplatform.model.TrackRecommendation
import retrofit2.http.GET
import retrofit2.http.Path

interface RecommendationApiService {

    @GET("recommendations/{trackId}")
    suspend fun getRecommendations(@Path("trackId") trackId: String): List<TrackRecommendation>
}