package de.rafaelbeckmann.hvkclient.features.vp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// TODO: maybe replace replace with ignore?
@Dao
interface VpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVpSelectedCourses(courses: List<VpSelectedCourseEntity>)

    @Query("SELECT * FROM vpselectedcourseentity")
    fun getVpSelectedCourses(): Flow<List<VpSelectedCourseEntity>>

    @Query("DELETE FROM vpselectedcourseentity")
    suspend fun clearVpSelectedCourses()


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVpSubstitutions(substitutions: List<VpSubstitutionEntity>)

    @Query("SELECT * FROM VpSubstitutionEntity WHERE courseName IN (:courseNames)")
    fun getVpSubstitutionsForCourses(courseNames: List<String>): Flow<List<VpSubstitutionEntity>>

    @Query("DELETE FROM vpsubstitutionentity")
    suspend fun clearVpSubstitutionsCache()

    @Query("DELETE FROM vpsubstitutionentity WHERE courseName IN (:courseNames)")
    suspend fun deleteVpSubstitutionsForCourses(courseNames: List<String>)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVpDay(days: List<VpDayEntity>)

    @Transaction
    @Query("SELECT * FROM vpdayentity")
    fun getVpDay(): Flow<List<VpDayWithInfo>>

    @Query("DELETE FROM vpdayentity")
    suspend fun clearVpDay()


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVpDayInfoItems(items: List<VpDayInfoItem>)

    @Query("DELETE FROM VpDayInfoItem")
    suspend fun deleteVpDayInfo()

}