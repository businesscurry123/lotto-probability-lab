package com.lottolab.probability.domain

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

object LottoAnalytics {
    private const val TOTAL_COMBINATIONS = 8_145_060L
    private val rangeDefinitions = listOf(
        "1~9" to (1..9),
        "10~19" to (10..19),
        "20~29" to (20..29),
        "30~39" to (30..39),
        "40~45" to (40..45),
    )

    fun match(numbers: List<Int>, draw: DrawResult): MatchSummary {
        val selected = numbers.toSet()
        val matchedCount = draw.mainNumbers.count(selected::contains)
        val bonusMatched = draw.bonusNumber in selected
        val rank = when {
            matchedCount == 6 -> PrizeRank.FIRST
            matchedCount == 5 && bonusMatched -> PrizeRank.SECOND
            matchedCount == 5 -> PrizeRank.THIRD
            matchedCount == 4 -> PrizeRank.FOURTH
            matchedCount == 3 -> PrizeRank.FIFTH
            else -> PrizeRank.NO_PRIZE
        }

        return MatchSummary(
            matchedCount = matchedCount,
            bonusMatched = bonusMatched,
            rank = rank,
        )
    }

    fun growthStats(numbers: List<Int>, draws: List<DrawResult>, currentRound: Int): GrowthStats? {
        val sortedDraws = draws.sortedByDescending(DrawResult::round)
        if (sortedDraws.isEmpty()) return null

        val matches = sortedDraws.map { match(numbers, it).matchedCount }
        val recentTen = matches.take(10)
        val recentHundred = matches.take(100)
        val currentDraw = sortedDraws.firstOrNull { it.round == currentRound } ?: sortedDraws.first()

        return GrowthStats(
            currentMatchCount = match(numbers, currentDraw).matchedCount,
            recentBestMatchCount = recentTen.maxOrNull() ?: 0,
            allTimeBestMatchCount = matches.maxOrNull() ?: 0,
            recentTenAverage = recentTen.averageOrZero(),
            recentHundredAverage = recentHundred.averageOrZero(),
            sampleSize = matches.size,
        )
    }

    fun leaderboard(numberSets: List<SavedNumberSet>, draw: DrawResult): List<LeaderboardEntry> {
        var previousMatch: Int? = null
        var previousRank = 0

        return numberSets
            .map { it to match(it.numbers, draw) }
            .sortedWith(
                compareByDescending<Pair<SavedNumberSet, MatchSummary>> { it.second.matchedCount }
                    .thenBy { it.first.name }
                    .thenBy { it.first.id },
            )
            .mapIndexed { index, (numberSet, match) ->
                val rank = if (previousMatch == match.matchedCount) {
                    previousRank
                } else {
                    index + 1
                }
                previousMatch = match.matchedCount
                previousRank = rank
                LeaderboardEntry(rank, numberSet, match)
            }
    }

    fun nearMiss(numbers: List<Int>, draw: DrawResult): NearMissSummary {
        val selectedNumbers = numbers.toSet()
        val exactNumbers = selectedNumbers.intersect(draw.mainNumbers.toSet())
        val unmatchedWinningNumbers = draw.mainNumbers
            .filterNot(exactNumbers::contains)
            .toMutableSet()
        var nearCount = 0

        val scores = numbers.map { selectedNumber ->
            if (selectedNumber in exactNumbers) {
                100
            } else {
                val pairedWinningNumber = unmatchedWinningNumbers
                    .sortedBy { abs(it - selectedNumber) }
                    .firstOrNull { abs(it - selectedNumber) == 1 }

                if (pairedWinningNumber != null) {
                    unmatchedWinningNumbers.remove(pairedWinningNumber)
                    nearCount += 1
                    92
                } else {
                    0
                }
            }
        }

        return NearMissSummary(
            score = scores.averageOrZero().roundToInt(),
            nearCount = nearCount,
            exactCount = exactNumbers.size,
        )
    }

    fun roundReport(draw: DrawResult, priorDraws: List<DrawResult>): RoundReport {
        val priorTen = priorDraws
            .filter { it.round < draw.round }
            .sortedByDescending(DrawResult::round)
            .take(10)
        val frequencies = priorTen
            .flatMap(DrawResult::mainNumbers)
            .groupingBy { it }
            .eachCount()
        val sortedNumbers = draw.mainNumbers.sorted()
        val oddCount = sortedNumbers.count { it % 2 == 1 }

        return RoundReport(
            oddCount = oddCount,
            evenCount = sortedNumbers.size - oddCount,
            hasConsecutiveNumbers = sortedNumbers.zipWithNext().any { (left, right) -> right - left == 1 },
            longAbsentCount = sortedNumbers.count { frequencies[it] == null },
            hotNumberCount = sortedNumbers.count { (frequencies[it] ?: 0) >= 2 },
            sameFrontGroupCount = sortedNumbers
                .groupingBy(::frontGroup)
                .eachCount()
                .values
                .maxOrNull() ?: 0,
        )
    }

    fun dailyCombinations(
        date: String,
        draws: List<DrawResult>,
        createdAt: Long = System.currentTimeMillis(),
    ): List<DailyCombination> {
        val stats = numberStatistics(draws).associateBy(NumberStatistic::number)
        val hotNumbers = numberStatistics(draws)
            .sortedWith(compareByDescending<NumberStatistic> { it.recent100Count }.thenBy { it.number })
            .take(15)
            .map(NumberStatistic::number)
            .toSet()

        val randomCombination = deterministicPick(
            seed = "$date:random".hashCode(),
            candidates = (1..45).toList(),
        )
        val longAbsentCombination = (1..45)
            .sortedWith(
                compareByDescending<Int> { stats[it]?.absenceStreak ?: draws.size }
                    .thenBy { stats[it]?.recent100Count ?: 0 }
                    .thenBy { it },
            )
            .take(6)
            .sorted()
        val popularExcludedCombination = deterministicPick(
            seed = "$date:exclude-hot".hashCode(),
            candidates = (1..45).filterNot(hotNumbers::contains).ifEmpty { (1..45).toList() },
        )

        return listOf(
            DailyCombination(date, "오늘의 랜덤 조합", randomCombination, createdAt),
            DailyCombination(date, "오늘의 장기 미출현 조합", longAbsentCombination, createdAt),
            DailyCombination(date, "오늘의 인기번호 제외 조합", popularExcludedCombination, createdAt),
        )
    }

    fun cumulativeRecommendationCombinations(
        seedKey: String,
        draws: List<DrawResult>,
        count: Int = 5,
        excludedKeys: Set<String> = emptySet(),
    ): List<List<Int>> {
        val stats = numberStatistics(draws).associateBy(NumberStatistic::number)
        val result = mutableListOf<List<Int>>()
        var salt = 0

        while (result.size < count && salt < 240) {
            val random = Random("$seedKey:$salt".hashCode())
            val candidate = (1..45)
                .map { number ->
                    val absence = stats[number]?.absenceStreak ?: draws.size
                    number to (cumulativeScore(absence) + random.nextDouble(0.0, 0.08))
                }
                .sortedWith(compareByDescending<Pair<Int, Double>> { it.second }.thenBy { it.first })
                .take(6)
                .map { it.first }
                .sorted()
            val key = combinationKey(candidate)
            if (key !in excludedKeys && result.none { combinationKey(it) == key }) {
                result += candidate
            }
            salt += 1
        }

        return result
    }

    fun scatteredNumberCombinations(
        seedKey: String,
        draws: List<DrawResult>,
        fixedNumbers: Collection<Int>,
        count: Int = 5,
    ): List<List<Int>> {
        val fixed = fixedNumbers
            .filter { it in 1..45 }
            .distinct()
            .take(6)
            .sorted()

        if (fixed.size == 6) return listOf(fixed)

        return cumulativeRecommendationCombinations(
            seedKey = "scattered:$seedKey:${fixed.joinToString("-")}",
            draws = draws,
            count = count * 4,
        )
            .map { candidate -> (fixed + candidate).distinct().take(6).sorted() }
            .filter { it.size == 6 }
            .distinctBy(::combinationKey)
            .take(count)
    }

    fun digitMatchPlan(
        seedKey: String,
        draws: List<DrawResult>,
        frontDigitMode: Boolean,
        count: Int = 5,
    ): DigitMatchPlan {
        val stats = numberStatistics(draws).associateBy(NumberStatistic::number)
        val groups = if (frontDigitMode) {
            rangeDefinitions.map { (label, range) -> "${label} 번호대" to range.toList() }
        } else {
            (0..9).map { digit ->
                "끝자리 ${digit}" to (1..45).filter { it % 10 == digit }
            }.filter { it.second.isNotEmpty() }
        }

        val selected = groups.maxWith(
            compareBy<Pair<String, List<Int>>> { (_, numbers) ->
                numbers
                    .map { number -> cumulativeScore(stats[number]?.absenceStreak ?: draws.size) }
                    .let { scores -> if (scores.isEmpty()) 0.0 else scores.average() }
            }.thenBy { it.first },
        )
        val preferredNumbers = selected.second.toSet()
        val combinations = mutableListOf<List<Int>>()
        var salt = 0

        while (combinations.size < count && salt < 240) {
            val random = Random("$seedKey:${selected.first}:$salt".hashCode())
            val rankedPreferred = selected.second
                .map { number ->
                    number to (cumulativeScore(stats[number]?.absenceStreak ?: draws.size) + random.nextDouble(0.0, 0.08))
                }
                .sortedByDescending { it.second }
                .map { it.first }
            val rankedOthers = (1..45)
                .filterNot(preferredNumbers::contains)
                .map { number ->
                    number to (cumulativeScore(stats[number]?.absenceStreak ?: draws.size) + random.nextDouble(0.0, 0.08))
                }
                .sortedByDescending { it.second }
                .map { it.first }
            val preferredTake = minOf(3, rankedPreferred.size)
            val candidate = (rankedPreferred.take(preferredTake) + rankedOthers)
                .distinct()
                .take(6)
                .sorted()
            if (candidate.size == 6 && combinations.none { combinationKey(it) == combinationKey(candidate) }) {
                combinations += candidate
            }
            salt += 1
        }

        return DigitMatchPlan(
            modeLabel = if (frontDigitMode) "앞자리" else "뒷자리",
            selectedLabel = selected.first,
            basisNumbers = selected.second,
            combinations = combinations,
        )
    }

    fun numberStatistics(draws: List<DrawResult>): List<NumberStatistic> {
        val sortedDesc = draws.sortedByDescending(DrawResult::round)
        val totalFrequencies = draws.flatMap(DrawResult::mainNumbers).groupingBy { it }.eachCount()
        val recent100 = sortedDesc.take(100).flatMap(DrawResult::mainNumbers).groupingBy { it }.eachCount()
        val recent50 = sortedDesc.take(50).flatMap(DrawResult::mainNumbers).groupingBy { it }.eachCount()
        val recent10 = sortedDesc.take(10).flatMap(DrawResult::mainNumbers).groupingBy { it }.eachCount()

        return (1..45).map { number ->
            val appearanceRounds = draws
                .filter { number in it.mainNumbers }
                .map(DrawResult::round)
                .sorted()
            val gaps = appearanceRounds.zipWithNext { left, right -> right - left }
            val absence = sortedDesc.indexOfFirst { number in it.mainNumbers }
                .let { if (it == -1) sortedDesc.size else it }
            val recentTenCount = recent10[number] ?: 0
            val status = when {
                absence >= 20 -> "장기 미출현"
                recentTenCount >= 2 -> "최근 강세"
                absence >= 10 -> "미출현 관찰"
                else -> "보통"
            }

            NumberStatistic(
                number = number,
                totalCount = totalFrequencies[number] ?: 0,
                recent100Count = recent100[number] ?: 0,
                recent50Count = recent50[number] ?: 0,
                recent10Count = recentTenCount,
                absenceStreak = absence,
                averageGap = gaps.averageOrZero(),
                status = status,
            )
        }
    }

    fun matchHistoryDetails(
        numbers: List<Int>,
        draws: List<DrawResult>,
        minMatches: Int,
    ): List<MatchHistoryDetail> = draws
        .sortedByDescending(DrawResult::round)
        .mapNotNull { draw ->
            val matchedNumbers = numbers.sorted().filter { it in draw.mainNumbers }
            if (matchedNumbers.size < minMatches) {
                null
            } else {
                MatchHistoryDetail(
                    round = draw.round,
                    date = draw.drawDate,
                    matchedNumbers = matchedNumbers,
                    missedNumbers = numbers.sorted().filterNot(matchedNumbers::contains),
                    drawNumbers = draw.mainNumbers.sorted(),
                )
            }
        }

    fun combinationMapPoints(draws: List<DrawResult>, limit: Int = 240): List<CombinationMapPoint> =
        draws
            .sortedByDescending(DrawResult::round)
            .take(limit)
            .map { draw ->
                val index = combinationIndex(draw.mainNumbers)
                CombinationMapPoint(
                    round = draw.round,
                    combinationIndex = index,
                    normalizedPosition = (index.toDouble() / TOTAL_COMBINATIONS.toDouble()).toFloat(),
                )
            }

    fun combinationIndex(numbers: List<Int>): Long {
        val sorted = numbers.toSet().sorted()
        require(sorted.size == 6) { "조합 인덱스는 중복 없는 6개 번호가 필요합니다." }
        require(sorted.all { it in 1..45 }) { "번호는 1부터 45 사이여야 합니다." }

        var rank = 0L
        var previous = 0
        sorted.forEachIndexed { index, value ->
            for (candidate in previous + 1 until value) {
                rank += combinations(45 - candidate, 6 - index - 1)
            }
            previous = value
        }
        return rank + 1L
    }

    fun cooccurrence(number: Int, draws: List<DrawResult>): List<Pair<Int, Int>> {
        require(number in 1..45) { "번호는 1부터 45 사이여야 합니다." }
        return draws
            .filter { number in it.mainNumbers }
            .flatMap { it.mainNumbers.filterNot { other -> other == number } }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedWith(compareByDescending<Pair<Int, Int>> { it.second }.thenBy { it.first })
    }

    fun rangeAnalysis(numbers: List<Int>): RangeAnalysis {
        val buckets = rangeDefinitions.map { (label, range) ->
            RangeBucket(
                label = label,
                numbers = range,
                count = numbers.count { it in range },
                expectedPerRound = range.count().toDouble() * 6.0 / 45.0,
            )
        }
        val maxBucket = buckets.maxByOrNull(RangeBucket::count)
        val emptyBuckets = buckets.filter { it.count == 0 }.map(RangeBucket::label)
        val breakdown = rangeScoreBreakdownFromBuckets(buckets)
        val notes = buildList {
            if (emptyBuckets.isNotEmpty()) add("${emptyBuckets.joinToString(", ")} 구간이 비어 있습니다.")
            if ((maxBucket?.count ?: 0) >= 3) add("${maxBucket?.label} 구간에 번호가 몰려 있습니다.")
            add("구간 밸런스는 번호 구성을 보기 쉽게 만든 참고 지표입니다.")
        }

        return RangeAnalysis(
            pattern = buckets.joinToString("-") { it.count.toString() },
            buckets = buckets,
            diversityScore = breakdown.totalScore,
            notes = notes,
        )
    }

    fun rangeScoreBreakdown(numbers: List<Int>): RangeScoreBreakdown =
        rangeScoreBreakdownFromBuckets(rangeAnalysisBuckets(numbers))

    fun rangePatternAppearances(pattern: String, draws: List<DrawResult>, limit: Int): Int =
        draws
            .sortedByDescending(DrawResult::round)
            .take(limit)
            .count { rangeAnalysis(it.mainNumbers).pattern == pattern }

    fun rangeExpectationDelta(draws: List<DrawResult>, limit: Int = 100): List<Pair<RangeBucket, Double>> {
        val targetDraws = draws.sortedByDescending(DrawResult::round).take(limit)
        if (targetDraws.isEmpty()) {
            return rangeAnalysis(emptyList()).buckets.map { it to 0.0 }
        }

        return rangeDefinitions.map { (_, range) ->
            val bucket = RangeBucket(
                label = rangeLabel(range),
                numbers = range,
                count = targetDraws.flatMap(DrawResult::mainNumbers).count { it in range },
                expectedPerRound = range.count().toDouble() * 6.0 / 45.0,
            )
            val expected = bucket.expectedPerRound * targetDraws.size
            val deltaPercent = if (expected == 0.0) 0.0 else ((bucket.count - expected) / expected) * 100.0
            bucket to deltaPercent
        }
    }

    fun rangeTemplateNumbers(template: List<Int>, seed: Int): List<Int> {
        require(template.size == 5) { "구간 템플릿은 5개 구간 개수가 필요합니다." }
        require(template.sum() == 6) { "구간 템플릿 합계는 6이어야 합니다." }

        val random = Random(seed)
        return template.flatMapIndexed { index, count ->
            rangeDefinitions[index].second.shuffled(random).take(count)
        }.sorted()
    }

    fun cumulativeProbability(singleTrialProbability: Double, trials: Int): Double {
        if (trials <= 0 || singleTrialProbability <= 0.0) return 0.0
        if (singleTrialProbability >= 1.0) return 1.0
        return 1.0 - (1.0 - singleTrialProbability).pow(trials)
    }

    fun specificNumberProbability(trials: Int): Double = cumulativeProbability(6.0 / 45.0, trials)

    fun sameCombinationFirstPrizeProbability(trials: Int): Double =
        cumulativeProbability(1.0 / TOTAL_COMBINATIONS.toDouble(), trials)

    fun fiveOrMoreMainNumberProbability(trials: Int): Double {
        val singleTrial = (combinations(6, 5) * combinations(39, 1) + combinations(6, 6)).toDouble() /
            TOTAL_COMBINATIONS.toDouble()
        return cumulativeProbability(singleTrial, trials)
    }

    fun multipleTicketFirstPrizeProbability(ticketCount: Int, trials: Int): Double {
        val perRoundProbability = (ticketCount.coerceAtLeast(0).toDouble() / TOTAL_COMBINATIONS.toDouble())
            .coerceAtMost(1.0)
        return cumulativeProbability(perRoundProbability, trials)
    }

    fun trialsNeededForProbability(targetProbability: Double, singleTrialProbability: Double): Int {
        if (targetProbability <= 0.0 || singleTrialProbability <= 0.0) return 0
        if (targetProbability >= 1.0) return Int.MAX_VALUE
        return ceil(ln(1.0 - targetProbability) / ln(1.0 - singleTrialProbability)).toInt()
    }

    private fun frontGroup(number: Int): Int = when (number) {
        in 1..9 -> 0
        in 10..19 -> 1
        in 20..29 -> 2
        in 30..39 -> 3
        else -> 4
    }

    private fun rangeAnalysisBuckets(numbers: List<Int>): List<RangeBucket> =
        rangeDefinitions.map { (label, range) ->
            RangeBucket(
                label = label,
                numbers = range,
                count = numbers.count { it in range },
                expectedPerRound = range.count().toDouble() * 6.0 / 45.0,
            )
        }

    private fun rangeScoreBreakdownFromBuckets(buckets: List<RangeBucket>): RangeScoreBreakdown {
        val usedBucketCount = buckets.count { it.count > 0 }
        val maxCount = buckets.maxOfOrNull(RangeBucket::count) ?: 0
        val diversityPoints = ((usedBucketCount / buckets.size.toDouble()) * 80.0).roundToInt()
        val concentrationPoints = if (maxCount <= 2) 20 else 8
        val totalScore = (diversityPoints + concentrationPoints).coerceIn(0, 100)
        return RangeScoreBreakdown(
            usedBucketCount = usedBucketCount,
            diversityPoints = diversityPoints,
            concentrationPoints = concentrationPoints,
            totalScore = totalScore,
        )
    }

    private fun deterministicPick(seed: Int, candidates: List<Int>): List<Int> =
        candidates.shuffled(Random(seed)).take(6).sorted()

    private fun cumulativeScore(absenceStreak: Int): Double =
        specificNumberProbability(absenceStreak.coerceAtLeast(0))

    private fun combinationKey(numbers: List<Int>): String =
        numbers.sorted().joinToString("-")

    private fun combinations(n: Int, r: Int): Long {
        if (r < 0 || n < r) return 0L
        val k = minOf(r, n - r)
        var result = 1L
        for (i in 1..k) {
            result = result * (n - k + i) / i
        }
        return result
    }

    private fun rangeLabel(range: IntRange): String = rangeDefinitions
        .firstOrNull { it.second == range }
        ?.first
        ?: "${range.first}~${range.last}"
}

private fun List<Int>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()
