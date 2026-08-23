package de.rafaelbeckmann.hvkclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import de.rafaelbeckmann.hvkclient.domain.repository.AuthRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: komplett neu machen
@AndroidEntryPoint
class PushNotificationService : FirebaseMessagingService() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    // Single coroutine scope for the service
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "PushNotificationService"

        // Notification channels
        const val CHANNEL_GRADES = "grade_notifications"
        const val CHANNEL_VP_UPDATES = "vp_updates"
        const val CHANNEL_OTHER = "other_notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")

        serviceScope.launch {
            // TODO: this fails on signup since you're not registered/logged in but it doesn't matter, it gets resent on signup
            sendTokenToServer(token)
        }
    }

    private fun sendTokenToServer(token: String) {
        serviceScope.launch {
            try {
                Log.d(TAG, "Sending token to server")

                authRepository.addNotificationToken(token)

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

        // update local database to the newest data (otherwise data will only load on app start, and without connection this will be older than this notification
        scheduleSyncUserDataTask(channelId)

        val title = message.data["title"] ?: "unbekannte Benachrichtigung"
        val body = message.data["body"] ?: "bitte gib Bescheid, um das Problem zu beheben"
        val notificationTag = message.data["notification_tag"]
        val notificationId = if (!notificationTag.isNullOrEmpty()) 0 else (System.currentTimeMillis() % 10000).toInt()
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
        showNotification(builder, notificationTag, notificationId)
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
    private fun showNotification(builder: NotificationCompat.Builder, notificationTag: String? = null, notificationId: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationTag, notificationId, builder.build())
    }

    fun scheduleSyncUserDataTask(channelId: String) {
        // TODO: one request for every notification (how to ddos yourself 101)
        // TODO: the worker may for example be skipped in battery saver. unsure if this is good or not
        val inputData = workDataOf("channel_id" to channelId)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<NotificationDataSyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueue(syncWorkRequest)
    }
}
