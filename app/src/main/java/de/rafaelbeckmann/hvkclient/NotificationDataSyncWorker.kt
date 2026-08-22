package de.rafaelbeckmann.hvkclient

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.rafaelbeckmann.hvkclient.PushNotificationService.Companion.CHANNEL_VP_UPDATES
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.onError
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.onSuccess
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.domain.repository.VpRepository
import kotlinx.coroutines.flow.first

// TODO: improve this (mehrere API calls, mehr Sachen synchronisieren)

// TODO: top 3 wege eine ddos Attacke zu erstellen
@HiltWorker
class NotificationDataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val vpRepository: VpRepository
): CoroutineWorker(context, workerParams) {
    companion object {
        private const val TAG = "NotificationDataSyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started")
        return try {
            val channelId = inputData.getString("channel_id") ?: return Result.failure()

            when (channelId) {
                CHANNEL_VP_UPDATES -> {
                    val coursesResource = vpRepository.refreshSelectedCourses()
                    val courses = vpRepository.observeSelectedCourses().first()

                    coursesResource
                        .onSuccess {
                            if (courses.isNotEmpty()) {
                                vpRepository.refreshSubstitutions(courses.map { it.name })
                                    .onError {
                                        Log.w(TAG, "Failed to fetch substitutions")
                                        return Result.failure()
                                    }
                            }
                        }
                        .onError {
                            Log.w(TAG, "Failed to fetch courses")
                            return Result.failure()
                        }
                }
            }

            Log.d(TAG, "Worker success")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in worker", e)
            Result.failure()
        }
    }
}