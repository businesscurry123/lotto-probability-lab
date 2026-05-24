package com.lottolab.probability.domain

data class DrawResult(
    val round: Int,
    val drawDate: String,
    val mainNumbers: List<Int>,
    val bonusNumber: Int,
)

data class SavedNumberSet(
    val id: Long,
    val name: String,
    val numbers: List<Int>,
    val createdAt: Long,
    val updatedAt: Long,
    val favorite: Boolean = false,
    val collectionName: String = "기본",
)

data class MatchSummary(
    val matchedCount: Int,
    val bonusMatched: Boolean,
    val rank: PrizeRank,
)

enum class PrizeRank(val label: String) {
    FIRST("1등"),
    SECOND("2등"),
    THIRD("3등"),
    FOURTH("4등"),
    FIFTH("5등"),
    NO_PRIZE("미당첨"),
}

data class GrowthStats(
    val currentMatchCount: Int,
    val recentBestMatchCount: Int,
    val allTimeBestMatchCount: Int,
    val recentTenAverage: Double,
    val recentHundredAverage: Double,
    val sampleSize: Int,
)

data class NearMissSummary(
    val score: Int,
    val nearCount: Int,
    val exactCount: Int,
)

data class LeaderboardEntry(
    val rank: Int,
    val numberSet: SavedNumberSet,
    val match: MatchSummary,
)

data class RoundReport(
    val oddCount: Int,
    val evenCount: Int,
    val hasConsecutiveNumbers: Boolean,
    val longAbsentCount: Int,
    val hotNumberCount: Int,
    val sameFrontGroupCount: Int,
)

enum class FeatureStatus(val label: String) {
    LIVE("사용 가능"),
    READY("준비중"),
}

enum class FeatureCategory(val label: String) {
    DAILY("매일 보기"),
    MY_NUMBERS("내 번호"),
    STATISTICS("통계"),
    PROBABILITY("누적확률"),
    MAPS("지도/구간"),
    SHARE("공유/알림"),
}

data class FeatureDefinition(
    val id: String,
    val name: String,
    val category: FeatureCategory,
    val description: String,
    val status: FeatureStatus = FeatureStatus.LIVE,
)

data class DailyCombination(
    val date: String,
    val type: String,
    val numbers: List<Int>,
    val createdAt: Long,
)

data class NumberStatistic(
    val number: Int,
    val totalCount: Int,
    val recent100Count: Int,
    val recent50Count: Int,
    val recent10Count: Int,
    val absenceStreak: Int,
    val averageGap: Double,
    val status: String,
)

data class ProbabilityResult(
    val label: String,
    val n: Int,
    val probability: Double,
    val warningText: String,
)

data class CombinationMapPoint(
    val round: Int,
    val combinationIndex: Long,
    val normalizedPosition: Float,
)

data class MatchHistoryDetail(
    val round: Int,
    val date: String,
    val matchedNumbers: List<Int>,
    val missedNumbers: List<Int>,
    val drawNumbers: List<Int>,
)

data class RangeBucket(
    val label: String,
    val numbers: IntRange,
    val count: Int,
    val expectedPerRound: Double,
)

data class RangeAnalysis(
    val pattern: String,
    val buckets: List<RangeBucket>,
    val diversityScore: Int,
    val notes: List<String>,
)

data class RangeScoreBreakdown(
    val usedBucketCount: Int,
    val diversityPoints: Int,
    val concentrationPoints: Int,
    val totalScore: Int,
)

data class DigitMatchPlan(
    val modeLabel: String,
    val selectedLabel: String,
    val basisNumbers: List<Int>,
    val combinations: List<List<Int>>,
)

enum class SlipQrDisplayMode(val label: String, val description: String) {
    GROUP_BY_FIVE(
        label = "5개 묶음 보기",
        description = "저장 번호 5개를 한 페이지로 묶어서 보여줍니다.",
    ),
    SINGLE(
        label = "1개씩 보기",
        description = "저장 번호 하나씩 크게 보여줍니다.",
    ),
}

data class SlipQrPage(
    val pageIndex: Int,
    val sets: List<SavedNumberSet>,
)
