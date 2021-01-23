package com.halilibo.eczane.di

import com.halilibo.eczane.ui.main.home.HomeViewModel
import com.halilibo.eczane.ui.main.locationlist.LocationListViewModel
import com.halilibo.eczane.ui.main.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { LocationListViewModel(get()) }
}