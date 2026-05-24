package com.lottolab.probability.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.NumberStatistic
import java.util.Locale

@Composable
internal fun NumberStatsFeature(draws: List<DrawResult>) {
    val minRound = draws.minOfOrNull(DrawResult::round) ?: 1
    val maxRound = draws.maxOfOrNull(DrawResult::round) ?: 1
    var startRoundText by remember(minRound) { mutableStateOf(minRound.toString()) }
    var endRoundText by remember(maxRound) { mutableStateOf(maxRound.toString()) }
    var includeBonus by remember { mutableStateOf(false) }
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    val startRound = startRoundText.toIntOrNull()?.coerceIn(minRound, maxRound) ?: minRound
    val endRound = endRoundText.toIntOrNull()?.coerceIn(minRound, maxRound) ?: maxRound
    val rangeStart = minOf(startRound, endRound)
    val rangeEnd = maxOf(startRound, endRound)
    val filteredDraws = remember(draws, rangeStart, rangeEnd) {
        draws.filter { it.round in rangeStart..rangeEnd }.sortedByDescending(DrawResult::round)
    }
    val stats = remember(filteredDraws, includeBonus) { numberStatisticsForDisplay(filteredDraws, includeBonus) }
    val selectedStat = stats.firstOrNull { it.number == selectedNumber }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("번호별 당첨횟수", "사진처럼 번호마다 몇 번 나왔는지 막대 그래프로 봅니다. 색상 히트맵과 오래 안 나온 기간은 별도 메뉴인 ‘출현 흐름 히트맵’에서 봅니다.")
        CardBlock {
            Text("조회 조건", color = FeatureInk, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = startRoundText,
                    onValueChange = { startRoundText = it.filter(Char::isDigit).take(4) },
                    label = { Text("시작 회차") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = endRoundText,
                    onValueChange = { endRoundText = it.filter(Char::isDigit).take(4) },
                    label = { Text("끝 회차") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(5, 10, 15).forEach { weeks ->
                    OutlinedButton(
                        onClick = {
                            endRoundText = maxRound.toString()
                            startRoundText = (maxRound - weeks + 1).coerceAtLeast(minRound).toString()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("${weeks}주간", color = FeatureGreen, fontWeight = FontWeight.Black)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("조회하기")
                }
                OutlinedButton(
                    onClick = { includeBonus = !includeBonus },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(if (includeBonus) "보너스 포함" else "보너스 미포함", color = FeatureInk, fontWeight = FontWeight.Bold)
                }
            }
            Text("${rangeStart}회차 ~ ${rangeEnd}회차 · ${filteredDraws.size}개 회차 조회", color = FeatureInk.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
        }
        CardBlock {
            Text("번호 · 그래프 · 당첨횟수", color = FeatureInk, fontWeight = FontWeight.Black)
            Text("막대가 길수록 선택한 회차 범위에서 더 자주 나온 번호입니다. 번호 줄을 누르면 상세가 열립니다.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("번호", color = FeatureInk.copy(alpha = 0.64f), modifier = Modifier.width(54.dp), fontWeight = FontWeight.Bold)
                Text("그래프", color = FeatureInk.copy(alpha = 0.64f), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Text("당첨횟수", color = FeatureInk.copy(alpha = 0.64f), modifier = Modifier.width(72.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            }
            val tableMax = stats.maxOfOrNull(NumberStatistic::totalCount)?.coerceAtLeast(1) ?: 1
            stats.sortedBy(NumberStatistic::number).forEach { stat ->
                NumberStatisticBarRow(
                    stat = stat,
                    maxCount = tableMax,
                    selected = selectedNumber == stat.number,
                    onClick = { selectedNumber = stat.number },
                )
            }
            if (selectedStat == null) {
                Text("알고 싶은 번호 줄을 한 번 눌러보세요.", color = FeatureInk.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
            } else {
                StatisticDetailCard(selectedStat, "전체")
            }
        }
    }
}

@Composable
internal fun NumberFlowHeatmapFeature(draws: List<DrawResult>) {
    var mode by remember { mutableStateOf("최근 100회") }
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    val stats = remember(draws) { numberStatisticsForDisplay(draws, includeBonus = false) }
    val hot = stats.sortedWith(compareByDescending<NumberStatistic> { it.recent100Count }.thenBy { it.number }).take(3)
    val absent = stats.sortedWith(compareByDescending<NumberStatistic> { it.absenceStreak }.thenBy { it.number }).take(3)
    val values = stats.map { statisticValue(it, mode) }
    val minValue = values.minOrNull() ?: 0
    val maxValue = values.maxOrNull() ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("출현 흐름 히트맵", "공 색이 진할수록 선택한 기준에서 값이 큽니다. 손가락으로 번호를 누르면 얼마나 자주 나왔고, 얼마나 안 나왔는지 바로 보여줍니다.")
        MetricGrid(
            metrics = listOf(
                "최근 100회 강세" to hot.joinToString { "${it.number}번" },
                "오래 미출현" to absent.joinToString { "${it.number}번" },
            ),
        )
        listOf("전체", "최근 100회", "최근 50회", "최근 10회", "안 나온 기간").chunked(3).forEach { rowModes ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowModes.forEach { label ->
                    OutlinedButton(
                        onClick = { mode = label },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            label,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (mode == label) FeatureOrange else FeatureInk,
                            fontWeight = if (mode == label) FontWeight.Black else FontWeight.Bold,
                        )
                    }
                }
                repeat(3 - rowModes.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
        InfoCard("선택 기준: $mode", statModeDescription(mode))
        CardBlock {
            Text("색상 히트맵", color = FeatureInk, fontWeight = FontWeight.Black)
            Text(
                if (mode == "안 나온 기간") "연한 파랑 = 짧게 미출현 · 진한 파랑 = 오래 미출현" else "연한 노랑 = 적게 출현 · 진한 빨강 = 많이 출현",
                color = FeatureInk.copy(alpha = 0.66f),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                items(stats, key = { it.number }) { stat: NumberStatistic ->
                    StatisticTile(
                        stat = stat,
                        mode = mode,
                        minValue = minValue,
                        maxValue = maxValue,
                        selected = selectedNumber == stat.number,
                        onClick = { selectedNumber = stat.number },
                    )
                }
            }
            val selectedStat = stats.firstOrNull { it.number == selectedNumber }
            if (selectedStat == null) {
                Text("알고 싶은 번호 공을 한 번 눌러보세요.", color = FeatureInk.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
            } else {
                StatisticDetailCard(selectedStat, mode)
            }
        }
        CardBlock {
            Text("상태 카드", color = FeatureInk, fontWeight = FontWeight.Black)
            hot.firstOrNull()?.let { stat ->
                StatisticBigCard(stat, "최근 100회 출현 ${stat.recent100Count}회")
            }
            absent.firstOrNull()?.let { stat ->
                StatisticBigCard(stat, "최근 ${stat.absenceStreak}회 미출현")
            }
        }
    }
}

@Composable
private fun NumberStatisticBarRow(
    stat: NumberStatistic,
    maxCount: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val fraction = (stat.totalCount.toFloat() / maxCount.toFloat()).coerceIn(0.03f, 1f)
    val color = featureBallColor(stat.number)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiniLottoBall(stat.number, size = if (selected) 42 else 36)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .height(16.dp)
                .background(Color(0xFFECEFF1), RoundedCornerShape(999.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(16.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.28f), color),
                        ),
                        RoundedCornerShape(999.dp),
                    ),
            )
        }
        Text(
            text = stat.totalCount.toString(),
            color = if (selected) FeaturePurple else FeatureInk,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun StatisticBigCard(stat: NumberStatistic, detail: String) {
    Surface(
        color = Color(0xFFF4F7F8),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniLottoBall(stat.number, size = 46)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("${stat.number}번", color = FeatureInk, fontWeight = FontWeight.Black)
                Text(detail, color = FeatureBlue, fontWeight = FontWeight.Bold)
                Text("전체 출현 ${stat.totalCount}회 · 평균 간격 ${formatStatisticAverage(stat.averageGap)}회 · ${stat.status}", color = FeatureInk.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatisticTile(
    stat: NumberStatistic,
    mode: String,
    minValue: Int,
    maxValue: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val value = statisticValue(stat, mode)
    val denominator = (maxValue - minValue).coerceAtLeast(1)
    val rawIntensity = ((value - minValue).toFloat() / denominator.toFloat()).coerceIn(0f, 1f)
    val intensity = if (value <= minValue) 0f else (0.18f + rawIntensity * 0.82f).coerceIn(0f, 1f)
    val color = if (mode == "안 나온 기간") {
        statisticBlueColor(intensity)
    } else {
        statisticWarmColor(intensity)
    }
    val tileColor = if (selected) FeaturePurple else color
    val textColor = if (selected || intensity >= 0.35f) Color.White else FeatureInk
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(tileColor, RoundedCornerShape(7.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(stat.number.toString(), color = textColor, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
    }
}

private fun statisticWarmColor(intensity: Float): Color = when {
    intensity >= 0.82f -> Color(0xFFB71C1C)
    intensity >= 0.62f -> Color(0xFFD84315)
    intensity >= 0.42f -> Color(0xFFF57C00)
    intensity >= 0.22f -> Color(0xFFFFB300)
    else -> Color(0xFFFFECB3)
}

private fun statisticBlueColor(intensity: Float): Color = when {
    intensity >= 0.82f -> Color(0xFF0D47A1)
    intensity >= 0.62f -> Color(0xFF1565C0)
    intensity >= 0.42f -> Color(0xFF1E88E5)
    intensity >= 0.22f -> Color(0xFF64B5F6)
    else -> Color(0xFFBBDEFB)
}

private fun statisticValue(stat: NumberStatistic, mode: String): Int = when (mode) {
    "전체" -> stat.totalCount
    "최근 50회" -> stat.recent50Count
    "최근 10회" -> stat.recent10Count
    "안 나온 기간" -> stat.absenceStreak
    else -> stat.recent100Count
}

private fun numberStatisticsForDisplay(draws: List<DrawResult>, includeBonus: Boolean): List<NumberStatistic> {
    val sortedDesc = draws.sortedByDescending(DrawResult::round)
    val sortedAsc = draws.sortedBy(DrawResult::round)
    fun DrawResult.statNumbers(): List<Int> =
        if (includeBonus) mainNumbers + bonusNumber else mainNumbers

    return (1..45).map { number ->
        val totalCount = sortedAsc.count { number in it.statNumbers() }
        val recent100Count = sortedDesc.take(100).count { number in it.statNumbers() }
        val recent50Count = sortedDesc.take(50).count { number in it.statNumbers() }
        val recent10Count = sortedDesc.take(10).count { number in it.statNumbers() }
        val absence = sortedDesc.indexOfFirst { number in it.statNumbers() }.let { index ->
            if (index < 0) sortedDesc.size else index
        }
        val hitRounds = sortedAsc.filter { number in it.statNumbers() }.map(DrawResult::round)
        val averageGap = if (hitRounds.size <= 1) {
            if (sortedAsc.isEmpty()) 0.0 else sortedAsc.size.toDouble()
        } else {
            hitRounds.zipWithNext { a, b -> (b - a).toDouble() }.average()
        }
        val status = when {
            absence >= 20 -> "장기 미출현"
            recent10Count >= 2 -> "최근 강세"
            absence >= 10 -> "미출현 관찰"
            else -> "보통"
        }
        NumberStatistic(
            number = number,
            totalCount = totalCount,
            recent100Count = recent100Count,
            recent50Count = recent50Count,
            recent10Count = recent10Count,
            absenceStreak = absence,
            averageGap = averageGap,
            status = status,
        )
    }
}

private fun statModeDescription(mode: String): String = when (mode) {
    "전체" -> "1회부터 현재 회차까지 몇 번 나왔는지 봅니다."
    "최근 50회" -> "최근 약 1년 흐름을 가볍게 봅니다."
    "최근 10회" -> "가장 최근 흐름만 빠르게 봅니다."
    "안 나온 기간" -> "최근 몇 회 동안 나오지 않았는지 봅니다. 파란색이 진할수록 오래 안 나온 번호입니다."
    else -> "최근 100회 기준으로 자주 나온 번호를 봅니다."
}

@Composable
private fun StatisticDetailCard(stat: NumberStatistic, mode: String) {
    val selectedValue = statisticValue(stat, mode)
    Surface(
        color = Color(0xFFFFF7E8),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MiniLottoBall(stat.number, size = 42)
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text("${stat.number}번 상세", color = FeatureInk, fontWeight = FontWeight.Black)
                    Text("현재 보기 기준($mode): ${selectedValue}회", color = FeatureOrange, fontWeight = FontWeight.Bold)
                }
            }
            Text("전체 회차에서는 ${stat.totalCount}회 나왔습니다.", color = FeatureInk)
            Text("최근 100회 ${stat.recent100Count}회 · 최근 50회 ${stat.recent50Count}회 · 최근 10회 ${stat.recent10Count}회", color = FeatureInk)
            Text("최근 ${stat.absenceStreak}회 동안 나오지 않았고, 평균 출현 간격은 ${formatStatisticAverage(stat.averageGap)}회입니다.", color = FeatureInk)
            Text("상태: ${stat.status}", color = FeatureBlue, fontWeight = FontWeight.Bold)
            Text("과거 기록을 쉽게 보기 위한 통계이며, 다음 회차 출현을 보장하지 않습니다.", color = FeatureInk.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
        }
    }
}


private fun formatStatisticAverage(value: Double): String = String.format(Locale.KOREA, "%.2f", value)

