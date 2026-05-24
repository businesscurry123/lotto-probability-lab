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
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lottolab.probability.ads.AdMobTopBanner
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.LeaderboardEntry
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.RoundReport
import com.lottolab.probability.domain.SavedNumberSet

@Composable
internal fun SavedNumberBand(
    numberSets: List<SavedNumberSet>,
    allSavedCount: Int,
    selectedDraw: DrawResult?,
    onAdd: () -> Unit,
    onOpenManage: () -> Unit,
    onOpenSet: (SavedNumberSet) -> Unit,
) {
    Surface(color = Color(0xFFF9FCFD), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            SectionTitle(
                title = "저장한 번호",
                subtitle = "내 번호가 이번 회차에 얼마나 가까웠을까?",
                action = {
                    Button(
                        onClick = onAdd,
                        contentPadding = ButtonDefaults.ContentPadding,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Spruce),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("번호 저장")
                    }
                },
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (numberSets.isEmpty()) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (allSavedCount == 0) {
                                "아직 저장한 번호가 없습니다."
                            } else {
                                "메인에 표시할 번호를 즐겨찾기로 고정해보세요."
                            },
                            color = Ink.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onOpenManage,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Icon(Icons.Outlined.StarBorder, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("번호 저장 / QR 설정")
                        }
                    }
                }
            } else {
                numberSets.forEachIndexed { index, numberSet ->
                    SavedNumberCard(
                        numberSet = numberSet,
                        draw = selectedDraw,
                        onClick = { onOpenSet(numberSet) },
                    )
                    if (index != numberSets.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
internal fun LeaderboardBand(entries: List<LeaderboardEntry>, round: Int) {
    Surface(color = Color(0xFFEFF5EE), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            SectionTitle(
                title = "내 번호 순위",
                subtitle = "이번 주 가장 선전한 내 번호는?",
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${round}회 기준",
                color = Ink.copy(alpha = 0.64f),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    LeaderboardHeader()
                    entries.forEach { entry ->
                        HorizontalDivider(color = Ink.copy(alpha = 0.08f))
                        LeaderboardRow(entry)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ReportBand(report: RoundReport, draw: DrawResult) {
    Surface(color = HeaderCream, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            SectionTitle(
                title = "이번 회차 분석 리포트",
                subtitle = "${draw.round}회 번호 특징",
            )
            Spacer(modifier = Modifier.height(12.dp))
            ReportMetric("홀짝 비율", "${report.oddCount}:${report.evenCount}")
            ReportMetric("연속 번호", if (report.hasConsecutiveNumbers) "있음" else "없음")
            ReportMetric("장기 미출현 번호 출현", "${report.longAbsentCount}개")
            ReportMetric("최근 강세 번호 출현", "${report.hotNumberCount}개")
            ReportMetric("같은 앞자리 번호수", "${report.sameFrontGroupCount}개")
        }
    }
}

@Composable
internal fun AdBand() {
    AdMobTopBanner()
}

@Composable
internal fun NumberStrip(numbers: List<Int>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        numbers.sorted().forEach { number -> SectionLottoBall(number = number) }
    }
}

@Composable
private fun SavedNumberCard(
    numberSet: SavedNumberSet,
    draw: DrawResult?,
    onClick: () -> Unit,
) {
    val match = draw?.let { LottoAnalytics.match(numberSet.numbers, it) }
    val nearMiss = draw?.let { LottoAnalytics.nearMiss(numberSet.numbers, it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (numberSet.favorite) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "즐겨찾기",
                        tint = Color(0xFFD59B00),
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp),
                    )
                }
                Text(
                    text = numberSet.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (match != null && nearMiss != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${match.matchedCount}개 적중",
                            color = River,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = "아깝다 ${nearMiss.score}점",
                            color = Citrus,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                } else if (match != null) {
                    Text(
                        text = "${match.matchedCount}개 적중",
                        color = River,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            if (numberSet.collectionName.isNotBlank()) {
                Text(
                    text = numberSet.collectionName,
                    color = Ink.copy(alpha = 0.54f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            NumberStrip(numberSet.numbers)
            if (match != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = match.rank.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.62f),
                )
                if (nearMiss != null) {
                    Text(
                        text = "당첨번호와 ±1 근접: ${nearMiss.nearCount}개",
                        style = MaterialTheme.typography.bodySmall,
                        color = Citrus.copy(alpha = 0.82f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        TableLabel(text = "순위", modifier = Modifier.width(50.dp))
        TableLabel(text = "번호 세트", modifier = Modifier.weight(1f))
        TableLabel(text = "적중", modifier = Modifier.width(62.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${entry.rank}위",
            modifier = Modifier.width(50.dp),
            fontWeight = FontWeight.Bold,
            color = Citrus,
        )
        Text(
            text = entry.numberSet.name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Ink,
        )
        Text(
            text = "${entry.match.matchedCount}개",
            modifier = Modifier.width(62.dp),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Bold,
            color = Ink,
        )
    }
}

@Composable
private fun TableLabel(text: String, modifier: Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = textAlign,
        style = MaterialTheme.typography.labelMedium,
        color = Ink.copy(alpha = 0.56f),
    )
}

@Composable
private fun ReportMetric(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = Ink.copy(alpha = 0.74f),
        )
        Text(text = value, color = Ink, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    action: @Composable (() -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink.copy(alpha = 0.66f),
            )
        }
        action?.invoke()
    }
}

@Composable
private fun SectionLottoBall(number: Int) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(ballColor(number), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
