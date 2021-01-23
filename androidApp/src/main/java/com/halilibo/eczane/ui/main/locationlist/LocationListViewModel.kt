package com.halilibo.eczane.ui.main.locationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halilibo.eczane.ui.common.StatefulViewModel
import com.halilibo.shared.model.City
import com.halilibo.shared.repository.PharmacyRepository
import kotlinx.coroutines.flow.*

class LocationListViewModel(
    pharmacyRepository: PharmacyRepository
) : StatefulViewModel<LocationListState>(LocationListState()) {

    init {
        pharmacyRepository.getCities().onEach { cityList ->
            setState { it.copy(cityList = cityList) }
        }.launchIn(viewModelScope)
    }

}

data class LocationListState(
    val cityList: List<City> = emptyList(),
)
