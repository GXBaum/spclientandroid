package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.domain.model.UserCourse
import de.rafaelbeckmann.hvkclient.domain.model.UserMark
import kotlinx.coroutines.flow.Flow

interface CoursesRepository {
    fun observeCourses(): Flow<List<UserCourse>>
    suspend fun refreshCourses(): EmptyResult<DataError>

    fun observeCourse(courseId: Int): Flow<UserCourse?>
    suspend fun refreshCourse(courseId: Int): EmptyResult<DataError>

    fun observeMarks(courseId: Int): Flow<List<UserMark>>
    suspend fun refreshMarks(courseId: Int): EmptyResult<DataError>
}
