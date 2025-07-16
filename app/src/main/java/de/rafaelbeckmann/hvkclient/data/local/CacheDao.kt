package de.rafaelbeckmann.hvkclient.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsCache
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCourses(courses: List<UserCourse>)

    @Query("SELECT * FROM usercourse")
    fun getUserCourses(): Flow<List<UserCourse>>

    @Query("DELETE FROM usercourse")
    suspend fun clearUserCourses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserMarks(marks: List<UserMark>)

    @Query("DELETE FROM usermark WHERE course_id = :courseId")
    suspend fun deleteUserMarksForCourse(courseId: Int)

    @Query("SELECT * FROM usermark WHERE course_id = :courseId")
    fun getUserMarksForCourse(courseId: Int): Flow<List<UserMark>>

    @Query("DELETE FROM usermark")
    suspend fun clearUserMarks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVpSelectedCourses(courses: List<VpSelectedCourse>)

    @Query("SELECT * FROM vpselectedcourse")
    fun getVpSelectedCourses(): Flow<List<VpSelectedCourse>>

    @Query("DELETE FROM vpselectedcourse")
    suspend fun clearVpSelectedCourses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVpSubstitutionsCache(cache: VpSubstitutionsCache)

    @Query("SELECT * FROM vp_substitutions_cache WHERE courseName IN (:courseNames)")
    fun getVpSubstitutionsForCourses(courseNames: List<String>): Flow<List<VpSubstitutionsCache>>

    @Query("DELETE FROM vp_substitutions_cache")
    suspend fun clearVpSubstitutionsCache()

    @Query("DELETE FROM vp_substitutions_cache WHERE courseName IN (:courseNames)")
    suspend fun deleteVpSubstitutionsForCourses(courseNames: List<String>)

    @Transaction
    suspend fun clearAllCache() {
        clearUserCourses()
        clearUserMarks()
        clearVpSelectedCourses()
        clearVpSubstitutionsCache()
    }
}