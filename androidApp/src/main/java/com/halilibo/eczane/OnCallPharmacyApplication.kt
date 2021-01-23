package com.halilibo.eczane

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import com.halilibo.eczane.di.appModule
import com.halilibo.eczane.di.viewModelModule
import com.halilibo.shared.di.initKoin
import com.halilibo.shared.repository.appContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class OnCallPharmacyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        appContext = this

        initKoin {
            androidLogger()
            androidContext(this@OnCallPharmacyApplication)
            modules(appModule, viewModelModule)
        }

        MapsInitializer.initialize(this)
    }
}