package com.lottolab.probability.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.FileProvider
import com.lottolab.probability.domain.DrawResult
import com.lottolab.probability.domain.LottoAnalytics
import com.lottolab.probability.domain.SavedNumberSet
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object LottoShareCard {
    fun share(context: Context, numberSet: SavedNumberSet, draw: DrawResult?) {
        val bitmap = createBitmap(numberSet, draw)
        val outputDir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
        val outputFile = File(outputDir, "lotto-share-${numberSet.id}.png")
        FileOutputStream(outputFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "로또 누적확률 연구소 (AI) - ${numberSet.name} 기록 카드",
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "결과 공유"))
    }

    private fun createBitmap(numberSet: SavedNumberSet, draw: DrawResult?): Bitmap {
        val width = 1080
        val height = 1500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(Color.rgb(246, 241, 236))
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(60f, 70f, 1020f, 1430f), 42f, 42f, paint)

        paint.color = Color.rgb(23, 48, 67)
        paint.textSize = 54f
        paint.isFakeBoldText = true
        canvas.drawText("로또 누적확률 연구소 (AI)", 110f, 170f, paint)

        paint.textSize = 72f
        canvas.drawText(numberSet.name, 110f, 300f, paint)

        paint.textSize = 34f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(88, 105, 116)
        canvas.drawText("내 저장 번호 중 이번 회차 확인 카드", 110f, 365f, paint)

        val ballY = 500f
        numberSet.numbers.sorted().forEachIndexed { index, number ->
            drawBall(canvas, 145f + index * 150f, ballY, number)
        }

        if (draw != null) {
            val match = LottoAnalytics.match(numberSet.numbers, draw)
            val nearMiss = LottoAnalytics.nearMiss(numberSet.numbers, draw)
            paint.color = Color.rgb(32, 125, 162)
            paint.textSize = 46f
            paint.isFakeBoldText = true
            canvas.drawText("${draw.round}회 기준 ${match.matchedCount}개 적중", 110f, 700f, paint)

            paint.color = Color.rgb(232, 109, 40)
            canvas.drawText("아깝다 지수 ${nearMiss.score}점", 110f, 780f, paint)

            paint.color = Color.rgb(23, 48, 67)
            paint.textSize = 38f
            paint.isFakeBoldText = false
            canvas.drawText("등위: ${match.rank.label}", 110f, 860f, paint)
            canvas.drawText("±1 근접: ${nearMiss.nearCount}개", 110f, 930f, paint)
        } else {
            paint.color = Color.rgb(23, 48, 67)
            paint.textSize = 44f
            paint.isFakeBoldText = true
            canvas.drawText("회차 결과를 불러온 뒤 적중 기록이 표시됩니다.", 110f, 720f, paint)
        }

        paint.color = Color.rgb(88, 105, 116)
        paint.textSize = 32f
        paint.isFakeBoldText = false
        drawMultiline(
            canvas = canvas,
            paint = paint,
            text = "아깝다 지수와 통계는 재미용 참고 지표이며 다음 회차 확률을 높이지 않습니다.",
            x = 110f,
            y = 1200f,
            maxChars = 27,
            lineHeight = 48f,
        )

        paint.textSize = 28f
        canvas.drawText("생성: 로또 누적확률 연구소 (AI)", 110f, 1360f, paint)
        return bitmap
    }

    private fun drawBall(canvas: Canvas, cx: Float, cy: Float, number: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = when (number) {
            in 1..10 -> Color.rgb(231, 172, 24)
            in 11..20 -> Color.rgb(37, 143, 208)
            in 21..30 -> Color.rgb(232, 102, 94)
            in 31..40 -> Color.rgb(101, 114, 126)
            else -> Color.rgb(67, 168, 81)
        }
        canvas.drawCircle(cx, cy, 58f, paint)

        paint.color = Color.WHITE
        paint.textSize = 42f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText(number.toString(), cx, cy + 15f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawMultiline(
        canvas: Canvas,
        paint: Paint,
        text: String,
        x: Float,
        y: Float,
        maxChars: Int,
        lineHeight: Float,
    ) {
        text.chunked(maxChars).forEachIndexed { index, line ->
            canvas.drawText(line, x, y + index * lineHeight, paint)
        }
    }

    fun formatPercent(value: Double): String = String.format(Locale.KOREA, "%.4f%%", value * 100.0)
}
