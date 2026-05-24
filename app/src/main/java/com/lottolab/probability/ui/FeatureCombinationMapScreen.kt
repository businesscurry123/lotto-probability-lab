package com.lottolab.probability.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.SavedNumberSet
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun CombinationMapFeature(
    draws: List<DrawResult>,
    selectedDraw: DrawResult?,
    numberSets: List<SavedNumberSet>,
) {
    val points = remember(draws) { LottoAnalytics.combinationMapPoints(draws) }
    var inputNumbers by remember { mutableStateOf(emptySet<Int>()) }
    val savedPositions = remember(numberSets) {
        numberSets.map { numberSet ->
            numberSet to (LottoAnalytics.combinationIndex(numberSet.numbers).toFloat() / 8_145_060f)
        }
    }
    var highlightedSavedSetId by remember(numberSets) { mutableStateOf<Long?>(null) }
    val highlightedSavedSet = numberSets.firstOrNull { it.id == highlightedSavedSetId }
    val inputPosition = if (inputNumbers.size == 6) {
        LottoAnalytics.combinationIndex(inputNumbers.toList()).toFloat() / 8_145_060f
    } else {
        null
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("사전식 조합 위치", "첫 칸은 1,2,3,4,5,6이고 마지막 칸은 40,41,42,43,44,45입니다.")
        CombinationMapGuideCard()
        highlightedSavedSet?.let { numberSet ->
            InfoCard(
                "길게 누른 위치의 저장 번호",
                "${numberSet.name} · ${numberSet.numbers.sorted().joinToString(", ")} · 위치 ${String.format(Locale.KOREA, "%,d", LottoAnalytics.combinationIndex(numberSet.numbers))} / 8,145,060",
            )
        }
        CardBlock {
            Text("세로선 지도", color = FeatureInk, fontWeight = FontWeight.Black)
            Text("초록 선 근처를 길게 누르면 어떤 저장 번호인지 표시됩니다.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFF4F7F8), RoundedCornerShape(8.dp))
                    .pointerInput(savedPositions) {
                        detectTapGestures(
                            onLongPress = { offset ->
                                if (savedPositions.isNotEmpty() && size.width > 0) {
                                    val normalized = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                    highlightedSavedSetId = savedPositions.minByOrNull { (_, position) ->
                                        abs(position - normalized)
                                    }?.first?.id
                                }
                            },
                        )
                    },
            ) {
                points.forEachIndexed { index, point ->
                    val x = size.width * point.normalizedPosition.coerceIn(0f, 1f)
                    drawLine(
                        color = FeatureOrange.copy(alpha = 0.18f + (0.55f * (1f - index / points.size.coerceAtLeast(1).toFloat()))),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round,
                    )
                }
                savedPositions.forEach { (numberSet, position) ->
                    val x = size.width * position.coerceIn(0f, 1f)
                    val highlighted = numberSet.id == highlightedSavedSetId
                    drawLine(
                        color = if (highlighted) FeaturePurple else if (numberSet.favorite) FeatureGreen else FeatureGreen.copy(alpha = 0.62f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = if (highlighted) 11f else if (numberSet.favorite) 8f else 5f,
                        cap = StrokeCap.Round,
                    )
                }
                inputPosition?.let { position ->
                    val x = size.width * position.coerceIn(0f, 1f)
                    drawLine(
                        color = FeaturePurple,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 7f,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
        CardBlock {
            Text("격자 지도", color = FeatureInk, fontWeight = FontWeight.Black)
            Text("파란 점은 역대 당첨 조합, 초록 점은 내 저장 번호입니다.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color(0xFFF4F7F8), RoundedCornerShape(8.dp))
                    .pointerInput(savedPositions) {
                        detectTapGestures(
                            onLongPress = { offset ->
                                if (savedPositions.isNotEmpty() && size.width > 0 && size.height > 0) {
                                    val columns = 24
                                    val rows = 10
                                    val col = ((offset.x / size.width.toFloat()) * columns).toInt().coerceIn(0, columns - 1)
                                    val row = ((offset.y / size.height.toFloat()) * rows).toInt().coerceIn(0, rows - 1)
                                    val normalized = ((row * columns + col).toFloat() / (columns * rows - 1).toFloat()).coerceIn(0f, 1f)
                                    highlightedSavedSetId = savedPositions.minByOrNull { (_, position) ->
                                        abs(position - normalized)
                                    }?.first?.id
                                }
                            },
                        )
                    },
            ) {
                val columns = 24
                val rows = 10
                points.forEach { point ->
                    val cell = (point.normalizedPosition.coerceIn(0f, 1f) * (columns * rows - 1)).toInt()
                    val col = cell % columns
                    val row = cell / columns
                    val x = (col + 0.5f) * size.width / columns
                    val y = (row + 0.5f) * size.height / rows
                    drawCircle(FeatureBlue.copy(alpha = 0.52f), radius = 4.5f, center = Offset(x, y))
                }
                savedPositions.forEach { (numberSet, position) ->
                    val cell = (position.coerceIn(0f, 1f) * (columns * rows - 1)).toInt()
                    val col = cell % columns
                    val row = cell / columns
                    val x = (col + 0.5f) * size.width / columns
                    val y = (row + 0.5f) * size.height / rows
                    val highlighted = numberSet.id == highlightedSavedSetId
                    drawCircle(
                        color = if (highlighted) FeaturePurple else FeatureGreen,
                        radius = if (highlighted) 10f else if (numberSet.favorite) 8f else 6f,
                        center = Offset(x, y),
                    )
                }
                inputPosition?.let { position ->
                    val cell = (position.coerceIn(0f, 1f) * (columns * rows - 1)).toInt()
                    val col = cell % columns
                    val row = cell / columns
                    val x = (col + 0.5f) * size.width / columns
                    val y = (row + 0.5f) * size.height / rows
                    drawCircle(FeaturePurple, radius = 8f, center = Offset(x, y))
                }
            }
        }
        CardBlock {
            Text("직접 입력", color = FeatureInk, fontWeight = FontWeight.Black)
            Text("번호 6개를 누르면 보라색 위치가 바로 표시됩니다. 선택 ${inputNumbers.size}/6", color = FeatureInk.copy(alpha = 0.66f))
            LazyVerticalGrid(
                columns = GridCells.Fixed(9),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                items((1..45).toList()) { number ->
                    PickNumberBubble(
                        number = number,
                        selected = number in inputNumbers,
                        onClick = {
                            inputNumbers = when {
                                number in inputNumbers -> inputNumbers - number
                                inputNumbers.size < 6 -> inputNumbers + number
                                else -> inputNumbers
                            }
                        },
                    )
                }
            }
            if (inputNumbers.size == 6) {
                Text(
                    "직접 입력 위치: ${String.format(Locale.KOREA, "%,d", LottoAnalytics.combinationIndex(inputNumbers.toList()))} / 8,145,060",
                    color = FeaturePurple,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        selectedDraw?.let {
            val index = LottoAnalytics.combinationIndex(it.mainNumbers)
            InfoCard("${it.round}회 위치", "사전식 조합 인덱스: ${String.format(Locale.KOREA, "%,d", index)} / 8,145,060")
        }
    }
}

@Composable
private fun CombinationMapGuideCard() {
    CardBlock {
        Text("지도 읽는 법", color = FeatureInk, fontWeight = FontWeight.Black)
        Text("선과 점은 번호 조합이 전체 8,145,060개 조합 중 어디쯤 있는지를 표시합니다.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
        LegendLine("주황 선: 역대 당첨 조합", FeatureOrange)
        LegendLine("파란 점: 역대 당첨 조합", FeatureBlue)
        LegendLine("초록 선/점: 저장 번호", FeatureGreen.copy(alpha = 0.72f))
        LegendLine("굵은 초록: 즐겨찾기 번호", FeatureGreen)
        LegendLine("보라 선/점: 직접 입력하거나 길게 눌러 확인한 번호", FeaturePurple)
    }
}
