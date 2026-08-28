package de.rafaelbeckmann.hvkclient.features.vp.domain

import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface VpRepository {
    fun observeSelectedCourses(): Flow<List<SelectedCourse>>
    suspend fun refreshSelectedCourses(): EmptyResult<DataError>

    suspend fun addSelectedCourse(courseName: String): EmptyResult<DataError>
    suspend fun removeSelectedCourse(courseId: String): EmptyResult<DataError>

    fun observeSubstitutions(courseNames: List<String>): Flow<VpDays>
    suspend fun refreshSubstitutions(courseNames: List<String>): EmptyResult<DataError>

    suspend fun searchCourses(query: String): Result<List<String>, DataError>
}