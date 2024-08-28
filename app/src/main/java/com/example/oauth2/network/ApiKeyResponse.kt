package com.example.oauth2.network

data class ApiKeyRequestBody(
    val displayName: String,
)

data class ApiKeyRestrictions(
    val androidKeyRestrictions: AndroidKeyRestrictions
)

data class AndroidKeyRestrictions(
    val allowedApplications: List<AllowedApplication>
)

data class AllowedApplication(
    val sha1Fingerprint: String
)

data class ApiKeyResponse(
    val apiKey: String
)
