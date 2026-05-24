package com.lottolab.probability.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.lottolab.probability.ads.AdMobTestIds
import kotlinx.coroutines.delay

@Composable
internal fun AdCountdownCard(
    title: String,
    body: String,
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    var remainingSeconds by remember(title, body) { mutableIntStateOf(15) }
    var finished by remember(title, body) { mutableStateOf(false) }
    var fallbackCountdown by remember(title, body) { mutableStateOf(false) }
    var rewardedAd by remember(title, body) { mutableStateOf<RewardedAd?>(null) }
    var loadingAd by remember(title, body) { mutableStateOf(true) }
    var adMessage by remember(title, body) { mutableStateOf("AdMob 테스트 광고를 불러오는 중입니다.") }

    fun finishOnce() {
        if (!finished) {
            finished = true
            onFinished()
        }
    }

    LaunchedEffect(title, body) {
        loadingAd = true
        fallbackCountdown = false
        rewardedAd = null
        remainingSeconds = 15
        finished = false
        adMessage = "AdMob 테스트 광고를 불러오는 중입니다."
        RewardedAd.load(
            context,
            AdMobTestIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    loadingAd = false
                    adMessage = "테스트 광고가 준비되었습니다."
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    loadingAd = false
                    fallbackCountdown = true
                    adMessage = "테스트 광고를 불러오지 못했습니다. 개발 확인용 15초 대기로 계속합니다."
                }
            },
        )
    }

    LaunchedEffect(remainingSeconds, fallbackCountdown, finished) {
        if (!fallbackCountdown || finished) return@LaunchedEffect
        if (remainingSeconds > 0) {
            delay(1_000)
            remainingSeconds -= 1
        } else {
            finishOnce()
        }
    }

    CardBlock {
        Text(title, color = FeatureInk, fontWeight = FontWeight.Black)
        Text(body, color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
        Text(adMessage, color = FeatureOrange, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        rewardedAd?.let { ad ->
            Button(
                onClick = {
                    val activity = context.findActivity()
                    if (activity == null) {
                        adMessage = "광고 화면을 열 수 없어 개발 확인용 15초 대기로 계속합니다."
                        fallbackCountdown = true
                        rewardedAd = null
                    } else {
                        rewardedAd = null
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                if (!finished) {
                                    adMessage = "광고를 끝까지 확인하지 않아 기능이 아직 열리지 않았습니다."
                                }
                            }

                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                adMessage = "테스트 광고 표시가 실패했습니다. 개발 확인용 15초 대기로 계속합니다."
                                fallbackCountdown = true
                            }
                        }
                        ad.show(activity) {
                            finishOnce()
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                enabled = !finished,
            ) {
                Text("AdMob 테스트 광고 보기")
            }
        }
        if (loadingAd) {
            Text("잠시만 기다려주세요.", color = FeatureInk.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color(0xFFE8ECEE), RoundedCornerShape(999.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(((15 - remainingSeconds).toFloat() / 15f).coerceIn(0.03f, 1f))
                    .height(12.dp)
                    .background(FeatureOrange, RoundedCornerShape(999.dp)),
            )
        }
        Text(
            when {
                finished -> "광고 확인 완료"
                fallbackCountdown -> "${remainingSeconds}초 남음"
                else -> "테스트 광고 대기 중"
            },
            color = FeatureOrange,
            fontWeight = FontWeight.Black,
        )
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
