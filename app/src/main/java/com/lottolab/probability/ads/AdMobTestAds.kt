package com.lottolab.probability.ads

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

object AdMobTestIds {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"
    const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
}

@Composable
fun AdMobTopBanner(modifier: Modifier = Modifier) {
    var loadFailed by remember { mutableStateOf(false) }

    Surface(color = Color(0xFFDCF1E7), modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (!loadFailed) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = AdMobTestIds.BANNER
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    loadFailed = false
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    loadFailed = true
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                )
            }
            if (loadFailed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .border(1.dp, Color(0xFF157766).copy(alpha = 0.28f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "상단 광고 영역",
                        color = Color(0xFF157766),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
