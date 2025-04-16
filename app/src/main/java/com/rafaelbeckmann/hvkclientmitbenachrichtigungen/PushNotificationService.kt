package com.rafaelbeckmann.hvkclientmitbenachrichtigungen

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.util.Log
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.TokenUpdateRequest
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.domaIn.repository.MyRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PushNotificationService: FirebaseMessagingService() {

    @Inject
    lateinit var repository: MyRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Store token locally (e.g., in SharedPreferences)
        /*
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
         */

        Log.d("PushNotificationService", "New token: $token")

        // Send the token to your server
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                // make TokenUpdateRequest object
                val tokenUpdateRequest = TokenUpdateRequest(token, "Rafael.Beckmann")
                
                Log.d("PushNotificationService", "TokenUpdateRequest: $tokenUpdateRequest")
                
                repository.updateToken(tokenUpdateRequest)
            } catch (e: Exception) {
                Log.e("PushNotificationService", "Failed to send token to server", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        //very custom notification behavior
    }
}
