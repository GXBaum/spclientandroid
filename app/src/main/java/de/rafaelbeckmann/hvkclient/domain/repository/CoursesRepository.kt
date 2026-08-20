package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.domain.model.UserCourse
import de.rafaelbeckmann.hvkclient.domain.model.UserMark
import kotlinx.coroutines.flow.Flow

interface CoursesRepository {
    fun observeCourses(): Flow<List<UserCourse>>
    suspend fun refreshCourses(): Result<Unit>

    fun observeCourse(courseId: Int): Flow<UserCourse?>
    suspend fun refreshCourse(courseId: Int): Result<Unit>

    fun observeMarks(courseId: Int): Flow<List<UserMark>>
    suspend fun refreshMarks(courseId: Int): Result<Unit>
}
