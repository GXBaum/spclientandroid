package de.rafaelbeckmann.hvkclient.data.repository

import androidx.room.withTransaction
import de.rafaelbeckmann.hvkclient.data.local.AppDatabase
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.mapEntitiesToVpDays
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCourseRequest
import de.rafaelbeckmann.hvkclient.data.toDomain
import de.rafaelbeckmann.hvkclient.data.toEntity
import de.rafaelbeckmann.hvkclient.domain.model.SelectedCourse
import de.rafaelbeckmann.hvkclient.domain.model.VpDays
import de.rafaelbeckmann.hvkclient.domain.repository.VpRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VpRepositoryImpl @Inject constructor(
    private val api: HvkClientApi,
    private val cacheDao: CacheDao,
    private val database: AppDatabase
) : VpRepository {
    override fun observeSelectedCourses(): Flow<List<SelectedCourse>> {
        return cacheDao.getVpSelectedCourses().map {
            it.map { it.toDomain() }
        }
    }

    override suspend fun refreshSelectedCourses(): Result<Unit> = try {
        val response = api.getVpSelectedCourses()

        if (!response.isSuccessful) {
            return Result.failure(
                IllegalStateException("Could not fetch selected courses: ${response.code()}")
            )
        }

        val body = requireNotNull(response.body()) {
            "selected courses response successful but had no body"
        }

        val parsed = body.courses.map { it.toEntity() }

        // improve this later
        database.withTransaction {
            cacheDao.clearVpSelectedCourses()

            cacheDao.insertVpSelectedCourses(parsed)
        }

        return Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    override suspend fun addSelectedCourse(courseName: String): Result<Unit> = try {
        val response = api.postVpSelectedCourses(
            NetworkVpSelectedCourseRequest(courseName)
        )

        if (!response.isSuccessful) {
            return Result.failure(
                IllegalStateException("Could not add selected course: ${response.code()}")
            )
        }

        refreshSelectedCourses() // improve this maybe
        // don't return success as refresh already does that
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    override suspend fun removeSelectedCourse(courseId: String): Result<Unit> = try {
        val response = api.deleteVpSelectedCourse(courseId)

        if (!response.isSuccessful) {
            return Result.failure(
                IllegalStateException("Could not delete selected course: ${response.code()}")
            )
        }

        refreshSelectedCourses() // improve this maybe
        // don't return success as refresh already does that
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    override fun observeSubstitutions(courseNames: List<String>): Flow<VpDays> {
        return cacheDao.getVpSubstitutionsForCourses(courseNames).combine(cacheDao.getVpDay()) { subs, days ->
            mapEntitiesToVpDays(days, subs)
        }
    }

    override suspend fun refreshSubstitutions(courseNames: List<String>): Result<Unit> = try {
        val response = api.getVpSubstitutionsMultipleCourses(courseNames)

        if (!response.isSuccessful) {
            return Result.failure(
                IllegalStateException("Could not fetch substitutions: ${response.code()}")
            )
        }

        val body = requireNotNull(response.body()) {
            "substitutions response successful but had no body"
        }

        val parsed = body.substitutions

        // improve this later
        database.withTransaction {
            // First delete existing entries for these courses
            // this is dumb, but nothing lasts longer than a temporary fix
            cacheDao.deleteVpSubstitutionsForCourses(courseNames)
            cacheDao.deleteVpDayInfo()
            cacheDao.clearVpDay()

            cacheDao.insertVpDay(
                listOfNotNull(parsed.today, parsed.tomorrow)
                    .map { it.toEntity() }
            )

            val substitutions = listOfNotNull(parsed.today?.substitutions, parsed.tomorrow?.substitutions)
                .flatten()
                .map { it.toEntity() }
            cacheDao.insertVpSubstitutions(substitutions)


            val infos = listOfNotNull(parsed.today?.info, parsed.tomorrow?.info)
                .flatten()
                .map { it.toEntity() }
            cacheDao.insertVpDayInfoItems(infos)
        }

        return Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

}