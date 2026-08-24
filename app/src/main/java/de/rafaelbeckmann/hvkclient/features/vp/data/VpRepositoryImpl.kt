package de.rafaelbeckmann.hvkclient.features.vp.data

import androidx.room.withTransaction
import de.rafaelbeckmann.hvkclient.core.database.AppDatabase
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.EmptyResult
import de.rafaelbeckmann.hvkclient.core.domain.Result
import de.rafaelbeckmann.hvkclient.core.domain.asEmptyDataResult
import de.rafaelbeckmann.hvkclient.core.domain.map
import de.rafaelbeckmann.hvkclient.core.domain.onSuccess
import de.rafaelbeckmann.hvkclient.features.vp.domain.SelectedCourse
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpDays
import de.rafaelbeckmann.hvkclient.features.vp.domain.VpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VpRepositoryImpl @Inject constructor(
    private val dao: VpDao,
    private val database: AppDatabase,
    private val remoteDataSource: VpRemoteDataSource
) : VpRepository {

    override fun observeSelectedCourses(): Flow<List<SelectedCourse>> {
        return dao.getVpSelectedCourses().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshSelectedCourses(): EmptyResult<DataError> {
        return remoteDataSource.getSelectedCourses()
            .onSuccess { data ->
                database.withTransaction {
                    dao.clearVpSelectedCourses()
                    dao.insertVpSelectedCourses(data.courses.map { it.toEntity() })
                }
            }
            .asEmptyDataResult()
    }

    override suspend fun addSelectedCourse(courseName: String): EmptyResult<DataError> {
        val result = remoteDataSource.postSelectedCourse(courseName)
        return when (result) {
            is Result.Error -> result.asEmptyDataResult()
            is Result.Success -> refreshSelectedCourses()
        }
    }

    override suspend fun removeSelectedCourse(courseId: String): EmptyResult<DataError> {
        val result = remoteDataSource.deleteSelectedCourse(courseId)
        return when (result) {
            is Result.Error -> result.asEmptyDataResult()
            is Result.Success -> refreshSelectedCourses()
        }
    }

    override fun observeSubstitutions(courseNames: List<String>): Flow<VpDays> {
        return dao.getVpSubstitutionsForCourses(courseNames)
            .combine(dao.getVpDay()) { subs, days ->
                mapEntitiesToVpDays(days, subs)
            }
    }

    override suspend fun refreshSubstitutions(courseNames: List<String>): EmptyResult<DataError> {
        return remoteDataSource.getSubstitutions(courseNames)
            .onSuccess {
                val parsed = it.substitutions
                database.withTransaction {
                    // First delete existing entries for these courses
                    // this is dumb, but nothing lasts longer than a temporary fix
                    dao.deleteVpSubstitutionsForCourses(courseNames)
                    dao.deleteVpDayInfo()
                    dao.clearVpDay()

                    dao.insertVpDay(
                        listOfNotNull(parsed.today, parsed.tomorrow)
                            .map { it.toEntity() }
                    )

                    val substitutions = listOfNotNull(parsed.today?.substitutions, parsed.tomorrow?.substitutions)
                            .flatten()
                            .map { it.toEntity() }
                    dao.insertVpSubstitutions(substitutions)

                    val infos = listOfNotNull(parsed.today?.info, parsed.tomorrow?.info)
                        .flatten()
                        .map { it.toEntity() }
                    dao.insertVpDayInfoItems(infos)
                }
            }
            .asEmptyDataResult()
    }

    override suspend fun searchCourses(query: String): Result<List<String>, DataError> {
        return remoteDataSource.searchCourses(query)
            .map { response -> response.courses.map { it.name } }
    }
}