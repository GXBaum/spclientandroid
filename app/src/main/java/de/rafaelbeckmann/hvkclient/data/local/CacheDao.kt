package de.rafaelbeckmann.hvkclient.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.rafaelbeckmann.hvkclient.data.model.FeatureFlagEntity
import de.rafaelbeckmann.hvkclient.data.model.UserCourseEntity
import de.rafaelbeckmann.hvkclient.data.model.UserMarkEntity
import de.rafaelbeckmann.hvkclient.data.model.VpDayEntity
import de.rafaelbeckmann.hvkclient.data.model.VpDayInfoItem
import de.rafaelbeckmann.hvkclient.data.model.VpDayWithInfo
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourseEntity
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionEntity
import kotlinx.coroutines.flow.Flow

// TODO: maybe replace replace with ignore?
@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCourses(courses: List<UserCourseEntity>)

    @Query("SELECT * FROM usercourseentity")
    fun getUserCourses(): Flow<List<UserCourseEntity>>

    @Query("SELECT * FROM usercourseentity WHERE courseId = :courseId")
    fun getUserCourseById(courseId: Int): Flow<UserCourseEntity>

    @Query("DELETE FROM usercourseentity")
    suspend fun clearUserCourses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserMarks(marks: List<UserMarkEntity>)

    @Query("DELETE FROM usermarkentity WHERE course_id = :courseId")
    suspend fun deleteUserMarksForCourse(courseId: Int)

    @Query("SELECT * FROM usermarkentity WHERE course_id = :courseId")
    fun getUserMarksForCourse(courseId: Int): Flow<List<UserMarkEntity>>

    @Query("DELETE FROM usermarkentity")
    suspend fun clearUserMarks()

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

    // Feature flags
    @Query("SELECT * FROM feature_flag")
    fun getFeatureFlags(): Flow<List<FeatureFlagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeatureFlags(flags: List<FeatureFlagEntity>)

    @Query("DELETE FROM feature_flag")
    suspend fun clearFeatureFlags()

}
