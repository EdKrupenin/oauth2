package com.example.oauth2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oauth2.viewmodel.AuthViewModel
import com.example.oauth2.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG_MAIN = "MainActivity"
        private const val TAG_SIGN_IN = "SignIn"
        private const val TAG_ACCOUNT_NAME = "AccountName"
        private const val TAG_API_KEY = "ApiKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSignInLauncher()
        setupSignInButton()
        observeIdToken()
        observeAccountName()
        observeApiKey()
    }

    private fun setupSignInLauncher() {
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG_MAIN, "Sign-in intent resultCode: ${result.resultCode}, data: ${result.data}")
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                Log.d(TAG_SIGN_IN, "Sign-in intent")
                authViewModel.handleSignInResult(task)
            } else {
                Log.e(TAG_SIGN_IN, "Sign-in failed with resultCode: ${result.resultCode}")
            }
        }
    }

    private fun setupSignInButton() {
        findViewById<Button>(R.id.signInButton).setOnClickListener {
            val signInIntent = authViewModel.getSignInIntent()
            signInLauncher.launch(signInIntent)
        }
    }

    private fun observeIdToken() {
        lifecycleScope.launch {
            authViewModel.idTokenFlow.collect { result ->
                result.fold(onSuccess = { idToken ->
                    idToken?.let {
                        Log.d(TAG_SIGN_IN, "ID Token: $idToken")
                        findViewById<TextView>(R.id.idTokenTextView).text = idToken
                    }
                }, onFailure = { error ->
                    Toast.makeText(this@MainActivity, "Sign-in failed: ${error.message}", Toast.LENGTH_LONG).show()
                })
            }
        }
    }

    private fun observeAccountName() {
        lifecycleScope.launch {
            authViewModel.accountNameFlow.collect { result ->
                result.fold(onSuccess = { accountName ->
                    accountName?.let {
                        Log.d(TAG_ACCOUNT_NAME, "Account Name: $accountName")
                        findViewById<TextView>(R.id.accountNameView).text = accountName
                    }
                }, onFailure = { error ->
                    Log.e(TAG_ACCOUNT_NAME, "Account Name failed with resultCode: $result", error)
                    Toast.makeText(this@MainActivity, "Sign-in failed: ${error.message}", Toast.LENGTH_LONG).show()
                })
            }
        }
    }

    private fun observeApiKey() {
        lifecycleScope.launch {
            authViewModel.apiKey.collect { result ->
                result.fold(onSuccess = { apiKey ->
                    apiKey?.let {
                        Log.d(TAG_API_KEY, "API Key: $apiKey")
                        findViewById<TextView>(R.id.apiKeyTextView).text = apiKey
                    }
                }, onFailure = { error ->
                    Log.e(TAG_API_KEY, "API Key failed with resultCode: $result", error)
                    Toast.makeText(this@MainActivity, "Sign-in failed: ${error.message}", Toast.LENGTH_LONG).show()
                })
            }
        }
    }
}
