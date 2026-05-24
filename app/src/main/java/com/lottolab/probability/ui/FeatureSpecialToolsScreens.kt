package com.lottolab.probability.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.SavedNumberSet
import com.lottolab.probability.domain.SlipQrDisplayMode
import com.lottolab.probability.settings.AdFeatureUsageStore
import kotlin.math.ceil

@Composable
internal fun ScatteredNumbersFeature(
    draws: List<DrawResult>,
    qrDisplayMode: SlipQrDisplayMode,
    onChangeQrDisplayMode: (SlipQrDisplayMode) -> Unit,
    onSaveGeneratedSet: (String, List<Int>, String) -> Unit,
) {
    var unlocked by remember { mutableStateOf(false) }
    var selectedNumbers by remember { mutableStateOf(emptySet<Int>()) }
    val today = remember { AdFeatureUsageStore.todayKey() }
    val generatedSets = remember(draws, selectedNumbers, today) {
        LottoAnalytics.scatteredNumberCombinations(
            seedKey = "scattered-ui:$today",
            draws = draws,
            fixedNumbers = selectedNumbers,
            count = 5,
        )
    }
    val temporarySets = remember(generatedSets) {
        generatedSets.mapIndexed { index, numbers ->
            temporaryNumberSet(
                id = -(10_000L + index),
                name = if (generatedSets.size == 1) "흩어진 번호 모음" else "흩어진 번호 ${index + 1}",
                numbers = numbers,
                collectionName = "흩어진 번호",
            )
        }
    }
    val paperCount = ceil(generatedSets.size.coerceAtLeast(1) / 5.0).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WarningCard(
            "이 기능은 안타까운 상황을 정리하는 도구입니다.",
            "구매나 당첨을 보장하지 않습니다. 흩어진 번호를 한 줄로 모아 보는 참고 기능이며, 사용하려면 15초 광고를 먼저 봅니다.",
        )
        InfoCard(
            "흩어진 번호",
            "로또 용지 안에 번호 6개가 흩어져 있는 안타까운 상황이 어쩌다 가끔 발생할 때 있죠? 그 안타까운 기회를 놓치지 않게 만든 기능입니다.",
        )

        if (!unlocked) {
            AdCountdownCard(
                title = "흩어진 번호 광고",
                body = "15초 광고가 끝나면 번호 선택, QR 보기, 저장 기능이 열립니다.",
                onFinished = { unlocked = true },
            )
        } else {
            CardBlock {
                Text("흩어진 번호 6개 선택", color = FeatureInk, fontWeight = FontWeight.Black)
                Text("이미 마음속에 있는 번호가 있으면 6개를 누르세요. 비워두면 누적확률 체감 기준으로 5개 조합을 만듭니다. 선택 ${selectedNumbers.size}/6", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { selectedNumbers = emptySet() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("비우기")
                    }
                    Button(
                        onClick = {
                            selectedNumbers = LottoAnalytics
                                .cumulativeRecommendationCombinations("scattered-pick:$today", draws, count = 1)
                                .firstOrNull()
                                .orEmpty()
                                .toSet()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("6개 뽑기")
                    }
                }
            }

            MetricGrid(
                metrics = listOf(
                    "만든 조합" to "${generatedSets.size}줄",
                    "5줄 용지 기준" to "${paperCount}장",
                ),
            )
            GeneratedCombinationList(
                sets = temporarySets,
                onSaveGeneratedSet = onSaveGeneratedSet,
            )
            GeneratedQrPreview(
                title = "흩어진 번호 QR",
                sets = temporarySets,
                qrDisplayMode = qrDisplayMode,
                onChangeQrDisplayMode = onChangeQrDisplayMode,
            )
        }
    }
}

@Composable
internal fun DigitMatcherFeature(
    draws: List<DrawResult>,
    qrDisplayMode: SlipQrDisplayMode,
    onChangeQrDisplayMode: (SlipQrDisplayMode) -> Unit,
    onSaveGeneratedSet: (String, List<Int>, String) -> Unit,
) {
    var unlocked by remember { mutableStateOf(false) }
    var frontDigitMode by remember { mutableStateOf(true) }
    val today = remember { AdFeatureUsageStore.todayKey() }
    val plan = remember(draws, frontDigitMode, today) {
        LottoAnalytics.digitMatchPlan(
            seedKey = "digit-matcher:$today:$frontDigitMode",
            draws = draws,
            frontDigitMode = frontDigitMode,
            count = 5,
        )
    }
    val temporarySets = remember(plan) {
        plan.combinations.mapIndexed { index, numbers ->
            temporaryNumberSet(
                id = -(20_000L + index),
                name = "${plan.modeLabel} 조합 ${index + 1}",
                numbers = numbers,
                collectionName = "앞뒤자리 추첨기",
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WarningCard(
            "이것은 누적확률 체감 추첨기입니다.",
            "앞자리나 뒷자리 선택은 실제 당첨 확률을 높이지 않습니다. 오래 안 나온 번호의 누적 체감값을 자리 단위로 묶어 보여주는 참고 기능입니다.",
        )
        InfoCard(
            "앞뒤자리 추첨기",
            "내가 앞자리 혹은 뒷자리만 맞추는데 재능이 있다! 그런 사람들을 위한 누적확률 추첨기 입니다.",
        )

        if (!unlocked) {
            AdCountdownCard(
                title = "앞뒤자리 추첨기 광고",
                body = "15초 광고가 끝나면 앞자리/뒷자리 기준 조합을 볼 수 있습니다.",
                onFinished = { unlocked = true },
            )
        } else {
            CardBlock {
                Text("기준 선택", color = FeatureInk, fontWeight = FontWeight.Black)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { frontDigitMode = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("앞자리", color = if (frontDigitMode) FeatureOrange else FeatureInk, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { frontDigitMode = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("뒷자리", color = if (!frontDigitMode) FeatureOrange else FeatureInk, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "오늘의 체감 기준: ${plan.selectedLabel}",
                    color = FeaturePurple,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "기준 번호: ${plan.basisNumbers.joinToString(", ")}",
                    color = FeatureInk.copy(alpha = 0.66f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            GeneratedCombinationList(
                sets = temporarySets,
                onSaveGeneratedSet = onSaveGeneratedSet,
            )
            GeneratedQrPreview(
                title = "앞뒤자리 조합 QR",
                sets = temporarySets,
                qrDisplayMode = qrDisplayMode,
                onChangeQrDisplayMode = onChangeQrDisplayMode,
            )
        }
    }
}

private fun temporaryNumberSet(
    id: Long,
    name: String,
    numbers: List<Int>,
    collectionName: String,
): SavedNumberSet = SavedNumberSet(
    id = id,
    name = name,
    numbers = numbers.sorted(),
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis(),
    favorite = false,
    collectionName = collectionName,
)
