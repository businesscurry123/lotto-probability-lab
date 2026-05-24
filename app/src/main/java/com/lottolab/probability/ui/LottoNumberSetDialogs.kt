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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.GrowthStats
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.MatchSummary
import com.lottolab.probability.domain.NearMissSummary
import com.lottolab.probability.domain.SavedNumberSet
import java.util.Locale

@Composable
internal fun NumberSetDetailDialog(
    numberSet: SavedNumberSet,
    draw: DrawResult?,
    draws: List<DrawResult>,
    historyReady: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val match = draw?.let { LottoAnalytics.match(numberSet.numbers, it) }
    val nearMiss = draw?.let { LottoAnalytics.nearMiss(numberSet.numbers, it) }
    val growthStats = draw?.let { LottoAnalytics.growthStats(numberSet.numbers, draws, it.round) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(numberSet.name, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                NumberStrip(numberSet.numbers)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (draw == null || match == null || nearMiss == null) {
                    Text("회차 결과를 불러오면 당첨 확인이 표시됩니다.")
                } else {
                    MatchBlock(draw.round, match)
                    NearMissBlock(nearMiss)
                    if (historyReady && growthStats != null) {
                        GrowthBlock(growthStats)
                    } else {
                        Text(
                            text = "역대 기록을 캐시하는 중입니다.",
                            color = Ink.copy(alpha = 0.68f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("수정")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("삭제")
                }
                TextButton(onClick = onDismiss) { Text("닫기") }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NumberSetEditorDialog(
    initial: SavedNumberSet?,
    onDismiss: () -> Unit,
    onSave: (String, Set<Int>, Boolean, String) -> Unit,
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var collectionName by remember(initial?.id) { mutableStateOf(initial?.collectionName ?: "기본") }
    var favorite by remember(initial?.id) { mutableStateOf(initial?.favorite ?: false) }
    var selectedNumbers by remember(initial?.id) {
        mutableStateOf(initial?.numbers?.toSet() ?: emptySet())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "번호 세트 저장" else "번호 세트 수정") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("번호 세트 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("묶음 이름") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { favorite = !favorite },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = if (favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (favorite) Color(0xFFD59B00) else Ink,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (favorite) "즐겨찾기 고정됨" else "즐겨찾기에 고정")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "선택 ${selectedNumbers.size}/6",
                    color = if (selectedNumbers.size == 6) Spruce else Ink.copy(alpha = 0.68f),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    items((1..45).toList()) { number ->
                        NumberPickButton(
                            number = number,
                            selected = number in selectedNumbers,
                            onClick = {
                                selectedNumbers = when {
                                    number in selectedNumbers -> selectedNumbers - number
                                    selectedNumbers.size < 6 -> selectedNumbers + number
                                    else -> selectedNumbers
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedNumbers, favorite, collectionName) },
                enabled = name.isNotBlank() && selectedNumbers.size == 6,
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

@Composable
private fun MatchBlock(round: Int, match: MatchSummary) {
    Surface(
        color = Color(0xFFE7F2F8),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${round}회 당첨 확인",
                fontWeight = FontWeight.Bold,
                color = River,
            )
            Text(
                text = "이번 회차 적중: ${match.matchedCount}개",
                color = Ink,
            )
            Text(
                text = "결과: ${match.rank.label}",
                color = Ink,
            )
        }
    }
}

@Composable
private fun NearMissBlock(nearMiss: NearMissSummary) {
    Surface(
        color = Color(0xFFFFEFE4),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "아깝다 지수: ${nearMiss.score}점",
                color = Citrus,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "이번 회차는 번호 6개 중 ${nearMiss.nearCount}개가 당첨번호와 ±1 차이였습니다.",
                color = Ink,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "아깝다 지수는 재미용 지표이며, 다음 회차 확률을 높이지 않습니다.",
                color = Ink.copy(alpha = 0.68f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun GrowthBlock(stats: GrowthStats) {
    Surface(
        color = Color(0xFFF2F0FF),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("내 번호 성장 기록", color = Violet, fontWeight = FontWeight.Black)
            StatLine("최근 최고 기록", "${stats.recentBestMatchCount}개")
            StatLine("역대 최고 기록", "${stats.allTimeBestMatchCount}개")
            StatLine("최근 10회 평균 적중", "${formatDialogAverage(stats.recentTenAverage)}개")
            StatLine("최근 100회 평균 적중", "${formatDialogAverage(stats.recentHundredAverage)}개")
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = Ink.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = value,
            color = Ink,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun NumberPickButton(
    number: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                color = if (selected) ballColor(number) else Color(0xFFE7E0D7),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = if (selected) Color.White else Ink,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun formatDialogAverage(value: Double): String = String.format(Locale.KOREA, "%.2f", value)
