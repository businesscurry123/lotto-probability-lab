package com.lottolab.probability.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

internal val FeatureBackground = Color(0xFFF6F1EC)
internal val FeatureSurface = Color(0xFFFFF8F1)
internal val FeatureInk = Color(0xFF173043)
internal val FeatureOrange = Color(0xFFE86D28)
internal val FeatureGreen = Color(0xFF157766)
internal val FeatureBlue = Color(0xFF207DA2)
internal val FeaturePurple = Color(0xFF6E4DB4)

@Composable
internal fun MetricGrid(metrics: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowMetrics.forEach { (label, value) ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(label, color = FeatureInk.copy(alpha = 0.62f), style = MaterialTheme.typography.bodySmall)
                            Text(value, color = FeatureInk, fontWeight = FontWeight.Black, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (rowMetrics.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content,
        )
    }
}

@Composable
internal fun InfoCard(title: String, body: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = FeatureInk, fontWeight = FontWeight.Black)
            Text(body, color = FeatureInk.copy(alpha = 0.68f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun WarningCard(title: String, body: String) {
    Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = Color(0xFFC62828), fontWeight = FontWeight.Black)
            Text(body, color = Color(0xFF8E1B1B), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun LegendLine(label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp)),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = FeatureInk, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun FeatureNumberStrip(numbers: List<Int>) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        numbers.sorted().forEach { MiniLottoBall(it) }
    }
}

@Composable
internal fun MiniLottoBall(number: Int, size: Int = 30) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(featureBallColor(number), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
internal fun PickNumberBubble(number: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(if (selected) featureBallColor(number) else Color(0xFFE7E0D7), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            color = if (selected) Color.White else FeatureInk,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

internal fun featureBallColor(number: Int): Color = when (number) {
    in 1..10 -> Color(0xFFE7AC18)
    in 11..20 -> Color(0xFF258FD0)
    in 21..30 -> Color(0xFFE8665E)
    in 31..40 -> Color(0xFF65727E)
    else -> Color(0xFF43A851)
}


