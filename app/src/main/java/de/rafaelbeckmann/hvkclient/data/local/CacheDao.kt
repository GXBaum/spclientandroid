package de.rafaelbeckmann.hvkclient.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.rafaelbeckmann.hvkclient.data.model.UserCourse
import de.rafaelbeckmann.hvkclient.data.model.UserMark
import de.rafaelbeckmann.hvkclient.data.model.VpSelectedCourse
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitution
import de.rafaelbeckmann.hvkclient.data.model.VpSubstitutionsAllCache
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
    suspend fun insertVpSelectedCourse(course: VpSelectedCourse)

    @Query("SELECT * FROM vpselectedcourse LIMIT 1")
    fun getVpSelectedCourse(): Flow<VpSelectedCourse?>

    @Query("DELETE FROM vpselectedcourse")
    suspend fun clearVpSelectedCourse()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVpSubstitutions(substitutions: List<VpSubstitution>)

    @Query("DELETE FROM vpsubstitution")
    suspend fun deleteVpSubstitutions()

    @Query("SELECT * FROM vpsubstitution")
    fun getVpSubstitutions(): Flow<List<VpSubstitution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVpSubstitutionsAll(substitutions: VpSubstitutionsAllCache)

    @Query("SELECT * FROM vp_substitutions_all WHERE courseName = :courseName")
    fun getVpSubstitutionsAll(courseName: String): Flow<VpSubstitutionsAllCache?>

    @Query("DELETE FROM vp_substitutions_all")
    suspend fun clearVpSubstitutionsAll()

    @Transaction
    suspend fun clearAllCache() {
        clearUserCourses()
        clearUserMarks()
        clearVpSelectedCourse()
        deleteVpSubstitutions()
        clearVpSubstitutionsAll()
    }
}