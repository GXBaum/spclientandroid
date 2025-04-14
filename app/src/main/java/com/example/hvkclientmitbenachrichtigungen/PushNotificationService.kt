package com.example.hvkclientmitbenachrichtigungen

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.util.Log
import com.example.TokenUpdateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PushNotificationService: FirebaseMessagingService() {

    /*override fun onNewToken(token: String) {
        super.onNewToken(token)
        // push new token to server

    }*/
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Store token locally (e.g., in SharedPreferences)
        /*
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
         */

        // Log the token for debugging
        Log.d("PushNotificationService", "New token: $token")

        // Send the token to your server
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                // make TokenUpdateRequest object
                val tokenUpdateRequest = TokenUpdateRequest(token, "Rafael.Beckmann")

                Log.d("PushNotificationService", "TokenUpdateRequest: $tokenUpdateRequest")

                FcmApiClient.getApi().updateToken(tokenUpdateRequest)
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