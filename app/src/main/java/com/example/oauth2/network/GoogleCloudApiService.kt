package com.example.oauth2.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GoogleCloudApiService {
    @POST("v2/projects/{projectId}/locations/global/keys")
    suspend fun createApiKey(
        @Header("Authorization") authHeader: String,
        @Path("projectId") projectId: String,
        @Body requestBody: ApiKeyRequestBody
    ): ApiKeyResponse
}