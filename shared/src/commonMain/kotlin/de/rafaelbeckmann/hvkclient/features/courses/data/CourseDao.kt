package de.rafaelbeckmann.hvkclient.features.courses.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// TODO: maybe replace replace with ignore?
@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCourses(courses: List<UserCourseEntity>)

    @Query("SELECT * FROM usercourseentity")
    fun getUserCourses(): Flow<List<UserCourseEntity>>

    @Query("SELECT * FROM usercourseentity WHERE courseId = :courseId")
    fun getUserCourseById(courseId: Int): Flow<UserCourseEntity?>

    @Query("DELETE FROM usercourseentity")
    suspend fun clearUserCourses()

    @Transaction
    suspend fun clearAndInsertCourses(courses: List<UserCourseEntity>) {
        clearUserCourses()
        insertUserCourses(courses)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserMarks(marks: List<UserMarkEntity>)

    @Query("DELETE FROM usermarkentity WHERE course_id = :courseId")
    suspend fun deleteUserMarksForCourse(courseId: Int)

    @Transaction
    suspend fun clearAndInsertUserMarks(courseId: Int, marks: List<UserMarkEntity>) {
        deleteUserMarksForCourse(courseId)
        insertUserMarks(marks)
    }

    @Query("SELECT * FROM usermarkentity WHERE course_id = :courseId")
    fun getUserMarksForCourse(courseId: Int): Flow<List<UserMarkEntity>>

    @Query("DELETE FROM usermarkentity")
    suspend fun clearUserMarks()
}