package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.domain.model.SelectedCourse
import de.rafaelbeckmann.hvkclient.domain.model.VpDays
import kotlinx.coroutines.flow.Flow

interface VpRepository {
    fun observeSelectedCourses(): Flow<List<SelectedCourse>>
    suspend fun refreshSelectedCourses(): Result<Unit>

    suspend fun addSelectedCourse(courseName: String): Result<Unit>
    suspend fun removeSelectedCourse(courseId: String): Result<Unit>

    fun observeSubstitutions(courseNames: List<String>): Flow<VpDays>
    suspend fun refreshSubstitutions(courseNames: List<String>): Result<Unit>
}