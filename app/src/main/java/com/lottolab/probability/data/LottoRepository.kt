package com.lottolab.probability.data

import com.lottolab.probability.data.local.LottoDatabase
import com.lottolab.probability.data.local.toDomain
import com.lottolab.probability.data.local.toEntity
import com.lottolab.probability.data.remote.DhlotteryRemoteDataSource
import com.lottolab.probability.domain.DailyCombination
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.SavedNumberSet
import kotlin.math.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DrawSyncProgress(
    val cachedDrawCount: Int,
    val historyReady: Boolean,
)

class LottoRepository(
    private val database: LottoDatabase,
    private val remoteDataSource: DhlotteryRemoteDataSource,
) {
    fun observeDraws(): Flow<List<DrawResult>> = database
        .drawResultDao()
        .observeAll()
        .map { draws -> draws.map { it.toDomain() } }

    fun observeNumberSets(): Flow<List<SavedNumberSet>> = database
        .savedNumberSetDao()
        .observeAll()
        .map { numberSets -> numberSets.map { it.toDomain() } }

    fun observeDailyCombinations(date: String): Flow<List<DailyCombination>> = database
        .dailyCombinationDao()
        .observeForDate(date)
        .map { combinations -> combinations.map { it.toDomain() } }

    suspend fun getCachedDraws(): List<DrawResult> = database
        .drawResultDao()
        .getAll()
        .map { it.toDomain() }

    suspend fun syncOfficialDrawHistory(onProgress: suspend (DrawSyncProgress) -> Unit) {
        val latestRound = remoteDataSource.discoverLatestRound()
        val latestWindow = remoteDataSource.fetchWindow(latestRound)
        if (latestWindow.isEmpty()) {
            error("최신 회차 결과가 아직 제공되지 않았습니다.")
        }

        insertDraws(latestWindow)
        publishProgress(onProgress)

        var oldestRound = database.drawResultDao().oldestRound() ?: latestWindow.minOf(DrawResult::round)
        while (oldestRound > FIRST_ROUND) {
            val nextCenterRound = max(FIRST_ROUND, oldestRound - WINDOW_CENTER_OFFSET)
            val previousOldestRound = oldestRound
            val nextWindow = remoteDataSource.fetchWindow(nextCenterRound)
            if (nextWindow.isEmpty()) break

            insertDraws(nextWindow)
            oldestRound = database.drawResultDao().oldestRound() ?: previousOldestRound
            publishProgress(onProgress)
            if (oldestRound >= previousOldestRound) break

            delay(BACKFILL_PAUSE_MILLIS)
        }
    }

    suspend fun ensureDailyCombinations(date: String, draws: List<DrawResult>): List<DailyCombination> {
        val existing = database.dailyCombinationDao().getForDate(date).map { it.toDomain() }
        if (existing.size >= 3) return existing

        val generated = LottoAnalytics.dailyCombinations(date, draws)
        database.dailyCombinationDao().upsertAll(generated.map { it.toEntity() })
        return generated
    }

    suspend fun saveNumberSet(
        existing: SavedNumberSet?,
        name: String,
        numbers: Collection<Int>,
        favorite: Boolean = existing?.favorite ?: false,
        collectionName: String = existing?.collectionName ?: "기본",
    ) {
        val sortedNumbers = numbers.toSet().sorted()
        require(name.isNotBlank()) { "번호 세트 이름이 필요합니다." }
        require(sortedNumbers.size == 6) { "번호는 중복 없이 6개를 선택해야 합니다." }
        require(sortedNumbers.all { it in 1..45 }) { "번호는 1부터 45 사이여야 합니다." }
        requireNoDuplicateCombination(existing, sortedNumbers)

        val now = System.currentTimeMillis()
        val numberSet = SavedNumberSet(
            id = existing?.id ?: 0,
            name = name.trim(),
            numbers = sortedNumbers,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            favorite = favorite,
            collectionName = collectionName.ifBlank { "기본" }.trim(),
        )

        if (existing == null) {
            database.savedNumberSetDao().insert(numberSet.toEntity())
        } else {
            database.savedNumberSetDao().update(numberSet.toEntity())
        }
    }

    suspend fun toggleFavorite(numberSet: SavedNumberSet) {
        database.savedNumberSetDao().update(
            numberSet.copy(
                favorite = !numberSet.favorite,
                updatedAt = System.currentTimeMillis(),
            ).toEntity(),
        )
    }

    suspend fun deleteNumberSet(numberSet: SavedNumberSet) {
        database.savedNumberSetDao().delete(numberSet.toEntity())
    }

    private suspend fun requireNoDuplicateCombination(existing: SavedNumberSet?, sortedNumbers: List<Int>) {
        val duplicate = database
            .savedNumberSetDao()
            .getAll()
            .map { it.toDomain() }
            .firstOrNull { saved ->
                saved.id != (existing?.id ?: 0L) && saved.numbers.sorted() == sortedNumbers
            }
        require(duplicate == null) { "같은 번호 조합이 이미 저장되어 있습니다." }
    }

    private suspend fun insertDraws(draws: List<DrawResult>) {
        database.drawResultDao().upsertAll(draws.map(DrawResult::toEntity))
    }

    private suspend fun publishProgress(onProgress: suspend (DrawSyncProgress) -> Unit) {
        val oldestRound = database.drawResultDao().oldestRound()
        onProgress(
            DrawSyncProgress(
                cachedDrawCount = database.drawResultDao().count(),
                historyReady = oldestRound == FIRST_ROUND,
            ),
        )
    }

    companion object {
        private const val FIRST_ROUND = 1
        private const val WINDOW_CENTER_OFFSET = 5
        private const val BACKFILL_PAUSE_MILLIS = 35L
    }
}
