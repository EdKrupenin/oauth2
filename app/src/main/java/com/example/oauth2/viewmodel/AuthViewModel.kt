package com.example.oauth2.viewmodel

import android.accounts.Account
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oauth2.auth.AuthTokenProvider
import com.example.oauth2.repository.GoogleCloudRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleSignInClient: GoogleSignInClient,
    private val googleCloudRepository: GoogleCloudRepository,
    private val authTokenProvider: AuthTokenProvider,
    @Named("projectId") private val projectId: String,
) : ViewModel() {

    companion object {
        private const val TAG_AUTH = "AuthViewModel"
        private const val TAG_SIGN_IN = "SignIn"
        private const val TAG_API_KEY = "ApiKey"
    }

    private val _idTokenFlow = MutableStateFlow<Result<String?>>(Result.success(null))
    val idTokenFlow: StateFlow<Result<String?>> get() = _idTokenFlow

    private val _accountNameFlow = MutableStateFlow<Result<String?>>(Result.success(null))
    val accountNameFlow: StateFlow<Result<String?>> get() = _accountNameFlow

    private val _apiKey = MutableStateFlow<Result<String?>>(Result.success(null))
    val apiKey: StateFlow<Result<String?>> get() = _apiKey

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            try {
                Log.d(TAG_SIGN_IN, "Sign-in start")
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    processSignInSuccess(it)
                } ?: run {
                    Log.d(TAG_SIGN_IN, "Sign-in failed: account is null")
                }
            } catch (e: ApiException) {
                Log.e(TAG_SIGN_IN, "Sign-in failed with exception: ${e.message}")
                _idTokenFlow.value = Result.failure(e)
                _accountNameFlow.value = Result.failure(e)
            }
        }
    }

    private suspend fun processSignInSuccess(account: GoogleSignInAccount) {
        val idToken = account.idToken
        if (idToken != null) {
            Log.d(TAG_SIGN_IN, "Sign-in successful. Account: ${account.displayName}, Email: ${account.email}")
            _accountNameFlow.value = Result.success(account.displayName)
            _idTokenFlow.value = Result.success(idToken)
        }

        val accessToken = authTokenProvider.getAccessToken(Account(account.email, "com.google"))
        if (accessToken != null) {
            Log.d(TAG_SIGN_IN, "AccessToken received: $accessToken")
            createApiKey(accessToken)
        } else {
            Log.e(TAG_SIGN_IN, "Failed to obtain access token")
        }
    }

    private fun createApiKey(accessToken: String) {
        viewModelScope.launch {
            googleCloudRepository.createApiKey(accessToken, projectId)
                .collect { result ->
                    result.fold(
                        onSuccess = { apiKey ->
                            _apiKey.value = Result.success(apiKey)
                            Log.d(TAG_API_KEY, "API Key created: $apiKey")
                        },
                        onFailure = { throwable ->
                            handleApiKeyCreationFailure(throwable)
                        }
                    )
                }
        }
    }

    private fun handleApiKeyCreationFailure(throwable: Throwable) {
        if (throwable is HttpException) {
            val errorBody = throwable.response()?.errorBody()?.string()
            Log.e(TAG_API_KEY, "API Key creation failed with error: $errorBody")
        } else {
            Log.e(TAG_API_KEY, "API Key creation failed with exception: ${throwable.message}")
        }
        _apiKey.value = Result.failure(throwable)
    }
}
