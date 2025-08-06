package de.rafaelbeckmann.hvkclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.CombinedVibration
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import de.rafaelbeckmann.hvkclient.data.AndroidAlarmScheduler
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmScheduler: AndroidAlarmScheduler

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val CHANNEL_ALARM = "alarm_notifications"
        private const val NOTIFICATION_ID_ALARM = 4001
        private var mediaPlayer: MediaPlayer? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        when (intent?.action) {
            "DISMISS_ALARM" -> {
                stopAlarmSound()
                stopVibrate(context)
                cancelNotification(context)
            }
            "SNOOZE_ALARM" -> {
                stopAlarmSound()
                stopVibrate(context)
                cancelNotification(context)

                val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Alarm"


                // TODO customizable time
                val alarmItem = AlarmItem(
                    time = LocalDateTime.now()
                        .plusSeconds(10.toLong()),
                    message = message
                )

                try {
                    alarmScheduler.schedule(alarmItem)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to schedule snooze alarm", e)
                }
            }
            else -> {
                val message = intent?.getStringExtra("EXTRA_MESSAGE") ?: return
                Log.i(TAG, "Alarm triggered: $message")

                createNotificationChannel(context)
                showFullScreenNotification(context, message)
            }
        }
    }

    private fun stopAlarmSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm sound", e)
        }
    }

    private fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID_ALARM)
    }

    private fun createNotificationChannel(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ALARM,
            "Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Wecker"
            enableVibration(true)
            setBypassDnd(true)
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun showFullScreenNotification(context: Context, message: String) {
        // Full screen intent
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ALARM_MESSAGE", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Content intent
        /*val contentIntent = Intent(Intent.ACTION_VIEW, "hvkclient://app".toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }*/
        val contentIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("ALARM_MESSAGE", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context, 1, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss intent
        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, 2, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze intent
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "SNOOZE_ALARM"
            putExtra("EXTRA_MESSAGE", message)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, 3, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        playAlarmSound(context)
        vibrate(context)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setDeleteIntent(dismissPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Schlummern", snoozePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stopp", dismissPendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_ALARM, notificationBuilder.build())
    }

    private fun playAlarmSound(context: Context) {
        try {
            stopAlarmSound()

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmSound)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
        }
    }

    private fun vibrate(context: Context){
        try {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator

            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(0, 400, 400, 400),
                intArrayOf(0, 0, 150, 255),
                0
            )
            val combinedVibration = CombinedVibration.createParallel(vibrationEffect)

            val attributes = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_ALARM)
                .build()

            vibratorManager.vibrate(combinedVibration, attributes)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration", e)
        }
    }
    private fun stopVibrate(context: Context) {
        try {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration", e)
        }
    }
}