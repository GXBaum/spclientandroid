package com.rafaelbeckmann.hvkclientmitbenachrichtigungen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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

    @Inject
    lateinit var prefUtils: PrefUtils

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
                //val username = prefUtils.getString("username").toString()
                val username = "Rafael.Beckmann" // TODO: entfernen, ist aber glaube noch nötig, da das token gesendet wird, bevor der Name existiert

                Log.d("PushNotificationService", "Username: $username")

                // make TokenUpdateRequest object
                val tokenUpdateRequest = TokenUpdateRequest(token, username)
                
                Log.d("PushNotificationService", "TokenUpdateRequest: $tokenUpdateRequest")
                
                repository.updateToken(username, tokenUpdateRequest)
            } catch (e: Exception) {
                Log.e("PushNotificationService", "Failed to send token to server", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("test123", "From: " + message.from);
        if (message.getData().isNotEmpty()) {
            Log.d("test123", "Message data payload: " + message.getData());
        }

        if(message.getNotification()!=null){
            Log.d("test123","Message body : "+ message.getNotification()?.body);
        }

        if (message.data["reveal_mark"] != null) {
            Log.d("test123", "Reveal mark notification received")

            val grade = message.data["reveal_mark"]
            Log.d("test123", "Grade: $grade")

            // Create intent to launch MainActivity with grade information
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("navigate_to_reveal_mark", true)
                putExtra("grade", grade)
                //addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                //addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            // Build notification
            val notificationBuilder = NotificationCompat.Builder(this, "grade_notifications")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(message.data["title"] ?: "undefined")
                .setContentText(message.data["body"] ?: "undefined")
                .setStyle(NotificationCompat.BigTextStyle().bigText(message.data["body"] ?: "undefined")) // to not truncate the text with \n linebreaks
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create notification channel for Android O and above
            val channel = NotificationChannel(
                "grade_notifications",
                "Grade Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )

            //random notification id
            val notificationId = (System.currentTimeMillis() % 10000).toInt()
            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(notificationId, notificationBuilder.build())

        }



    }
}
