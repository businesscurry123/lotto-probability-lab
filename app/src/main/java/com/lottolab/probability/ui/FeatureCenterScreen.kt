package com.lottolab.probability.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lottolab.probability.domain.DailyCombination
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.FeatureCatalog
import com.lottolab.probability.domain.FeatureCategory
import com.lottolab.probability.domain.FeatureDefinition
import com.lottolab.probability.domain.FeatureStatus
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.NumberStatistic
import com.lottolab.probability.domain.RangeAnalysis
import com.lottolab.probability.domain.SavedNumberSet
import com.lottolab.probability.domain.SlipQrDisplayMode
import com.lottolab.probability.domain.SlipQrPage
import com.lottolab.probability.domain.SlipQrPlanner
import com.lottolab.probability.notifications.LottoNotificationScheduler
import com.lottolab.probability.notifications.ReminderType
import com.lottolab.probability.qr.QrCodeGenerator
import com.lottolab.probability.settings.AdFeatureUsageStore
import com.lottolab.probability.share.LottoShareCard
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import kotlin.math.abs
import kotlin.math.ceil
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun FeatureCenterScreen(
    state: LottoUiState,
    initialFeatureId: String? = null,
    showFavoritesOnly: Boolean = false,
    favoriteFeatureIds: Set<String>,
    onToggleFeatureFavorite: (String) -> Unit,
    onClose: () -> Unit,
    onAddNumberSet: () -> Unit,
    onOpenQr: () -> Unit,
    onSaveGeneratedSet: (String, List<Int>, String) -> Unit,
    onToggleFavorite: (SavedNumberSet) -> Unit,
    onClearActionMessage: () -> Unit,
    qrDisplayMode: SlipQrDisplayMode,
    onChangeQrDisplayMode: (SlipQrDisplayMode) -> Unit,
) {
    var selectedFeatureId by remember(initialFeatureId) { mutableStateOf(initialFeatureId) }
    var favoritesOnly by remember(showFavoritesOnly) { mutableStateOf(showFavoritesOnly) }
    val selectedFeature = FeatureCatalog.all.firstOrNull { it.id == selectedFeatureId }

    LaunchedEffect(state.actionMessage) {
        if (state.actionMessage != null) {
            delay(2_000)
            onClearActionMessage()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FeatureBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            FeatureTopBar(
                title = selectedFeature?.name ?: "부가기능 센터",
                subtitle = selectedFeature?.description
                    ?: if (favoritesOnly) "즐겨찾기한 부가기능만 모아 봅니다." else "기록, 통계, 누적확률, 공유 기능을 한눈에 봅니다.",
                showBack = selectedFeature != null,
                onBack = { selectedFeatureId = null },
                onClose = onClose,
            )
            state.actionMessage?.let { message ->
                Surface(color = Color(0xFFE7F2F8), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.weight(1f),
                            color = FeatureBlue,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(onClick = onClearActionMessage) {
                            Icon(Icons.Outlined.Close, contentDescription = "메시지 닫기", tint = FeatureBlue)
                        }
                    }
                }
            }
            if (selectedFeature == null) {
                FeatureCatalogList(
                    features = FeatureCatalog.all,
                    favoritesOnly = favoritesOnly,
                    favoriteFeatureIds = favoriteFeatureIds,
                    onToggleFavoritesOnly = { favoritesOnly = !favoritesOnly },
                    onToggleFeatureFavorite = onToggleFeatureFavorite,
                    onSelect = { selectedFeatureId = it.id },
                )
            } else {
                FeatureDetail(
                    feature = selectedFeature,
                    state = state,
                    onAddNumberSet = onAddNumberSet,
                    onOpenQr = onOpenQr,
                    onSaveGeneratedSet = onSaveGeneratedSet,
                    onToggleFavorite = onToggleFavorite,
                    qrDisplayMode = qrDisplayMode,
                    onChangeQrDisplayMode = onChangeQrDisplayMode,
                )
            }
        }
    }
}

@Composable
private fun FeatureTopBar(
    title: String,
    subtitle: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(color = FeatureSurface, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = if (showBack) onBack else onClose) {
                Icon(
                    imageVector = if (showBack) Icons.AutoMirrored.Outlined.ArrowBack else Icons.Outlined.Close,
                    contentDescription = if (showBack) "뒤로" else "닫기",
                    tint = FeatureInk,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = FeatureInk,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = FeatureInk.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "닫기", tint = FeatureInk)
            }
        }
    }
}

@Composable
private fun FeatureCatalogList(
    features: List<FeatureDefinition>,
    favoritesOnly: Boolean,
    favoriteFeatureIds: Set<String>,
    onToggleFavoritesOnly: () -> Unit,
    onToggleFeatureFavorite: (String) -> Unit,
    onSelect: (FeatureDefinition) -> Unit,
) {
    val visibleFeatures = if (favoritesOnly) {
        features.filter { it.id in favoriteFeatureIds }
    } else {
        features
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FavoriteFeatureSummary(
                favoritesOnly = favoritesOnly,
                favoriteCount = favoriteFeatureIds.size,
                onToggleFavoritesOnly = onToggleFavoritesOnly,
            )
        }
        if (favoritesOnly && visibleFeatures.isEmpty()) {
            item {
                EmptyFavoriteFeaturesCard()
            }
        }
        FeatureCategory.entries.forEach { category ->
            val categoryFeatures = visibleFeatures.filter { it.category == category }
            if (categoryFeatures.isNotEmpty()) {
                item(key = category.name) {
                    Text(
                        text = category.label,
                        color = FeatureInk,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                }
                items(categoryFeatures, key = FeatureDefinition::id) { feature ->
                    FeatureCard(
                        feature = feature,
                        isFavorite = feature.id in favoriteFeatureIds,
                        onToggleFavorite = { onToggleFeatureFavorite(feature.id) },
                        onClick = { onSelect(feature) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteFeatureSummary(
    favoritesOnly: Boolean,
    favoriteCount: Int,
    onToggleFavoritesOnly: () -> Unit,
) {
    Surface(color = Color.White, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFFFFF4CC), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Star, contentDescription = null, tint = Color(0xFFD59B00))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("부가기능 즐겨찾기", color = FeatureInk, fontWeight = FontWeight.Black)
                Text(
                    if (favoriteCount == 0) "자주 쓰는 기능 카드의 별을 눌러 오른쪽 상단에서 한눈에 볼 수 있습니다."
                    else "현재 ${favoriteCount}개 기능을 즐겨찾기에 넣었습니다.",
                    color = FeatureInk.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(onClick = onToggleFavoritesOnly, shape = RoundedCornerShape(8.dp)) {
                Text(if (favoritesOnly) "전체 보기" else "즐겨찾기만")
            }
        }
    }
}

@Composable
private fun EmptyFavoriteFeaturesCard() {
    InfoCard(
        title = "즐겨찾기한 부가기능이 없습니다.",
        body = "전체 보기로 돌아가서 자주 쓰는 부가기능 오른쪽 별을 눌러보세요. 그러면 메인 오른쪽 상단 즐겨찾기에서 바로 볼 수 있습니다.",
    )
}

@Composable
private fun FeatureCard(
    feature: FeatureDefinition,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(iconTint(feature.id).copy(alpha = 0.14f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = featureIcon(feature.id),
                    contentDescription = null,
                    tint = iconTint(feature.id),
                    modifier = Modifier.size(30.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = feature.name,
                        color = FeatureInk,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FeatureBadge(feature.status)
                }
                Text(
                    text = feature.description,
                    color = FeatureInk.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "부가기능 즐겨찾기 해제" else "부가기능 즐겨찾기 등록",
                    tint = if (isFavorite) Color(0xFFD59B00) else FeatureInk.copy(alpha = 0.36f),
                )
            }
        }
    }
}

@Composable
private fun FeatureBadge(status: FeatureStatus) {
    Surface(
        color = if (status == FeatureStatus.LIVE) Color(0xFFE3F5EF) else Color(0xFFFFF0D9),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = if (status == FeatureStatus.LIVE) FeatureGreen else FeatureOrange,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun FeatureDetail(
    feature: FeatureDefinition,
    state: LottoUiState,
    onAddNumberSet: () -> Unit,
    onOpenQr: () -> Unit,
    onSaveGeneratedSet: (String, List<Int>, String) -> Unit,
    onToggleFavorite: (SavedNumberSet) -> Unit,
    qrDisplayMode: SlipQrDisplayMode,
    onChangeQrDisplayMode: (SlipQrDisplayMode) -> Unit,
) {
    val selectedSet = state.numberSets.firstOrNull { it.favorite } ?: state.numberSets.firstOrNull()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SafetyNotice()
        }
        when (feature.id) {
            "daily_combo" -> item {
                DailyCombinationDetail(state.dailyCombinations, onSaveGeneratedSet)
            }
            "today_draw" -> item {
                TodayDrawNumberFeature(state.draws)
            }
            "save_number" -> item {
                SaveNumberDetail(
                    numberSets = state.numberSets,
                    qrDisplayMode = qrDisplayMode,
                    onChangeQrDisplayMode = onChangeQrDisplayMode,
                    onOpenQr = onOpenQr,
                    onAddNumberSet = onAddNumberSet,
                    onToggleFavorite = onToggleFavorite,
                )
            }
            "match_history" -> item {
                MatchHistoryFeature(state.numberSets, state.draws)
            }
            "number_stats" -> item {
                NumberStatsFeature(state.draws)
            }
            "number_flow" -> item {
                NumberFlowHeatmapFeature(state.draws)
            }
            "draw_history" -> item {
                DrawHistoryFeature(state.draws)
            }
            "probability" -> item {
                ProbabilityFeature(state.draws)
            }
            "scattered_numbers" -> item {
                ScatteredNumbersFeature(
                    draws = state.draws,
                    qrDisplayMode = qrDisplayMode,
                    onChangeQrDisplayMode = onChangeQrDisplayMode,
                    onSaveGeneratedSet = onSaveGeneratedSet,
                )
            }
            "digit_matcher" -> item {
                DigitMatcherFeature(
                    draws = state.draws,
                    qrDisplayMode = qrDisplayMode,
                    onChangeQrDisplayMode = onChangeQrDisplayMode,
                    onSaveGeneratedSet = onSaveGeneratedSet,
                )
            }
            "combo_map" -> item {
                CombinationMapFeature(state.draws, state.selectedDraw, state.numberSets)
            }
            "my_analysis" -> item {
                MyNumberAnalysisFeature(state.numberSets, state.draws, onSaveGeneratedSet)
            }
            "cooccurrence" -> item {
                CooccurrenceFeature(selectedSet, state.draws)
            }
            "share_card" -> item {
                ShareCardFeature(selectedSet, state.selectedDraw)
            }
            "notifications" -> item {
                NotificationFeature()
            }
            "qr" -> item {
                QrFeature(
                    numberSets = state.numberSets,
                    qrDisplayMode = qrDisplayMode,
                    onChangeQrDisplayMode = onChangeQrDisplayMode,
                )
            }
            "privacy_policy" -> item {
                PrivacyPolicyFeature()
            }
            else -> item {
                InfoCard("준비중", "이 기능의 상세 화면은 다음 업데이트에서 연결합니다.")
            }
        }
    }
}

@Composable
private fun SafetyNotice() {
    Surface(color = Color(0xFFFFF0D9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "번호를 맞히는 예측이 아니라, 내가 고른 번호의 과거 기록과 구간 분포를 보기 쉽게 분석합니다.",
            modifier = Modifier.padding(12.dp),
            color = FeatureInk,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DailyCombinationDetail(
    combinations: List<DailyCombination>,
    onSaveGeneratedSet: (String, List<Int>, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard(
            title = "오늘 하루 고정",
            body = "KST 날짜 기준으로 같은 날에는 같은 3개 조합을 다시 보여줍니다.",
        )
        if (combinations.isEmpty()) {
            InfoCard("계산 중", "회차 데이터를 불러온 뒤 오늘의 조합을 만들고 저장합니다.")
        } else {
            combinations.forEach { combination ->
                CardBlock {
                    Text(
                        text = combination.type,
                        color = FeatureInk,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = combinationReason(combination.type),
                        color = FeatureInk.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureNumberStrip(combination.numbers)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSaveGeneratedSet(
                                "${combination.type} ${combination.date.substring(5)}",
                                combination.numbers,
                                "오늘의 조합",
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("이 조합 저장")
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayDrawNumberFeature(draws: List<DrawResult>) {
    val context = LocalContext.current
    val today = remember { AdFeatureUsageStore.todayKey() }
    var usage by remember(today) {
        mutableStateOf(AdFeatureUsageStore.readTodayDrawUsage(context, today))
    }
    var revealedNumbers by remember(today) { mutableStateOf<List<Int>?>(null) }
    val allCandidates = remember(draws, today) {
        LottoAnalytics.cumulativeRecommendationCombinations(
            seedKey = "today-draw:$today",
            draws = draws,
            count = 5,
        )
    }
    val candidates = remember(allCandidates, usage.shownKeys) {
        allCandidates.filter { AdFeatureUsageStore.combinationKey(it) !in usage.shownKeys }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WarningCard(
            "이것은 누적확률 조합이지 당첨 예측이 아닙니다.",
            "다음 회차 실제 확률은 모든 조합이 같습니다. 오래 안 나온 번호의 누적 체감값만 사용해 오늘 볼 조합을 만듭니다.",
        )
        InfoCard(
            title = "오늘의 추첨번호",
            body = "오늘 후보 5개 중 하나를 15초 광고 뒤 하나씩 보여줍니다. 같은 사용자에게 오늘 이미 보여준 조합은 다시 보여주지 않고, 하루 4번까지만 열 수 있습니다.",
        )
        MetricGrid(
            metrics = listOf(
                "오늘 남은 열람" to "${usage.remainingUses}번",
                "오늘 본 조합" to "${usage.usedCount}개",
            ),
        )

        if (revealedNumbers == null) {
            if (usage.remainingUses <= 0) {
                InfoCard("오늘 제한 완료", "오늘은 4번을 모두 사용했습니다. 내일 다시 새로운 누적확률 조합을 볼 수 있습니다.")
            } else if (candidates.isEmpty()) {
                InfoCard("조합 없음", "오늘 보여줄 새 조합이 없습니다. 내일 다시 확인해보세요.")
            } else {
                AdCountdownCard(
                    title = "오늘의 추첨번호 광고",
                    body = "15초 광고가 끝나면 오늘의 누적확률 조합 1개를 보여줍니다.",
                    onFinished = {
                        revealedNumbers = AdFeatureUsageStore.claimTodayDrawCombination(
                            context = context,
                            candidates = candidates,
                            date = today,
                        )
                        usage = AdFeatureUsageStore.readTodayDrawUsage(context, today)
                    },
                )
            }
        } else {
            CardBlock {
                Text("오늘 공개된 조합", color = FeatureInk, fontWeight = FontWeight.Black)
                FeatureNumberStrip(revealedNumbers.orEmpty())
                Text(
                    "누적확률 체감 기준으로 만든 조합입니다. 실제 다음 회차 확률이 올라간다는 뜻은 아닙니다.",
                    color = FeatureInk.copy(alpha = 0.66f),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (usage.remainingUses > 0) {
                    OutlinedButton(
                        onClick = { revealedNumbers = null },
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("15초 광고 보고 다음 조합 보기")
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawHistoryFeature(draws: List<DrawResult>) {
    var visibleCount by remember { mutableIntStateOf(30) }
    val sortedDraws = remember(draws) { draws.sortedByDescending(DrawResult::round) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("회차별 당첨번호", "최근 회차부터 당첨번호를 한 줄씩 보여줍니다. 색은 번호대별 공 색상입니다.")
        CardBlock {
            Text("표 읽는 법", color = FeatureInk, fontWeight = FontWeight.Black)
            LegendLine("왼쪽 진회색: 회차", Color(0xFF4D4D4D))
            LegendLine("가운데 6칸: 당첨번호", FeatureBlue)
            LegendLine("오른쪽 + 뒤: 보너스 번호", Color(0xFF777777))
        }
        CardBlock {
            Text("최근 당첨번호", color = FeatureInk, fontWeight = FontWeight.Black)
            sortedDraws.take(visibleCount).forEach { draw ->
                DrawHistoryRow(draw)
            }
            if (visibleCount < sortedDraws.size) {
                TextButton(onClick = { visibleCount = (visibleCount + 30).coerceAtMost(sortedDraws.size) }) {
                    Text("30회 더 보기")
                }
            }
        }
    }
}

@Composable
private fun DrawHistoryRow(draw: DrawResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DrawHistoryCell(
            text = draw.round.toString(),
            color = Color(0xFF4F4F4F),
            modifier = Modifier.width(54.dp),
        )
        draw.mainNumbers.sorted().forEach { number ->
            DrawHistoryCell(
                text = number.toString(),
                color = featureBallColor(number),
                modifier = Modifier.weight(1f),
            )
        }
        DrawHistoryCell(
            text = "+",
            color = Color(0xFF444444),
            modifier = Modifier.width(34.dp),
        )
        DrawHistoryCell(
            text = draw.bonusNumber.toString(),
            color = featureBallColor(draw.bonusNumber),
            modifier = Modifier.weight(0.9f),
        )
    }
}

@Composable
private fun DrawHistoryCell(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(42.dp)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun CooccurrenceFeature(numberSet: SavedNumberSet?, draws: List<DrawResult>) {
    var selectedNumber by remember(numberSet?.id) {
        mutableIntStateOf(numberSet?.numbers?.firstOrNull() ?: 7)
    }
    val ranking = remember(selectedNumber, draws) {
        LottoAnalytics.cooccurrence(selectedNumber.coerceIn(1, 45), draws).take(12)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("번호 궁합", "과거 회차에서 함께 출현한 기록입니다. 미래 출현 확률을 의미하지 않습니다.")
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
                    selected = number == selectedNumber,
                    onClick = { selectedNumber = number },
                )
            }
        }
        CardBlock {
            Text("${selectedNumber}번과 함께 자주 나온 번호", color = FeatureInk, fontWeight = FontWeight.Black)
            ranking.forEachIndexed { index, (number, count) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1}위", modifier = Modifier.width(42.dp), color = FeatureOrange, fontWeight = FontWeight.Bold)
                    MiniLottoBall(number)
                    Text("${number}번", modifier = Modifier.weight(1f).padding(start = 10.dp), color = FeatureInk)
                    Text("${count}회", color = FeatureInk, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CollectionFeature(
    numberSets: List<SavedNumberSet>,
    onToggleFavorite: (SavedNumberSet) -> Unit,
) {
    val groups = numberSets.groupBy { it.collectionName.ifBlank { "기본" } }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("컬렉션", "메인 번호, 생일 번호, 꿈 번호처럼 번호 조합에 이름과 묶음을 주면 확인하기 쉽습니다.")
        if (groups.isEmpty()) {
            InfoCard("저장 번호 없음", "번호 저장 기능에서 첫 번호 세트를 만들어보세요.")
        }
        groups.forEach { (collection, sets) ->
            CardBlock {
                Text(collection, color = FeatureInk, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                sets.forEach { numberSet ->
                    SavedSetRow(numberSet = numberSet, onToggleFavorite = { onToggleFavorite(numberSet) })
                }
            }
        }
    }
}

@Composable
private fun ShareCardFeature(numberSet: SavedNumberSet?, draw: DrawResult?) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("공유 카드", "검증되지 않은 사용자 상위 비율 대신, 내 저장 번호 중 확인 가능한 결과만 카드로 만듭니다.")
        if (numberSet == null) {
            InfoCard("저장 번호 필요", "공유할 번호 세트를 먼저 저장해주세요.")
        } else {
            CardBlock {
                Text(numberSet.name, color = FeatureInk, fontWeight = FontWeight.Black)
                FeatureNumberStrip(numberSet.numbers)
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { LottoShareCard.share(context, numberSet, draw) }, shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Outlined.IosShare, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("이미지 카드 공유")
                }
            }
        }
    }
}

@Composable
private fun NotificationFeature() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    var enabledMap by remember {
        mutableStateOf<Map<ReminderType, Boolean>>(
            ReminderType.entries.associateWith { LottoNotificationScheduler.isEnabled(context, it) },
        )
    }

    LaunchedEffect(Unit) {
        LottoNotificationScheduler.cancelLegacyNextDay(context)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard("로컬 알림", "권한을 허용한 경우에만 기기 안에서 알림을 예약합니다. 토요일 19:00 번호 확인과 추첨 직후 리포트를 포함합니다.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            OutlinedButton(
                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("알림 권한 요청")
            }
        }
        ReminderType.entries.forEach { type ->
            val enabled = enabledMap[type] == true
            CardBlock {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(type.label, color = FeatureInk, fontWeight = FontWeight.Black)
                        Text(type.message, color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { checked: Boolean ->
                            LottoNotificationScheduler.setEnabled(context, type, checked)
                            enabledMap = enabledMap + (type to checked)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyFeature() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        InfoCard(
            title = "개인정보처리방침",
            body = "비공개 테스트 업로드 전에 Play Console에 넣을 정책 내용을 앱 안에서도 확인할 수 있게 둔 화면입니다.",
        )
        CardBlock {
            Text("수집/저장하는 정보", color = FeatureInk, fontWeight = FontWeight.Black)
            Text(
                "저장 번호 세트, 즐겨찾기, QR 보기 설정, 광고형 기능 사용 횟수, 알림 설정은 기기 안에 저장됩니다. 계정 로그인과 클라우드 동기화는 없습니다.",
                color = FeatureInk.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider(color = FeatureInk.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            Text("네트워크와 광고", color = FeatureInk, fontWeight = FontWeight.Black)
            Text(
                "앱은 동행복권 공식 회차 결과를 조회하고, 테스트 단계에서는 Google Mobile Ads SDK의 테스트 광고를 표시합니다. 광고 SDK는 기기 식별자 등 광고 제공에 필요한 정보를 처리할 수 있습니다.",
                color = FeatureInk.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider(color = FeatureInk.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            Text("삭제 방법", color = FeatureInk, fontWeight = FontWeight.Black)
            Text(
                "앱 안에서 저장 번호를 삭제할 수 있고, Android 앱 정보에서 저장공간을 지우면 로컬 데이터가 삭제됩니다.",
                color = FeatureInk.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        WarningCard(
            "정책 메모",
            "이 앱은 복권 구매, 공식 슬립 연동, 당첨 확률 상승을 제공하지 않습니다. 과거 기록과 누적 시행 계산을 보기 쉽게 보여주는 참고 도구입니다.",
        )
    }
}

private fun featureIcon(id: String): ImageVector = when (id) {
    "daily_combo" -> Icons.Outlined.AutoAwesome
    "today_draw" -> Icons.Outlined.AutoAwesome
    "save_number" -> Icons.Outlined.Save
    "match_history" -> Icons.Outlined.Timeline
    "number_stats" -> Icons.Outlined.BarChart
    "number_flow" -> Icons.Outlined.GridView
    "draw_history" -> Icons.Outlined.GridView
    "probability" -> Icons.Outlined.Functions
    "scattered_numbers" -> Icons.Outlined.Hub
    "digit_matcher" -> Icons.Outlined.Functions
    "combo_map" -> Icons.Outlined.Map
    "my_analysis" -> Icons.Outlined.Analytics
    "cooccurrence" -> Icons.Outlined.Hub
    "share_card" -> Icons.Outlined.IosShare
    "notifications" -> Icons.Outlined.Notifications
    "qr" -> Icons.Outlined.QrCode2
    "privacy_policy" -> Icons.Outlined.IosShare
    else -> Icons.Outlined.AutoAwesome
}

private fun iconTint(id: String): Color = when (id) {
    "daily_combo", "today_draw", "probability", "digit_matcher" -> FeatureOrange
    "number_stats", "number_flow", "draw_history", "cooccurrence" -> FeatureBlue
    "combo_map" -> FeatureGreen
    "my_analysis", "scattered_numbers" -> FeaturePurple
    "share_card", "notifications", "qr", "privacy_policy" -> Color(0xFF9B4AA6)
    else -> FeatureInk
}

private fun combinationReason(type: String): String = when {
    "랜덤" in type -> "오늘 날짜를 기준으로 고정된 랜덤 조합입니다."
    "미출현" in type -> "최근 기록에서 오래 나오지 않은 번호를 우선 정렬한 조합입니다."
    "인기번호" in type -> "최근 100회 상위 출현 번호를 제외하고 만든 참고 조합입니다."
    else -> "기록 확인용 참고 조합입니다."
}


