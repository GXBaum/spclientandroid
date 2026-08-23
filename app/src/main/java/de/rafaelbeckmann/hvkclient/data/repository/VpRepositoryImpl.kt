package de.rafaelbeckmann.hvkclient.data.repository

import androidx.room.withTransaction
import de.rafaelbeckmann.hvkclient.data.local.AppDatabase
import de.rafaelbeckmann.hvkclient.data.local.CacheDao
import de.rafaelbeckmann.hvkclient.data.mapEntitiesToVpDays
import de.rafaelbeckmann.hvkclient.data.remote.VpRemoteDataSource
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.DataError
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.EmptyResult
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.Result
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.asEmptyDataResult
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.map
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.onSuccess
import de.rafaelbeckmann.hvkclient.data.toDomain
import de.rafaelbeckmann.hvkclient.data.toEntity
import de.rafaelbeckmann.hvkclient.domain.model.SelectedCourse
import de.rafaelbeckmann.hvkclient.domain.model.VpDays
import de.rafaelbeckmann.hvkclient.domain.repository.VpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VpRepositoryImpl @Inject constructor(
    private val cacheDao: CacheDao,
    private val database: AppDatabase,
    private val remoteDataSource: VpRemoteDataSource
) : VpRepository {

    override fun observeSelectedCourses(): Flow<List<SelectedCourse>> {
        return cacheDao.getVpSelectedCourses().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshSelectedCourses(): EmptyResult<DataError> {
        return remoteDataSource.getSelectedCourses()
            .onSuccess { data ->
                database.withTransaction {
                    cacheDao.clearVpSelectedCourses()
                    cacheDao.insertVpSelectedCourses(data.courses.map { it.toEntity() })
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
        return cacheDao.getVpSubstitutionsForCourses(courseNames)
            .combine(cacheDao.getVpDay()) { subs, days ->
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
            }
            .asEmptyDataResult()
    }

    override suspend fun searchCourses(query: String): Result<List<String>, DataError> {
        return remoteDataSource.searchCourses(query)
            .map { response -> response.courses.map { it.name } }
    }
}