package de.rafaelbeckmann.hvkclient.features.other.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// TODO: maybe replace replace with ignore?
@Dao
interface OtherDao {
    @Query("SELECT * FROM feature_flag")
    fun getFeatureFlags(): Flow<List<FeatureFlagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeatureFlags(flags: List<FeatureFlagEntity>)

    @Query("DELETE FROM feature_flag")
    suspend fun clearFeatureFlags()

    @Transaction
    suspend fun clearAndUpsertFeatureFlags(flags: List<FeatureFlagEntity>) {
        clearFeatureFlags()
        upsertFeatureFlags(flags)
    }



    @Query("DELETE FROM usercourseentity")
    suspend fun clearUserCourses()
    @Query("DELETE FROM usermarkentity")
    suspend fun clearUserMarks()
    @Query("DELETE FROM vpselectedcourseentity")
    suspend fun clearVpSelectedCourses()
    @Query("DELETE FROM vpsubstitutionentity")
    suspend fun clearVpSubstitutions()
    @Query("DELETE FROM vpdayentity")
    suspend fun clearVpDays()
    @Query("DELETE FROM vpdayinfoitem")
    suspend fun clearVpInfos()

    // TODO: this sucks hard but i don't care right now
    @Transaction
    suspend fun clearAllTablesButManualAgainThanksToKmp() {
        clearUserCourses()
        clearUserMarks()
        clearVpSelectedCourses()
        clearVpSubstitutions()
        clearVpDays()
        clearVpInfos()
        clearFeatureFlags()
    }

}