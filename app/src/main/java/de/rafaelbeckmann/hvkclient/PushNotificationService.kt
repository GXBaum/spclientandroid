package de.rafaelbeckmann.hvkclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import de.rafaelbeckmann.hvkclient.data.model.TokenUpdateRequest
import de.rafaelbeckmann.hvkclient.domain.repository.HvkRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: komplett neu machen
@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject lateinit var repository: HvkRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    // Single coroutine scope for the service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "PushNotificationService"

        // Notification channels
        private const val CHANNEL_GRADES = "grade_notifications"
        private const val CHANNEL_VP_UPDATES = "vp_updates"
        private const val CHANNEL_OTHER = "other_notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")

        serviceScope.launch {
            val userId = settingsRepository.getUserId()
            if (userId != null) {
                sendTokenToServer(token, userId)
            } else {
                // TODO: no explanation needed
                Log.d(TAG, "Token updated, will send when user logs in")
            }
        }
    }

    private fun sendTokenToServer(token: String, userId: Int) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Sending token to server for user: $userId")

                // make TokenUpdateRequest object
                val tokenUpdateRequest = TokenUpdateRequest(token, userId)
                repository.updateToken(userId, tokenUpdateRequest)

                Log.d(TAG, "Token sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send token to server", e)
                // TODO: Store the failed update to retry later
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

        val otherChannel = NotificationChannel(
            CHANNEL_OTHER,
            "Andere Benachrichtigungen",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Andere Benachrichtigungen"
            enableVibration(false)
        }

        notificationManager.createNotificationChannels(listOf(gradeChannel, vpChannel, otherChannel))
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
        }

        if (message.notification != null) {
            Log.d(TAG, "Message body: ${message.notification?.body}")
        }

        handleNotification(message)
    }

    private fun handleNotification(message: RemoteMessage) {
        Log.d(TAG, "notification received: ${message.data}")

        // TODO: improve this code
        // TODO: let more notification settings be controlled by the server
        val channelId = when (message.data["channel_id"]){
            CHANNEL_GRADES -> CHANNEL_GRADES
            CHANNEL_VP_UPDATES -> CHANNEL_VP_UPDATES
            else -> CHANNEL_OTHER
        }

        val title = message.data["title"] ?: "unbekannte Benachrichtigung"
        val body = message.data["body"] ?: "bitte gib Bescheid, um das Problem zu beheben"
        val notificationId = message.data["notification_id"]?.toIntOrNull()?: (System.currentTimeMillis() % 10000).toInt()
        // TODO: hvkclient://app ist vlt dumm
        val deepLinkUri = (message.data["deepLink"] ?: "hvkclient://app").toUri()

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = buildNotification(channelId, title, body, pendingIntent)
        showNotification(builder, notificationId)
    }

    private fun buildNotification(channelId: String, title: String, body: String, pendingIntent: PendingIntent): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // TODO: maybe make this adaptable?
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // TODO: muss das anders sein bei z.B other notifications?
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION) // TODO: changed to fix Android System Intelligence recommendations (open Map) // TODO: muss das anders sein bei nicht-Nachrichten
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }
    private fun showNotification(builder: NotificationCompat.Builder, notificationId: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}