package com.halilibo.eczane.di

import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.halilibo.eczane.util.locationFlow
import kotlinx.coroutines.flow.Flow
import org.koin.dsl.module

val appModule = module {
    single<Flow<Location>> {
        LocationServices.getFusedLocationProviderClient(get<Context>())
            .locationFlow()
    }
}
