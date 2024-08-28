package com.example.oauth2.auth

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthTokenProvider @Inject constructor(private val context: Context) {

    companion object {
        private const val TAG_AUTH = "AuthTokenProvider"
        private const val SCOPES =
            "oauth2:profile email openid https://www.googleapis.com/auth/cloud-platform"
    }

    suspend fun getAccessToken(account: Account): String? {
        return try {
            withContext(Dispatchers.IO) {
                GoogleAuthUtil.getToken(context, account, SCOPES)
            }
        } catch (e: Exception) {
            Log.e(
                TAG_AUTH,
                "Failed to retrieve access token for account: ${account.name}. Error: ${e.localizedMessage}",
                e
            )
            null
        }
    }
}
