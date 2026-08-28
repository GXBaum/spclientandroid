package de.rafaelbeckmann.hvkclient.features.courses.data

import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.asEmptyDataResult
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.data.remote.PayloadDecoder
import de.rafaelbeckmann.hvkclient.features.courses.domain.CoursesRepository
import de.rafaelbeckmann.hvkclient.features.courses.domain.UserCourse
import de.rafaelbeckmann.hvkclient.features.courses.domain.UserMark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [CoursesRepository::class])
class CoursesRepositoryImpl(
    private val dao: CourseDao,
    private val database: AppDatabase,
    private val remoteDataSource: CoursesRemoteDataSource,
    private val payloadDecoder: PayloadDecoder
) : CoursesRepository {
    override fun observeCourses(): Flow<List<UserCourse>> {
        return dao.getUserCourses().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshCourses(): EmptyResult<DataError> {
        return remoteDataSource.getCourses()
            .onSuccess { response ->
                val parsed = payloadDecoder
                    .decodeUserCourses(response.courses)
                    .map { it.toEntity() }

                dao.clearAndInsertCourses(parsed)
            }
            .asEmptyDataResult()
    }

    override fun observeCourse(courseId: Int): Flow<UserCourse?> {
        return dao.getUserCourseById(courseId).map {
            it?.toDomain()
        }
    }

    override suspend fun refreshCourse(courseId: Int): EmptyResult<DataError> {
        return remoteDataSource.getCourse(courseId)
            .onSuccess {
                dao.insertUserCourses(listOf(it.course.toEntity()))
            }
            .asEmptyDataResult()
    }

    override fun observeMarks(courseId: Int): Flow<List<UserMark>> {
        return dao.getUserMarksForCourse(courseId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshMarks(courseId: Int): EmptyResult<DataError> {
        return remoteDataSource.getMarks(courseId)
            .onSuccess { response ->
                // improve this
                dao.clearAndInsertUserMarks(courseId, response.marks.map {it.toEntity()})
            }
            .asEmptyDataResult()
    }

}