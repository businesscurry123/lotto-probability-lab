package com.lottolab.probability.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.NumberStatistic
import java.util.Locale

@Composable
internal fun ProbabilityFeature(draws: List<DrawResult>) {
    val stats = remember(draws) { LottoAnalytics.numberStatistics(draws) }
    val defaultTargetNumber = stats.maxByOrNull(NumberStatistic::absenceStreak)?.number?.toString() ?: "7"
    var targetNumber by remember(defaultTargetNumber) { mutableStateOf(defaultTargetNumber) }
    var trials by remember { mutableStateOf("10") }
    var tickets by remember { mutableStateOf("5") }
    var selectedNumberFromGrid by remember { mutableStateOf<Int?>(null) }
    val trialCount = trials.toIntOrNull()?.coerceIn(1, 100_000) ?: 10
    val ticketCount = tickets.toIntOrNull()?.coerceIn(1, 10_000) ?: 5
    val number = (selectedNumberFromGrid ?: targetNumber.toIntOrNull()?.coerceIn(1, 45) ?: 7).coerceIn(1, 45)
    val numberProbability = LottoAnalytics.specificNumberProbability(trialCount)
    val sameCombo = LottoAnalytics.sameCombinationFirstPrizeProbability(trialCount)
    val fivePlus = LottoAnalytics.fiveOrMoreMainNumberProbability(trialCount)
    val multiTicket = LottoAnalytics.multipleTicketFirstPrizeProbability(ticketCount, trialCount)
    val gachaTrials = LottoAnalytics.trialsNeededForProbability(0.003, 1.0 / 8_145_060.0)
    val numberStat = stats.firstOrNull { it.number == number }
    val absentCumulative = LottoAnalytics.specificNumberProbability(numberStat?.absenceStreak ?: 0)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WarningCard(
            "이것은 누적확률이지 실제 확률이 아닙니다.",
            "다음 회차에 특정 번호가 나올 실제 확률은 모든 번호가 약 13.33%로 같습니다. 빨간색은 오래 안 나온 기간을 기준으로 한 누적 체감 표시입니다.",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = selectedNumberFromGrid?.toString() ?: targetNumber,
                onValueChange = {
                    targetNumber = it.filter(Char::isDigit).take(2)
                    selectedNumberFromGrid = null
                },
                label = { Text("번호") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            OutlinedTextField(
                value = trials,
                onValueChange = { trials = it.filter(Char::isDigit).take(6) },
                label = { Text("앞으로 회차") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }
        CardBlock {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ProbabilityBall(number, absentCumulative)
                Column {
                    Text("${number}번이 다음 회차에 나올 확률은 항상 약 13.33%입니다.", color = FeatureInk)
                    Text(
                        "앞으로 ${trialCount}회 안에 한 번 이상 나올 누적확률은 ${formatProbabilityPercent(numberProbability)}입니다.",
                        color = FeatureBlue,
                        fontWeight = FontWeight.Bold,
                    )
                    numberStat?.let { stat ->
                        Text(
                            text = "${stat.number}번은 최근 ${stat.absenceStreak}회 동안 나오지 않았습니다.",
                            color = FeatureInk,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "그 기간 기준 누적 출현확률은 ${formatProbabilityPercent(absentCumulative)}입니다.",
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = "과거 기록 상태: ${stat.status} · 최근 100회 ${stat.recent100Count}회 · ${stat.absenceStreak}회 미출현",
                            color = probabilityStatusColor(stat),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        CardBlock {
            Text("오래 안 나온 번호 누적표", color = FeatureInk, fontWeight = FontWeight.Black)
            Text(
                text = "공이 붉을수록 최근 안 나온 기간이 길고, 그 기간 동안 한 번 이상 나왔을 누적확률이 높다는 뜻입니다. 실제 다음 회차 확률 상승은 아닙니다.",
                color = FeatureInk.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodySmall,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                items(stats, key = { it.number }) { stat ->
                    AbsenceProbabilityTile(
                        stat = stat,
                        selected = stat.number == number,
                        onClick = {
                            selectedNumberFromGrid = stat.number
                            targetNumber = stat.number.toString()
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = tickets,
            onValueChange = { tickets = it.filter(Char::isDigit).take(5) },
            label = { Text("서로 다른 조합 구매 장수") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        MetricGrid(
            metrics = listOf(
                "같은 번호 1등" to formatProbabilityPercent(sameCombo),
                "5개 이상" to formatProbabilityPercent(fivePlus),
                "${ticketCount}장 1등" to formatProbabilityPercent(multiTicket),
                "가챠 0.3% 도달" to "${gachaTrials}회차",
            ),
        )
        InfoCard(
            title = "가챠 비교",
            body = "같은 번호로 1,000회차 도전해도 1등 누적확률은 약 ${formatProbabilityPercent(LottoAnalytics.sameCombinationFirstPrizeProbability(1000))}입니다.",
        )
    }
}

@Composable
private fun AbsenceProbabilityTile(
    stat: NumberStatistic,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val cumulative = LottoAnalytics.specificNumberProbability(stat.absenceStreak)
    val intensity = cumulative.toFloat().coerceIn(0f, 1f)
    val color = if (selected) FeaturePurple else absenceProbabilityColor(intensity)
    val textColor = if (selected || intensity >= 0.35f) Color.White else FeatureInk
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(color, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(stat.number.toString(), color = textColor, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ProbabilityBall(number: Int, absenceCumulative: Double) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(absenceProbabilityColor(absenceCumulative.toFloat()), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            number.toString(),
            color = if (absenceCumulative >= 0.35) Color.White else FeatureInk,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

private fun absenceProbabilityColor(intensity: Float): Color = when {
    intensity >= 0.96f -> Color(0xFFB71C1C)
    intensity >= 0.90f -> Color(0xFFD32F2F)
    intensity >= 0.76f -> Color(0xFFF4511E)
    intensity >= 0.52f -> Color(0xFFFF9800)
    intensity >= 0.25f -> Color(0xFFFFCC80)
    else -> Color(0xFFFFF3E0)
}

private fun probabilityStatusColor(stat: NumberStatistic?): Color = when (stat?.status) {
    "장기 미출현", "미출현 관찰" -> FeatureBlue
    "최근 강세" -> FeatureOrange
    else -> Color(0xFF65727E)
}

private fun formatProbabilityPercent(value: Double): String =
    String.format(Locale.KOREA, "%.4f%%", value * 100.0)
