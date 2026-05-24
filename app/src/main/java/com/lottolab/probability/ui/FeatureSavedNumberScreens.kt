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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.RangeAnalysis
import com.lottolab.probability.domain.SavedNumberSet
import com.lottolab.probability.domain.SlipQrDisplayMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
internal fun SaveNumberDetail(
    numberSets: List<SavedNumberSet>,
    qrDisplayMode: SlipQrDisplayMode,
    onChangeQrDisplayMode: (SlipQrDisplayMode) -> Unit,
    onOpenQr: () -> Unit,
    onAddNumberSet: () -> Unit,
    onToggleFavorite: (SavedNumberSet) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard(
            title = "번호 저장 / QR 설정",
            body = "6개 번호 세트를 저장하고, 묶음 이름과 즐겨찾기, 모바일 슬립지 QR 보기 방식을 함께 관리합니다.",
        )
        CardBlock {
            Text("모바일 슬립지 QR 보기 방식", color = FeatureInk, fontWeight = FontWeight.Black)
            Text(qrDisplayMode.description, color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SlipQrDisplayMode.entries.forEach { mode ->
                    OutlinedButton(
                        onClick = { onChangeQrDisplayMode(mode) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = mode.label,
                            color = if (mode == qrDisplayMode) FeatureGreen else FeatureInk,
                            fontWeight = if (mode == qrDisplayMode) FontWeight.Black else FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Button(onClick = onOpenQr, shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Outlined.QrCode2, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("모바일 슬립지 QR 열기")
            }
        }
        Button(onClick = onAddNumberSet, shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("번호 세트 만들기")
        }
        numberSets.forEach { numberSet ->
            SavedSetRow(numberSet = numberSet, onToggleFavorite = { onToggleFavorite(numberSet) })
        }
    }
}

@Composable
internal fun MatchHistoryFeature(numberSets: List<SavedNumberSet>, draws: List<DrawResult>) {
    if (numberSets.isEmpty()) {
        InfoCard("저장 번호 필요", "먼저 번호 세트를 저장하면 적중 기록을 계산할 수 있습니다.")
        return
    }

    val selectableSets = remember(numberSets) {
        numberSets.sortedWith(compareByDescending<SavedNumberSet> { it.favorite }.thenBy { it.createdAt })
    }
    val defaultSetId = selectableSets.firstOrNull { it.favorite }?.id ?: selectableSets.first().id
    var selectedSetId by remember(selectableSets) { mutableStateOf(defaultSetId) }
    val numberSet = selectableSets.firstOrNull { it.id == selectedSetId } ?: selectableSets.first()
    val over3 = LottoAnalytics.matchHistoryDetails(numberSet.numbers, draws, 3)
    val over4 = LottoAnalytics.matchHistoryDetails(numberSet.numbers, draws, 4)
    val over5 = LottoAnalytics.matchHistoryDetails(numberSet.numbers, draws, 5)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("적중 기록", "저장한 번호를 바꿔가며 3개 이상, 4개 이상, 5개 이상 맞았던 회차를 확인합니다.")
        AnalysisNumberSetSelector(
            numberSets = selectableSets,
            selectedSetId = numberSet.id,
            onSelect = { selectedSetId = it },
            title = "적중 기록 볼 번호 선택",
            description = "번호를 누르면 아래 적중 횟수와 회차 목록이 그 번호 기준으로 바뀝니다.",
        )
        MetricGrid(
            metrics = listOf(
                "3개 이상" to "${over3.size}회",
                "4개 이상" to "${over4.size}회",
                "5개 이상" to "${over5.size}회",
            ),
        )
        CardBlock {
            Text("최근 3개 이상 적중 회차", color = FeatureInk, fontWeight = FontWeight.Black)
            if (over3.isEmpty()) {
                Text("아직 3개 이상 적중 기록이 없습니다.", color = FeatureInk.copy(alpha = 0.66f))
            } else {
                over3.take(12).forEach { detail ->
                    HorizontalDivider(color = FeatureInk.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                    Text("${detail.round}회 · ${detail.matchedNumbers.size}개 일치", color = FeatureBlue, fontWeight = FontWeight.Bold)
                    FeatureNumberStrip(detail.matchedNumbers)
                    Text("당첨번호: ${detail.drawNumbers.joinToString(", ")}", color = FeatureInk.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
internal fun MyNumberAnalysisFeature(
    numberSets: List<SavedNumberSet>,
    draws: List<DrawResult>,
    onSaveGeneratedSet: (String, List<Int>, String) -> Unit,
) {
    if (numberSets.isEmpty()) {
        InfoCard("저장 번호 필요", "내 번호 분석은 저장된 번호 세트가 있어야 계산할 수 있습니다.")
        return
    }
    val templates = listOf(
        "고르게 분산" to listOf(1, 1, 1, 2, 1),
        "중간 번호대 중심" to listOf(0, 2, 2, 1, 1),
        "한 자리 포함형" to listOf(2, 1, 1, 1, 1),
        "20~30번대 중심" to listOf(0, 1, 2, 2, 1),
        "10번대 제외형" to listOf(1, 0, 2, 1, 2),
    )
    val selectableSets = remember(numberSets) {
        numberSets.sortedWith(compareByDescending<SavedNumberSet> { it.favorite }.thenBy { it.createdAt })
    }
    val defaultSetId = selectableSets.firstOrNull { it.favorite }?.id ?: selectableSets.first().id
    var selectedSetId by remember(selectableSets) { mutableStateOf(defaultSetId) }
    val numberSet = selectableSets.firstOrNull { it.id == selectedSetId } ?: selectableSets.first()
    val history = LottoAnalytics.matchHistoryDetails(numberSet.numbers, draws, 1)
        .sortedWith(compareByDescending<com.lottolab.probability.domain.MatchHistoryDetail> { it.matchedNumbers.size }.thenByDescending { it.round })
        .take(5)
    val rangeAnalysis = LottoAnalytics.rangeAnalysis(numberSet.numbers)
    val currentAnalysis = draws.firstOrNull()?.mainNumbers?.let { LottoAnalytics.rangeAnalysis(it) }
    val deltas = LottoAnalytics.rangeExpectationDelta(draws)
    val weekKey = remember {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val weekFields = WeekFields.ISO
        "${today.get(weekFields.weekBasedYear())}-${today.get(weekFields.weekOfWeekBasedYear())}"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("내 번호 분석", "역대 근접 회차와 번호대 분석을 이 화면 하나로 합쳤습니다. 번호대 분석은 따로 들어갈 필요 없이 여기서 보면 됩니다.")
        AnalysisNumberSetSelector(
            numberSets = selectableSets,
            selectedSetId = numberSet.id,
            onSelect = { selectedSetId = it },
        )
        MyNumberAnalysisHero(numberSet, rangeAnalysis, history.firstOrNull(), draws)
        RangeAnalysisCard(rangeAnalysis)
        RangeSetComparison(
            numberSets = selectableSets,
            selectedSetId = numberSet.id,
            onSelect = { selectedSetId = it },
        )
        currentAnalysis?.let { analysis ->
            InfoCard(
                "이번 회차 번호대",
                easyRangeSummary(analysis) + " 최근 100회 중 비슷한 모양은 ${LottoAnalytics.rangePatternAppearances(analysis.pattern, draws, 100)}회 있었습니다.",
            )
        }
        CardBlock {
            Text("역대 최고 근접 회차", color = FeatureInk, fontWeight = FontWeight.Black)
            if (history.isEmpty()) {
                Text("아직 비교 가능한 기록이 없습니다.", color = FeatureInk.copy(alpha = 0.66f))
            } else {
                history.forEach { detail ->
                    HorizontalDivider(color = FeatureInk.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                    Text("${detail.round}회 · ${detail.matchedNumbers.size}개 일치", color = FeatureBlue, fontWeight = FontWeight.Bold)
                    FeatureNumberStrip(detail.matchedNumbers)
                }
            }
        }
        CardBlock {
            Text("최근 100회 번호대 흐름", color = FeatureInk, fontWeight = FontWeight.Black)
            deltas.forEach { (bucket, delta) ->
                RangeBar(
                    label = bucket.label,
                    count = bucket.count,
                    maxCount = deltas.maxOf { it.first.count }.coerceAtLeast(1),
                    detail = "${if (delta >= 0) "+" else ""}${String.format(Locale.KOREA, "%.1f", delta)}%",
                )
            }
            Text(
                "40번대는 번호 개수가 적기 때문에 출현 횟수가 낮게 보일 수 있습니다.",
                color = FeatureInk.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        CardBlock {
            Text("번호대 템플릿 생성", color = FeatureInk, fontWeight = FontWeight.Black)
            Text("매주 한 번씩 바뀌는 번호대 모양 조합입니다. 번호 예측이 아니라 번호대 구성을 골라 랜덤 조합을 만드는 기능입니다.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
            Text("이번 주 키: $weekKey", color = FeaturePurple, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            templates.forEach { (name, template) ->
                val numbers = LottoAnalytics.rangeTemplateNumbers(template, "$weekKey-$name".hashCode())
                HorizontalDivider(color = FeatureInk.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                Text("$name · ${template.joinToString("-")}", color = FeatureInk, fontWeight = FontWeight.Bold)
                FeatureNumberStrip(numbers)
                TextButton(onClick = { onSaveGeneratedSet(name, numbers, "번호대 템플릿") }) {
                    Text("저장")
                }
            }
        }
    }
}

@Composable
private fun MyNumberAnalysisHero(
    numberSet: SavedNumberSet,
    rangeAnalysis: RangeAnalysis,
    bestHistory: com.lottolab.probability.domain.MatchHistoryDetail?,
    draws: List<DrawResult>,
) {
    val latestRound = draws.maxByOrNull(DrawResult::round)?.round ?: 0
    val stats = if (latestRound == 0) null else LottoAnalytics.growthStats(numberSet.numbers, draws, latestRound)

    Surface(
        color = Color(0xFFEAF4FF),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(numberSet.name, color = FeatureInk, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("이 번호를 기준으로 아래 분석이 모두 바뀝니다.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
                }
                Surface(color = FeaturePurple, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        "밸런스 ${rangeAnalysis.diversityScore}점",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            FeatureNumberStrip(numberSet.numbers)
            MetricGrid(
                metrics = listOf(
                    "이번 회차 적중" to "${stats?.currentMatchCount ?: 0}개",
                    "역대 최고" to "${stats?.allTimeBestMatchCount ?: 0}개",
                    "최고 근접 회차" to (bestHistory?.let { "${it.round}회 ${it.matchedNumbers.size}개" } ?: "기록 없음"),
                    "번호대 요약" to easyRangeSummary(rangeAnalysis),
                ),
            )
        }
    }
}

@Composable
private fun AnalysisNumberSetSelector(
    numberSets: List<SavedNumberSet>,
    selectedSetId: Long,
    onSelect: (Long) -> Unit,
    title: String = "분석할 번호 선택",
    description: String = "저장한 번호를 누르면 아래 분석이 그 번호 기준으로 바뀝니다.",
) {
    CardBlock {
        Text(title, color = FeatureInk, fontWeight = FontWeight.Black)
        Text(description, color = FeatureInk.copy(alpha = 0.64f), style = MaterialTheme.typography.bodySmall)
        numberSets.forEach { numberSet ->
            val selected = numberSet.id == selectedSetId
            Surface(
                color = if (selected) Color(0xFFE3F5EF) else Color(0xFFF4F7F8),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(numberSet.id) },
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (numberSet.favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (numberSet.favorite) Color(0xFFD59B00) else FeatureInk.copy(alpha = 0.34f),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = numberSet.name,
                            color = if (selected) FeatureGreen else FeatureInk,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (selected) {
                            Text("선택됨", color = FeatureGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "묶음 이름: ${numberSet.collectionName.ifBlank { "기본" }}",
                        color = FeatureInk.copy(alpha = 0.58f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    FeatureNumberStrip(numberSet.numbers)
                }
            }
        }
    }
}

@Composable
private fun RangeSetComparison(
    numberSets: List<SavedNumberSet>,
    selectedSetId: Long?,
    onSelect: (Long) -> Unit,
) {
    CardBlock {
        Text("내 저장 번호 번호대 비교", color = FeatureInk, fontWeight = FontWeight.Black)
        Text("각 줄을 누르면 아래 상세 분석이 그 번호 기준으로 바뀝니다.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
        numberSets.forEach { numberSet ->
            val analysis = LottoAnalytics.rangeAnalysis(numberSet.numbers)
            val selected = numberSet.id == selectedSetId
            Surface(
                color = if (selected) Color(0xFFE3F5EF) else Color(0xFFF4F7F8),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(numberSet.id) },
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(numberSet.name, color = if (selected) FeatureGreen else FeatureInk, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("밸런스 ${analysis.diversityScore}점", color = FeaturePurple, fontWeight = FontWeight.Bold)
                    }
                    Text(easyRangeSummary(analysis), color = FeatureInk.copy(alpha = 0.68f), style = MaterialTheme.typography.bodySmall)
                    Text("패턴 ${analysis.pattern}", color = FeatureInk.copy(alpha = 0.46f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun RangeAnalysisCard(analysis: RangeAnalysis) {
    var helpOpen by remember { mutableStateOf(false) }
    val breakdown = LottoAnalytics.rangeScoreBreakdown(analysis.buckets.flatMap { bucket ->
        List(bucket.count) { bucket.numbers.first }
    })

    CardBlock {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("번호대 밸런스 ${analysis.diversityScore}점", color = FeaturePurple, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(FeaturePurple.copy(alpha = 0.16f), CircleShape)
                    .clickable { helpOpen = true },
                contentAlignment = Alignment.Center,
            ) {
                Text("!", color = FeaturePurple, fontWeight = FontWeight.Black)
            }
        }
        Text(easyRangeSummary(analysis), color = FeatureInk, fontWeight = FontWeight.Bold)
        analysis.buckets.forEach { bucket ->
            RangeBar(
                label = bucket.label,
                count = bucket.count,
                maxCount = 6,
                detail = "${bucket.count}개",
            )
        }
        Text("세부 패턴: ${analysis.pattern}", color = FeatureInk.copy(alpha = 0.46f), style = MaterialTheme.typography.labelSmall)
        analysis.notes.forEach { note ->
            Text(note, color = FeatureInk.copy(alpha = 0.64f), style = MaterialTheme.typography.bodySmall)
        }
    }

    if (helpOpen) {
        AlertDialog(
            onDismissRequest = { helpOpen = false },
            title = { Text("밸런스 점수 산정 방식") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("사용한 번호대가 많을수록 점수가 올라갑니다.")
                    Text("한 번호대에 3개 이상 몰리면 점수가 덜 올라갑니다.")
                    Text("점수 = 번호대 다양성 점수 + 몰림 완화 점수")
                    Text("현재 점수 = ${breakdown.diversityPoints}점 + ${breakdown.concentrationPoints}점 = ${breakdown.totalScore}점")
                    Text("이 점수는 번호 구성을 보기 쉽게 만든 참고 지표이며 당첨 확률을 높이지 않습니다.")
                }
            },
            confirmButton = {
                TextButton(onClick = { helpOpen = false }) { Text("확인") }
            },
        )
    }
}

@Composable
internal fun SavedSetRow(numberSet: SavedNumberSet, onToggleFavorite: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (numberSet.favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                contentDescription = "즐겨찾기",
                tint = if (numberSet.favorite) Color(0xFFD59B00) else FeatureInk.copy(alpha = 0.42f),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(numberSet.name, color = FeatureInk, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(numberSet.collectionName, color = FeatureInk.copy(alpha = 0.56f), style = MaterialTheme.typography.bodySmall)
            FeatureNumberStrip(numberSet.numbers)
        }
    }
}

@Composable
private fun RangeBar(label: String, count: Int, maxCount: Int, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.width(70.dp), color = FeatureInk, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .background(Color(0xFFE8ECEE), RoundedCornerShape(999.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((count.toFloat() / maxCount.toFloat()).coerceIn(0.02f, 1f))
                    .height(12.dp)
                    .background(FeatureGreen, RoundedCornerShape(999.dp)),
            )
        }
        Text(
            text = detail,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End,
            color = FeatureInk,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun easyRangeSummary(analysis: RangeAnalysis): String {
    val maxBucket = analysis.buckets.maxByOrNull { it.count }
    val emptyBuckets = analysis.buckets.filter { it.count == 0 }.map { it.label }
    val mainText = if (maxBucket != null && maxBucket.count > 0) {
        "${maxBucket.label} 번호대에 ${maxBucket.count}개가 있어요."
    } else {
        "아직 번호가 없습니다."
    }
    val emptyText = if (emptyBuckets.isNotEmpty()) {
        " ${emptyBuckets.joinToString(", ")} 번호대는 비어 있어요."
    } else {
        " 모든 번호대가 골고루 들어 있어요."
    }
    return mainText + emptyText
}
