package com.lottolab.probability.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LottoAnalyticsTest {
    private val winningDraw = draw(
        round = 1224,
        numbers = listOf(9, 18, 21, 27, 44, 45),
        bonus = 28,
    )

    @Test
    fun matchAssignsLotteryRanks() {
        assertEquals(
            PrizeRank.FIRST,
            LottoAnalytics.match(listOf(9, 18, 21, 27, 44, 45), winningDraw).rank,
        )
        assertEquals(
            PrizeRank.SECOND,
            LottoAnalytics.match(listOf(9, 18, 21, 27, 44, 28), winningDraw).rank,
        )
        assertEquals(
            PrizeRank.THIRD,
            LottoAnalytics.match(listOf(9, 18, 21, 27, 44, 1), winningDraw).rank,
        )
        assertEquals(
            PrizeRank.FOURTH,
            LottoAnalytics.match(listOf(9, 18, 21, 27, 1, 2), winningDraw).rank,
        )
        assertEquals(
            PrizeRank.FIFTH,
            LottoAnalytics.match(listOf(9, 18, 21, 1, 2, 3), winningDraw).rank,
        )
        assertEquals(
            PrizeRank.NO_PRIZE,
            LottoAnalytics.match(listOf(1, 2, 3, 4, 5, 6), winningDraw).rank,
        )
    }

    @Test
    fun growthStatsUseAvailableRecentDraws() {
        val draws = listOf(
            draw(3, listOf(1, 2, 3, 4, 5, 6)),
            draw(2, listOf(1, 2, 7, 8, 9, 10)),
            draw(1, listOf(1, 11, 12, 13, 14, 15)),
        )

        val stats = LottoAnalytics.growthStats(listOf(1, 2, 3, 20, 21, 22), draws, currentRound = 3)

        requireNotNull(stats)
        assertEquals(3, stats.currentMatchCount)
        assertEquals(3, stats.recentBestMatchCount)
        assertEquals(3, stats.allTimeBestMatchCount)
        assertEquals(2.0, stats.recentTenAverage, 0.001)
        assertEquals(2.0, stats.recentHundredAverage, 0.001)
        assertEquals(3, stats.sampleSize)
    }

    @Test
    fun leaderboardKeepsTiesAtSameRank() {
        val draw = draw(10, listOf(1, 2, 3, 4, 5, 6))
        val entries = LottoAnalytics.leaderboard(
            numberSets = listOf(
                savedSet(1, "B", listOf(1, 2, 8, 9, 10, 11)),
                savedSet(2, "A", listOf(1, 2, 3, 9, 10, 11)),
                savedSet(3, "C", listOf(1, 2, 7, 12, 13, 14)),
            ),
            draw = draw,
        )

        assertEquals(listOf("A", "B", "C"), entries.map { it.numberSet.name })
        assertEquals(listOf(1, 2, 2), entries.map { it.rank })
    }

    @Test
    fun nearMissScoresExactAndPlusMinusOneNumbers() {
        val draw = draw(100, listOf(5, 12, 19, 28, 33, 41))
        val allNear = LottoAnalytics.nearMiss(listOf(6, 11, 18, 27, 34, 42), draw)
        val exactAndFar = LottoAnalytics.nearMiss(listOf(5, 1, 2, 3, 7, 45), draw)

        assertEquals(92, allNear.score)
        assertEquals(6, allNear.nearCount)
        assertEquals(0, allNear.exactCount)
        assertEquals(17, exactAndFar.score)
        assertEquals(1, exactAndFar.exactCount)
    }

    @Test
    fun nearMissDoesNotReuseExactWinningNumberForNeighborScore() {
        val draw = draw(1224, listOf(9, 18, 21, 27, 44, 45))

        val summary = LottoAnalytics.nearMiss(listOf(4, 5, 21, 22, 32, 33), draw)

        assertEquals(1, summary.exactCount)
        assertEquals(0, summary.nearCount)
        assertEquals(17, summary.score)
    }

    @Test
    fun roundReportUsesPriorTenFrequenciesAndFrontGroups() {
        val selected = draw(20, listOf(4, 5, 12, 18, 27, 44))
        val priorDraws = listOf(
            draw(19, listOf(4, 11, 12, 21, 31, 41)),
            draw(18, listOf(7, 11, 12, 22, 32, 42)),
            draw(17, listOf(8, 13, 14, 23, 33, 43)),
        )

        val report = LottoAnalytics.roundReport(selected, priorDraws + selected)

        assertEquals(2, report.oddCount)
        assertEquals(4, report.evenCount)
        assertTrue(report.hasConsecutiveNumbers)
        assertEquals(4, report.longAbsentCount)
        assertEquals(1, report.hotNumberCount)
        assertEquals(2, report.sameFrontGroupCount)
    }

    @Test
    fun roundReportDetectsNoConsecutiveNumbers() {
        val report = LottoAnalytics.roundReport(
            draw(10, listOf(1, 3, 5, 20, 32, 44)),
            priorDraws = emptyList(),
        )

        assertFalse(report.hasConsecutiveNumbers)
    }

    @Test
    fun dailyCombinationsAreStableForSameDate() {
        val draws = (1..12).map { round ->
            draw(round, listOf(round, round + 1, round + 2, 30, 40, 45).map { ((it - 1) % 45) + 1 })
        }

        val first = LottoAnalytics.dailyCombinations("2026-05-23", draws, createdAt = 1)
        val second = LottoAnalytics.dailyCombinations("2026-05-23", draws, createdAt = 1)
        val nextDay = LottoAnalytics.dailyCombinations("2026-05-24", draws, createdAt = 1)

        assertEquals(first, second)
        assertEquals(3, first.size)
        assertTrue(first.all { it.numbers.size == 6 && it.numbers.toSet().size == 6 })
        assertFalse(first.first().numbers == nextDay.first().numbers)
    }

    @Test
    fun numberStatisticsCalculateRecentCountsAndAbsence() {
        val draws = listOf(
            draw(3, listOf(1, 2, 3, 4, 5, 6)),
            draw(2, listOf(1, 7, 8, 9, 10, 11)),
            draw(1, listOf(12, 13, 14, 15, 16, 17)),
        )

        val stats = LottoAnalytics.numberStatistics(draws).associateBy { it.number }

        assertEquals(2, stats.getValue(1).totalCount)
        assertEquals(0, stats.getValue(1).absenceStreak)
        assertEquals(3, stats.getValue(45).absenceStreak)
    }

    @Test
    fun cumulativeProbabilityUsesOneMinusFailurePower() {
        val probability = LottoAnalytics.specificNumberProbability(10)

        assertEquals(0.7609, probability, 0.001)
        assertEquals(0.0001228, LottoAnalytics.sameCombinationFirstPrizeProbability(1000), 0.000001)
    }

    @Test
    fun combinationIndexUsesLexicographicOrder() {
        assertEquals(1L, LottoAnalytics.combinationIndex(listOf(1, 2, 3, 4, 5, 6)))
        assertEquals(8_145_060L, LottoAnalytics.combinationIndex(listOf(40, 41, 42, 43, 44, 45)))
    }

    @Test
    fun rangeTemplateCreatesSixNumbersInRequestedBuckets() {
        val numbers = LottoAnalytics.rangeTemplateNumbers(listOf(1, 1, 1, 2, 1), seed = 7)
        val analysis = LottoAnalytics.rangeAnalysis(numbers)

        assertEquals(6, numbers.size)
        assertEquals(6, numbers.toSet().size)
        assertEquals("1-1-1-2-1", analysis.pattern)
    }

    @Test
    fun cooccurrenceCountsOnlyRoundsContainingSelectedNumber() {
        val draws = listOf(
            draw(3, listOf(7, 12, 20, 21, 22, 23)),
            draw(2, listOf(7, 12, 29, 30, 31, 32)),
            draw(1, listOf(1, 12, 29, 34, 35, 36)),
        )

        val ranking = LottoAnalytics.cooccurrence(7, draws)

        assertEquals(12 to 2, ranking.first())
        assertFalse(ranking.any { it.first == 7 })
    }

    @Test
    fun mainDisplayShowsOnlyFavoriteNumberSets() {
        val sets = listOf(
            savedSet(1, "A", listOf(1, 2, 3, 4, 5, 6), favorite = false),
            savedSet(2, "B", listOf(7, 8, 9, 10, 11, 12), favorite = true),
            savedSet(3, "C", listOf(13, 14, 15, 16, 17, 18), favorite = true),
        )

        val mainSets = LottoDisplayRules.mainNumberSets(sets)

        assertEquals(listOf("B", "C"), mainSets.map { it.name })
    }

    @Test
    fun slipQrPagesUseConfiguredGrouping() {
        val sets = (1L..11L).map { id ->
            savedSet(id, "Set$id", listOf(1, 2, 3, 4, 5, ((id.toInt() + 5 - 1) % 40) + 6))
        }

        val groupedPages = SlipQrPlanner.pages(sets, SlipQrDisplayMode.GROUP_BY_FIVE)
        val singlePages = SlipQrPlanner.pages(sets, SlipQrDisplayMode.SINGLE)

        assertEquals(listOf(5, 5, 1), groupedPages.map { it.sets.size })
        assertEquals(11, singlePages.size)
        assertEquals(emptyList<SlipQrPage>(), SlipQrPlanner.pages(emptyList(), SlipQrDisplayMode.GROUP_BY_FIVE))
    }

    @Test
    fun combinationMapIndexesSavedAndInputNumbersWithSameRule() {
        val saved = savedSet(1, "Saved", listOf(4, 8, 15, 16, 23, 42))
        val directInput = listOf(4, 8, 15, 16, 23, 42)

        assertEquals(
            LottoAnalytics.combinationIndex(saved.numbers),
            LottoAnalytics.combinationIndex(directInput),
        )
    }

    @Test
    fun specificNumberProbabilityDoesNotChangeByNumberStatus() {
        val draws = listOf(
            draw(3, listOf(7, 8, 9, 10, 11, 12)),
            draw(2, listOf(7, 13, 14, 15, 16, 17)),
            draw(1, listOf(18, 19, 20, 21, 22, 23)),
        )
        val stats = LottoAnalytics.numberStatistics(draws).associateBy { it.number }

        assertEquals("최근 강세", stats.getValue(7).status)
        assertEquals("보통", stats.getValue(1).status)
        assertEquals(
            LottoAnalytics.specificNumberProbability(10),
            LottoAnalytics.specificNumberProbability(10),
            0.0,
        )
    }

    @Test
    fun cumulativeRecommendationsAvoidExcludedShownCombinations() {
        val draws = listOf(
            draw(3, listOf(1, 2, 3, 4, 5, 6)),
            draw(2, listOf(7, 8, 9, 10, 11, 12)),
            draw(1, listOf(13, 14, 15, 16, 17, 18)),
        )
        val first = LottoAnalytics.cumulativeRecommendationCombinations("today", draws, count = 1)
        val excluded = first.map { it.sorted().joinToString("-") }.toSet()
        val next = LottoAnalytics.cumulativeRecommendationCombinations("today", draws, count = 1, excludedKeys = excluded)

        assertEquals(1, first.size)
        assertEquals(1, next.size)
        assertFalse(excluded.contains(next.first().sorted().joinToString("-")))
    }

    @Test
    fun scatteredNumbersReturnExactSetWhenSixNumbersAreSelected() {
        val numbers = listOf(3, 11, 18, 27, 34, 42)
        val result = LottoAnalytics.scatteredNumberCombinations("scatter", emptyList(), numbers)

        assertEquals(listOf(numbers.sorted()), result)
    }

    @Test
    fun digitMatcherCreatesFiveSixNumberCombinations() {
        val draws = (1..20).map { round ->
            draw(round, listOf(round, round + 3, round + 6, round + 9, round + 12, round + 15).map { ((it - 1) % 45) + 1 })
        }

        val plan = LottoAnalytics.digitMatchPlan("digit", draws, frontDigitMode = true, count = 5)

        assertEquals("앞자리", plan.modeLabel)
        assertEquals(5, plan.combinations.size)
        assertTrue(plan.combinations.all { it.size == 6 && it.toSet().size == 6 })
    }

    @Test
    fun rangeScoreBreakdownMatchesRangeAnalysisScore() {
        val numbers = listOf(1, 2, 11, 21, 31, 41)
        val analysis = LottoAnalytics.rangeAnalysis(numbers)
        val breakdown = LottoAnalytics.rangeScoreBreakdown(numbers)

        assertEquals(analysis.diversityScore, breakdown.totalScore)
        assertEquals(5, breakdown.usedBucketCount)
        assertEquals(80, breakdown.diversityPoints)
        assertEquals(20, breakdown.concentrationPoints)
    }

    private fun draw(round: Int, numbers: List<Int>, bonus: Int = 45): DrawResult = DrawResult(
        round = round,
        drawDate = "20260516",
        mainNumbers = numbers,
        bonusNumber = bonus,
    )

    private fun savedSet(
        id: Long,
        name: String,
        numbers: List<Int>,
        favorite: Boolean = false,
    ): SavedNumberSet = SavedNumberSet(
        id = id,
        name = name,
        numbers = numbers,
        createdAt = 1,
        updatedAt = 1,
        favorite = favorite,
    )
}
