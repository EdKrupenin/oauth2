package com.example.oauth2.repository

import com.example.oauth2.network.ApiKeyRequestBody
import com.example.oauth2.network.GoogleCloudApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GoogleCloudRepository @Inject constructor(
    private val googleCloudApiService: GoogleCloudApiService
) {
    fun createApiKey(
        accessToken: String,
        projectId: String,
    ): Flow<Result<String>> = flow {
        try {
            val requestBody = ApiKeyRequestBody(
                displayName = "User-specific API Key"
            )
            val response = googleCloudApiService.createApiKey(
                authHeader = "Bearer $accessToken",
                projectId = projectId,
                requestBody = requestBody
            )
            emit(Result.success(response.apiKey))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
