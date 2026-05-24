package com.lottolab.probability.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.LottoDisplayRules
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.SavedNumberSet
import com.lottolab.probability.domain.SlipQrDisplayMode
import com.lottolab.probability.settings.FeatureFavoritesStore
import com.lottolab.probability.settings.SlipQrSettingsStore
import java.util.Locale

internal val LabBackground = Color(0xFFF6F1EC)
internal val HeaderCream = Color(0xFFFFF8F1)
internal val Ink = Color(0xFF173043)
internal val Citrus = Color(0xFFE86D28)
internal val Violet = Color(0xFF6E4DB4)
internal val Spruce = Color(0xFF157766)
internal val River = Color(0xFF207DA2)

@Composable
fun LottoProbabilityLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Ink,
            secondary = Citrus,
            tertiary = Spruce,
            background = LabBackground,
            surface = HeaderCream,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Ink,
            onSurface = Ink,
        ),
        typography = MaterialTheme.typography,
        content = content,
    )
}

@Composable
fun LottoLabApp(viewModel: LottoViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var editingSet by remember { mutableStateOf<SavedNumberSet?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var detailSet by remember { mutableStateOf<SavedNumberSet?>(null) }
    var featureCenterOpen by remember { mutableStateOf(false) }
    var featureCenterStartFeature by remember { mutableStateOf<String?>(null) }
    var featureCenterFavoritesOnly by remember { mutableStateOf(false) }
    var favoriteFeatureIds by remember {
        mutableStateOf(FeatureFavoritesStore.readFavorites(context))
    }
    var qrDisplayMode by remember {
        mutableStateOf(SlipQrSettingsStore.readDisplayMode(context))
    }

    fun openFeature(featureId: String?, favoritesOnly: Boolean = false) {
        featureCenterStartFeature = featureId
        featureCenterFavoritesOnly = favoritesOnly
        featureCenterOpen = true
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = LabBackground,
    ) { paddingValues ->
        LottoHomeScreen(
            state = uiState,
            modifier = Modifier.padding(paddingValues),
            onRefresh = viewModel::refreshDraws,
            onSelectRound = viewModel::selectRound,
            onAdd = {
                editingSet = null
                editorOpen = true
            },
            onOpenSet = { detailSet = it },
            onOpenFeatures = { openFeature(null) },
            onOpenFavoriteFeatures = { openFeature(null, favoritesOnly = true) },
            onOpenQr = { openFeature("qr") },
            onOpenTodayDraw = { openFeature("today_draw") },
            onOpenNumberSettings = { openFeature("save_number") },
        )
    }

    if (editorOpen) {
        NumberSetEditorDialog(
            initial = editingSet,
            onDismiss = { editorOpen = false },
            onSave = { name, numbers, favorite, collectionName ->
                viewModel.saveNumberSet(editingSet, name, numbers, favorite, collectionName)
                editorOpen = false
            },
        )
    }

    detailSet?.let { numberSet ->
        NumberSetDetailDialog(
            numberSet = numberSet,
            draw = uiState.selectedDraw,
            draws = uiState.draws,
            historyReady = uiState.historyReady,
            onDismiss = { detailSet = null },
            onEdit = {
                editingSet = numberSet
                detailSet = null
                editorOpen = true
            },
            onDelete = {
                viewModel.deleteNumberSet(numberSet)
                detailSet = null
            },
        )
    }

    if (featureCenterOpen) {
        FeatureCenterScreen(
            state = uiState,
            initialFeatureId = featureCenterStartFeature,
            showFavoritesOnly = featureCenterFavoritesOnly,
            favoriteFeatureIds = favoriteFeatureIds,
            onToggleFeatureFavorite = { featureId ->
                favoriteFeatureIds = if (featureId in favoriteFeatureIds) {
                    favoriteFeatureIds - featureId
                } else {
                    favoriteFeatureIds + featureId
                }
                FeatureFavoritesStore.saveFavorites(context, favoriteFeatureIds)
            },
            onClose = { featureCenterOpen = false },
            onAddNumberSet = {
                featureCenterOpen = false
                editingSet = null
                editorOpen = true
            },
            onOpenQr = { openFeature("qr") },
            onSaveGeneratedSet = { name, numbers, collectionName ->
                viewModel.saveNumberSet(
                    existing = null,
                    name = name,
                    numbers = numbers,
                    favorite = false,
                    collectionName = collectionName,
                )
            },
            onToggleFavorite = viewModel::toggleFavorite,
            onClearActionMessage = viewModel::clearActionMessage,
            qrDisplayMode = qrDisplayMode,
            onChangeQrDisplayMode = { mode: SlipQrDisplayMode ->
                qrDisplayMode = mode
                SlipQrSettingsStore.saveDisplayMode(context, mode)
            },
        )
    }
}

@Composable
private fun LottoHomeScreen(
    state: LottoUiState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onSelectRound: (Int) -> Unit,
    onAdd: () -> Unit,
    onOpenSet: (SavedNumberSet) -> Unit,
    onOpenFeatures: () -> Unit,
    onOpenFavoriteFeatures: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenTodayDraw: () -> Unit,
    onOpenNumberSettings: () -> Unit,
) {
    val selectedDraw = state.selectedDraw
    val homeNumberSets = LottoDisplayRules.mainNumberSets(state.numberSets)

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        AppHeader(
            onFeatureClick = onOpenFeatures,
            onFavoriteFeatureClick = onOpenFavoriteFeatures,
        )
        AdBand()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
            DrawHeaderBand(
                draws = state.draws,
                selectedDraw = selectedDraw,
                isSyncing = state.isSyncing,
                syncMessage = state.syncMessage,
                onRefresh = onRefresh,
                onSelectRound = onSelectRound,
            )
            FeatureDock(onOpenQr = onOpenQr, onOpenTodayDraw = onOpenTodayDraw)
        }

            item {
            SavedNumberBand(
                numberSets = homeNumberSets,
                allSavedCount = state.numberSets.size,
                selectedDraw = selectedDraw,
                onAdd = onAdd,
                onOpenManage = onOpenNumberSettings,
                onOpenSet = onOpenSet,
            )
        }

            if (selectedDraw != null && homeNumberSets.isNotEmpty()) {
                item {
                LeaderboardBand(
                    entries = LottoAnalytics.leaderboard(homeNumberSets, selectedDraw),
                    round = selectedDraw.round,
                )
            }
        }

            if (selectedDraw != null) {
                item {
                ReportBand(
                    report = LottoAnalytics.roundReport(selectedDraw, state.draws),
                    draw = selectedDraw,
                )
            }
        }

            item {
            Text(
                text = "분석과 아깝다 지수는 기록 확인과 재미를 위한 정보입니다.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                color = Ink.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
        }
    }
}

@Composable
private fun AppHeader(
    onFeatureClick: () -> Unit,
    onFavoriteFeatureClick: () -> Unit,
) {
    Surface(color = HeaderCream, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onFeatureClick) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = "부가기능 열기",
                        tint = River,
                    )
                }
                Text(
                    text = "부가기능",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink.copy(alpha = 0.68f),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "로또 누적확률 연구소",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Ink,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "(AI)",
                    style = MaterialTheme.typography.labelLarge,
                    color = Citrus,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onFavoriteFeatureClick) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "즐겨찾기 부가기능 열기",
                        tint = Color(0xFFD59B00),
                    )
                }
                Text(
                    text = "즐겨찾기",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink.copy(alpha = 0.68f),
                )
            }
        }
    }
}

@Composable
private fun DrawHeaderBand(
    draws: List<DrawResult>,
    selectedDraw: DrawResult?,
    isSyncing: Boolean,
    syncMessage: String,
    onRefresh: () -> Unit,
    onSelectRound: (Int) -> Unit,
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (selectedDraw == null) {
                Text(
                    text = "당첨번호를 불러오는 중입니다.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                RoundSelector(draws, selectedDraw, onSelectRound)
                Text(
                    text = formatDrawDate(selectedDraw.drawDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink.copy(alpha = 0.68f),
                )
                Spacer(modifier = Modifier.height(18.dp))
                WinningBallRow(selectedDraw)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = syncMessage,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSyncing) River else Ink.copy(alpha = 0.68f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "회차 갱신",
                        tint = Ink,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundSelector(
    draws: List<DrawResult>,
    selectedDraw: DrawResult,
    onSelectRound: (Int) -> Unit,
) {
    var pickerOpen by remember { mutableStateOf(false) }

    TextButton(onClick = { pickerOpen = true }) {
        Text(
            text = "${selectedDraw.round}회 당첨번호",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Ink,
        )
        Icon(
            imageVector = Icons.Outlined.ExpandMore,
            contentDescription = "역대 회차 선택",
            tint = Ink,
        )
    }

    if (pickerOpen) {
        AlertDialog(
            onDismissRequest = { pickerOpen = false },
            title = { Text("역대 회차 선택") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                ) {
                    items(draws, key = DrawResult::round) { draw ->
                        TextButton(
                            onClick = {
                                onSelectRound(draw.round)
                                pickerOpen = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "${draw.round}회 ${formatDrawDate(draw.drawDate)}",
                                modifier = Modifier.fillMaxWidth(),
                                color = if (draw.round == selectedDraw.round) River else Ink,
                                fontWeight = if (draw.round == selectedDraw.round) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickerOpen = false }) { Text("닫기") }
            },
        )
    }
}

@Composable
private fun WinningBallRow(draw: DrawResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        draw.mainNumbers.forEach { number ->
            LottoBall(number = number)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = "+",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            fontWeight = FontWeight.Black,
        )
        Spacer(modifier = Modifier.width(4.dp))
        LottoBall(number = draw.bonusNumber)
    }
}

@Composable
private fun LottoBall(number: Int, compact: Boolean = false) {
    val size = if (compact) 28.dp else 38.dp
    Box(
        modifier = Modifier
            .size(size)
            .background(ballColor(number), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun FeatureDock(onOpenQr: () -> Unit, onOpenTodayDraw: () -> Unit) {
    Surface(color = Color(0xFFE7EEF2), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlaceholderFeatureButton(
                modifier = Modifier.weight(1f),
                title = "모바일 슬립지 QR",
                icon = Icons.Outlined.QrCode2,
                color = Citrus,
                onClick = onOpenQr,
                status = "열기",
            )
            PlaceholderFeatureButton(
                modifier = Modifier.weight(1f),
                title = "오늘의 추첨번호",
                icon = Icons.Outlined.CalendarToday,
                color = Violet,
                onClick = onOpenTodayDraw,
                status = "광고 후 열기",
            )
        }
    }
}

@Composable
private fun PlaceholderFeatureButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    status: String = "준비중",
) {
    Surface(
        onClick = onClick,
        color = color,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .height(68.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White)
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
                Text(
                    text = status,
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

internal fun ballColor(number: Int): Color = when (number) {
    in 1..10 -> Color(0xFFE7AC18)
    in 11..20 -> Color(0xFF258FD0)
    in 21..30 -> Color(0xFFE8665E)
    in 31..40 -> Color(0xFF65727E)
    else -> Color(0xFF43A851)
}

private fun formatDrawDate(rawDate: String): String {
    if (rawDate.length != 8) return rawDate
    return "${rawDate.substring(0, 4)}년 ${rawDate.substring(4, 6)}월 ${rawDate.substring(6, 8)}일 추첨"
}
