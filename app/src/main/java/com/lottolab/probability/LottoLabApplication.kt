package com.lottolab.probability

import android.app.Application
import com.lottolab.probability.data.LottoRepository
import com.lottolab.probability.data.local.LottoDatabase
import com.lottolab.probability.data.remote.DhlotteryRemoteDataSource
import com.google.android.gms.ads.MobileAds

class LottoLabApplication : Application() {
    private val database by lazy { LottoDatabase.getInstance(this) }

    val repository by lazy {
        LottoRepository(
            database = database,
            remoteDataSource = DhlotteryRemoteDataSource(),
        )
    }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) {}
    }
}
