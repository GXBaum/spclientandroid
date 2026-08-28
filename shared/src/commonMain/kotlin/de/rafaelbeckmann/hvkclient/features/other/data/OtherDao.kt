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
}