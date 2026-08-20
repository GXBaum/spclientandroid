package de.rafaelbeckmann.hvkclient.data.repository

import androidx.room.withTransaction
import de.rafaelbeckmann.hvkclient.data.local.AppDatabase
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.remote.HvkClientApi
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoder
import de.rafaelbeckmann.hvkclient.data.toDomain
import de.rafaelbeckmann.hvkclient.data.toEntity
import de.rafaelbeckmann.hvkclient.domain.model.UserCourse
import de.rafaelbeckmann.hvkclient.domain.model.UserMark
import de.rafaelbeckmann.hvkclient.domain.repository.CoursesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CoursesRepositoryImpl @Inject constructor(
    private val api: HvkClientApi,
    private val cacheDao: CacheDao,
    private val database: AppDatabase,
    private val payloadDecoder: PayloadDecoder
) : CoursesRepository {
    override fun observeCourses(): Flow<List<UserCourse>> {
        return cacheDao.getUserCourses().map {
            it.map { it.toDomain() }
        }
    }

    override suspend fun refreshCourses(): Result<Unit> = try {
        val response = api.getUserCourses()

        if (!response.isSuccessful) {
            return Result.failure(
                IllegalStateException("Could not fetch courses: ${response.code()}")
            )
        }

        val body = requireNotNull(response.body()) {
            "Courses response successful but had no body"
        }

        val parsed = payloadDecoder
            .decodeUserCourses(body.courses)
            .map { it.toEntity() }

        // improve this later
        database.withTransaction {
            cacheDao.clearUserCourses()
            cacheDao.insertUserCourses(parsed)
        }

        return Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

    override fun observeCourse(courseId: Int): Flow<UserCourse?> {
        return cacheDao.getUserCourseById(courseId).map {
            it?.toDomain()
        }
    }

    override suspend fun refreshCourse(courseId: Int): Result<Unit> = try {
        val response = api.getUserCourseById(courseId)

        if (!response.isSuccessful) {
            return Result.failure(
                IllegalStateException("Could not fetch course: ${response.code()}")
            )
        }

        val body = requireNotNull(response.body()) {
            "Course response successful but had no body"
        }

        val parsed = body.course.toEntity()

        cacheDao.insertUserCourses(listOf(parsed))

        return Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }


    override fun observeMarks(courseId: Int): Flow<List<UserMark>> {
        return cacheDao.getUserMarksForCourse(courseId).map {
            it.map { it.toDomain() }
        }
    }

    override suspend fun refreshMarks(courseId: Int): Result<Unit> = try {
        val response = api.getUserMarksForCourse(courseId)

        if (!response.isSuccessful) {
            return Result.failure(
                IllegalStateException("Could not fetch marks: ${response.code()}")
            )
        }

        val body = requireNotNull(response.body()) {
            "Marks response successful but had no body"
        }

        val parsed = body.marks.map { it.toEntity() }

        // improve this
        database.withTransaction {
            cacheDao.deleteUserMarksForCourse(courseId)
            cacheDao.insertUserMarks(parsed)
        }

        return Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }

}
