package com.lottolab.probability

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lottolab.probability.ui.LottoLabApp
import com.lottolab.probability.ui.LottoProbabilityLabTheme
import com.lottolab.probability.ui.LottoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val application = application as LottoLabApplication
            val viewModel: LottoViewModel = viewModel(
                factory = LottoViewModel.Factory(application.repository),
            )

            LottoProbabilityLabTheme {
                LottoLabApp(viewModel)
            }
        }
    }
}

