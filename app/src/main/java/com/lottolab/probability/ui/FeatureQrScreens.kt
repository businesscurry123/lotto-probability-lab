package com.lottolab.probability.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lottolab.probability.domain.SavedNumberSet
import com.lottolab.probability.domain.SlipQrDisplayMode
import com.lottolab.probability.domain.SlipQrPlanner
import com.lottolab.probability.qr.QrCodeGenerator

@Composable
internal fun QrFeature(
    numberSets: List<SavedNumberSet>,
    qrDisplayMode: SlipQrDisplayMode,
    onChangeQrDisplayMode: (SlipQrDisplayMode) -> Unit,
) {
    val pages = remember(numberSets, qrDisplayMode) { SlipQrPlanner.pages(numberSets, qrDisplayMode) }
    var pageIndex by remember(pages.size, qrDisplayMode) { mutableIntStateOf(0) }
    val safePageIndex = pageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val page = pages.getOrNull(safePageIndex)
    val content = page?.let(SlipQrPlanner::qrContent) ?: "lotto-lab://slip-qr?empty=true"
    val bitmap = remember(content) { QrCodeGenerator.createBitmap(content, 560) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        InfoCard("앱 내부 확인용 QR", "구매나 공식 모바일 슬립지 연동이 아니라, 저장 번호를 앱 안에서 확인하기 위한 QR입니다.")
        CardBlock {
            Text("표시 방식", color = FeatureInk, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SlipQrDisplayMode.entries.forEach { mode ->
                    OutlinedButton(
                        onClick = {
                            pageIndex = 0
                            onChangeQrDisplayMode(mode)
                        },
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
            Text(qrDisplayMode.description, color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
        }
        CardBlock {
            Text(
                text = if (pages.isEmpty()) "저장 번호 없음" else "${safePageIndex + 1}/${pages.size} 페이지",
                color = FeatureInk,
                fontWeight = FontWeight.Black,
            )
            if (page == null) {
                Text("저장 번호를 만든 뒤 QR로 확인할 수 있습니다.", color = FeatureInk.copy(alpha = 0.66f))
            } else {
                page.sets.forEach { numberSet ->
                    Text(numberSet.name, color = FeatureInk, fontWeight = FontWeight.Bold)
                    FeatureNumberStrip(numberSet.numbers)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "저장 번호 QR",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            )
            if (pages.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { pageIndex = (safePageIndex - 1).coerceAtLeast(0) },
                        enabled = safePageIndex > 0,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("이전")
                    }
                    OutlinedButton(
                        onClick = { pageIndex = (safePageIndex + 1).coerceAtMost(pages.lastIndex) },
                        enabled = safePageIndex < pages.lastIndex,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("다음")
                    }
                }
            }
        }
    }
}

@Composable
internal fun GeneratedCombinationList(
    sets: List<SavedNumberSet>,
    onSaveGeneratedSet: (String, List<Int>, String) -> Unit,
) {
    CardBlock {
        Text("생성된 조합", color = FeatureInk, fontWeight = FontWeight.Black)
        if (sets.isEmpty()) {
            Text("아직 표시할 조합이 없습니다.", color = FeatureInk.copy(alpha = 0.66f))
        } else {
            sets.forEach { set ->
                HorizontalDivider(color = FeatureInk.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                Text(set.name, color = FeatureInk, fontWeight = FontWeight.Bold)
                FeatureNumberStrip(set.numbers)
                TextButton(onClick = { onSaveGeneratedSet(set.name, set.numbers, set.collectionName) }) {
                    Text("저장")
                }
            }
        }
    }
}

@Composable
internal fun GeneratedQrPreview(
    title: String,
    sets: List<SavedNumberSet>,
    qrDisplayMode: SlipQrDisplayMode,
    onChangeQrDisplayMode: (SlipQrDisplayMode) -> Unit,
) {
    val pages = remember(sets, qrDisplayMode) { SlipQrPlanner.pages(sets, qrDisplayMode) }
    var pageIndex by remember(pages.size, qrDisplayMode) { mutableIntStateOf(0) }
    val safePageIndex = pageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
    val page = pages.getOrNull(safePageIndex)
    val content = page?.let(SlipQrPlanner::qrContent) ?: "lotto-lab://generated-qr?empty=true"
    val bitmap = remember(content) { QrCodeGenerator.createBitmap(content, 560) }

    CardBlock {
        Text(title, color = FeatureInk, fontWeight = FontWeight.Black)
        Text("앱 내부 확인용 QR입니다. 공식 구매나 슬립지 연동을 의미하지 않습니다.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SlipQrDisplayMode.entries.forEach { mode ->
                OutlinedButton(
                    onClick = {
                        pageIndex = 0
                        onChangeQrDisplayMode(mode)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(mode.label, color = if (mode == qrDisplayMode) FeatureGreen else FeatureInk, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }
        Text(
            text = if (pages.isEmpty()) "QR 페이지 없음" else "${safePageIndex + 1}/${pages.size} 페이지",
            color = FeatureInk,
            fontWeight = FontWeight.Bold,
        )
        page?.sets.orEmpty().forEach { set ->
            Text(set.name, color = FeatureInk, fontWeight = FontWeight.Bold)
            FeatureNumberStrip(set.numbers)
        }
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "$title QR",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        )
        if (pages.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { pageIndex = (safePageIndex - 1).coerceAtLeast(0) },
                    enabled = safePageIndex > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) { Text("이전") }
                OutlinedButton(
                    onClick = { pageIndex = (safePageIndex + 1).coerceAtMost(pages.lastIndex) },
                    enabled = safePageIndex < pages.lastIndex,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) { Text("다음") }
            }
        }
    }
}
