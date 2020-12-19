package com.halilibo.eczane

import android.app.Application
import co.touchlab.kermit.Kermit
import com.halilibo.eczane.di.appModule
import com.halilibo.shared.di.initKoin
import com.halilibo.shared.repository.appContext
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class OnCallPharmacyApplication : Application() {
    private val logger: Kermit by inject()

    override fun onCreate() {
        super.onCreate()

        appContext = this

        initKoin {
            androidLogger()
            androidContext(this@OnCallPharmacyApplication)
            modules(appModule)
        }

        logger.d { "OnCallPharmacyApplication" }
    }
}