package com.lottolab.probability.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawResultDao {
    @Query("SELECT * FROM draw_results ORDER BY round DESC")
    fun observeAll(): Flow<List<DrawResultEntity>>

    @Query("SELECT * FROM draw_results ORDER BY round DESC")
    suspend fun getAll(): List<DrawResultEntity>

    @Query("SELECT MIN(round) FROM draw_results")
    suspend fun oldestRound(): Int?

    @Query("SELECT COUNT(*) FROM draw_results")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(draws: List<DrawResultEntity>)
}

@Dao
interface SavedNumberSetDao {
    @Query("SELECT * FROM saved_number_sets ORDER BY favorite DESC, updatedAt DESC, id DESC")
    fun observeAll(): Flow<List<SavedNumberSetEntity>>

    @Query("SELECT * FROM saved_number_sets ORDER BY favorite DESC, updatedAt DESC, id DESC")
    suspend fun getAll(): List<SavedNumberSetEntity>

    @Insert
    suspend fun insert(numberSet: SavedNumberSetEntity): Long

    @Update
    suspend fun update(numberSet: SavedNumberSetEntity)

    @Delete
    suspend fun delete(numberSet: SavedNumberSetEntity)
}

@Dao
interface DailyCombinationDao {
    @Query("SELECT * FROM daily_combinations WHERE date = :date ORDER BY type")
    fun observeForDate(date: String): Flow<List<DailyCombinationEntity>>

    @Query("SELECT * FROM daily_combinations WHERE date = :date ORDER BY type")
    suspend fun getForDate(date: String): List<DailyCombinationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(combinations: List<DailyCombinationEntity>)
}
