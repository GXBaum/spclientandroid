package de.rafaelbeckmann.hvkclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
import de.rafaelbeckmann.hvkclient.domain.repository.MyRepository
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

    companion object {
        private const val TAG = "PushNotificationService"
        private const val CHANNEL_GRADES = "grade_notifications"
        private const val CHANNEL_VP_UPDATES = "vp_updates"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "New token: $token")

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            prefUtils.saveString("fcm_token", token)

            // Only send token to server if user is already logged in
            val username = "Rafael.Beckmann" // TODO: REMOVE, get username from shared preferences
            //val username = prefUtils.getString("username")
            if (!username.isNullOrEmpty()) {
                sendTokenToServer(token, username)
            } else {
                Log.d(TAG, "Token stored locally, will send when user logs in")
            }
        }
    }

    private fun sendTokenToServer(token: String, username: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                Log.d(TAG, "Sending token to server for user: $username")

                // make TokenUpdateRequest object
                val tokenUpdateRequest = TokenUpdateRequest(token, username)
                repository.updateToken(username, tokenUpdateRequest)

                Log.d(TAG, "Token sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send token to server", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Grade notifications channel
        val gradeChannel = NotificationChannel(
            CHANNEL_GRADES,
            "SP Noten",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Benachrichtigungen für neue Schulportal Noten"
            enableVibration(true)
        }

        // VP updates channel
        val vpChannel = NotificationChannel(
            CHANNEL_VP_UPDATES,
            "Vertretungsplan",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Benachrichtigungen für den Vertretungsplan"
            enableVibration(true)
        }

        notificationManager.createNotificationChannels(listOf(gradeChannel, vpChannel))
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "From: ${message.from}")
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
        }

        if (message.notification != null) {
            Log.d(TAG, "Message body: ${message.notification?.body}")
        }

        when {
            message.data["mark"] != null -> {
                handleGradeNotification(message)
            }
            message.data["open_vp"] != null -> {
                handleVpUpdateNotification(message)
            }
            else -> {
                Log.d(TAG, "Received notification of unknown type")
            }
        }
    }

    private fun handleGradeNotification(message: RemoteMessage) {
        Log.d(TAG, "Reveal mark notification received")

        Log.d(TAG, "Full data payload in handler: ${message.data}")

        val grade = message.data["mark"]
        Log.d(TAG, "Grade: $grade")

        // Create intent to launch MainActivity with grade information
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to_reveal_mark", true)
            putExtra("grade", grade)
            // Add these flags to ensure proper navigation when app is closed
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_GRADES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(message.data["title"] ?: "Neue Note")
            .setContentText(message.data["body"] ?: "Eine neue Note ist verfügbar")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.data["body"] ?: "Eine neue Note ist verfügbar"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        showNotification(notificationBuilder)
    }

    private fun handleVpUpdateNotification(message: RemoteMessage) {
        Log.d(TAG, "VP update notification received")

        // Create intent to launch MainActivity with VP screen destination
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to_vp", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_VP_UPDATES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(message.data["title"] ?: "Vertretungsplan Update")
            .setContentText(message.data["body"] ?: "Der Vertretungsplan wurde aktualisiert")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.data["body"] ?: "Der Vertretungsplan wurde aktualisiert"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        showNotification(notificationBuilder)
    }

    private fun showNotification(builder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(notificationId, builder.build())
    }
}