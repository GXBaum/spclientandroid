package de.rafaelbeckmann.hvkclient.data.repository

import androidx.room.withTransaction
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.asEmptyDataResult
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.remote.CoursesRemoteDataSource
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoder
import de.rafaelbeckmann.hvkclient.data.toDomain
import de.rafaelbeckmann.hvkclient.data.toEntity
import de.rafaelbeckmann.hvkclient.domain.model.UserCourse
import de.rafaelbeckmann.hvkclient.domain.model.UserMark
import de.rafaelbeckmann.hvkclient.domain.repository.CoursesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CoursesRepositoryImpl @Inject constructor(
    private val cacheDao: CacheDao,
    private val database: AppDatabase,
    private val remoteDataSource: CoursesRemoteDataSource,
    private val payloadDecoder: PayloadDecoder
) : CoursesRepository {
    override fun observeCourses(): Flow<List<UserCourse>> {
        return cacheDao.getUserCourses().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshCourses(): EmptyResult<DataError> {
        return remoteDataSource.getCourses()
            .onSuccess { response ->
                val parsed = payloadDecoder
                    .decodeUserCourses(response.courses)
                    .map { it.toEntity() }

                database.withTransaction {
                    cacheDao.clearUserCourses()
                    cacheDao.insertUserCourses(parsed)
                }
            }
            .asEmptyDataResult()
    }

    override fun observeCourse(courseId: Int): Flow<UserCourse?> {
        return cacheDao.getUserCourseById(courseId).map {
            it?.toDomain()
        }
    }

    override suspend fun refreshCourse(courseId: Int): EmptyResult<DataError> {
        return remoteDataSource.getCourse(courseId)
            .onSuccess {
                cacheDao.insertUserCourses(listOf(it.course.toEntity()))
            }
            .asEmptyDataResult()
    }

    override fun observeMarks(courseId: Int): Flow<List<UserMark>> {
        return cacheDao.getUserMarksForCourse(courseId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshMarks(courseId: Int): EmptyResult<DataError> {
        return remoteDataSource.getMarks(courseId)
            .onSuccess { response ->
                // improve this
                database.withTransaction {
                    cacheDao.deleteUserMarksForCourse(courseId)
                    cacheDao.insertUserMarks(response.marks.map {it.toEntity()})
                }
            }
            .asEmptyDataResult()
    }

}
