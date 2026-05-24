package com.lottolab.probability.data.remote

import com.lottolab.probability.domain.DrawResult
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class DhlotteryRemoteDataSource {
    suspend fun discoverLatestRound(): Int = withContext(Dispatchers.IO) {
        val html = requestText(RESULT_PAGE_URL)
        LATEST_ROUND_REGEX.find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
            ?: throw IOException("최신 회차를 공식 결과 화면에서 찾지 못했습니다.")
    }

    suspend fun fetchWindow(centerRound: Int): List<DrawResult> = withContext(Dispatchers.IO) {
        val json = JSONObject(requestText("$WINDOW_URL$centerRound"))
        val results = json.optJSONObject("data")?.optJSONArray("list") ?: JSONArray()
        buildList {
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index) ?: continue
                item.toDrawResult()?.let(::add)
            }
        }.distinctBy(DrawResult::round)
    }

    private fun JSONObject.toDrawResult(): DrawResult? {
        val round = optInt("ltEpsd")
        val date = optString("ltRflYmd")
        if (round <= 0 || date.isBlank()) return null

        return DrawResult(
            round = round,
            drawDate = date,
            mainNumbers = listOf(
                optInt("tm1WnNo"),
                optInt("tm2WnNo"),
                optInt("tm3WnNo"),
                optInt("tm4WnNo"),
                optInt("tm5WnNo"),
                optInt("tm6WnNo"),
            ).sorted(),
            bonusNumber = optInt("bnsWnNo"),
        )
    }

    private fun requestText(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json,text/html")
        connection.setRequestProperty("User-Agent", "LottoProbabilityLab-Android/1.0")

        return try {
            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                throw IOException("공식 결과 요청 실패: HTTP $statusCode")
            }
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val RESULT_PAGE_URL = "https://www.dhlottery.co.kr/lt645/result"
        private const val WINDOW_URL =
            "https://www.dhlottery.co.kr/lt645/selectPstLt645InfoNew.do?srchLtEpsd="
        private val LATEST_ROUND_REGEX = Regex("""id="opt_val"[^>]*value="(\d+)"""")
    }
}

