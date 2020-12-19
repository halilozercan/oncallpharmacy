package com.halilibo.eczane

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.shared.model.Pharmacy
import com.halilibo.shared.repository.PharmacyRepository
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    val pharmacyRepository: PharmacyRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val pharmacies by produceState<List<Pharmacy>>(
                initialValue = listOf(),
                producer = {
                    pharmacyRepository.getPharmaciesByCityAsFlow(1).collect {
                        value = it
                    }
                })

            LazyColumnFor(items = pharmacies, contentPadding = PaddingValues(8.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = it.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps?q=${it.latitude},${it.longitude}"))
                            startActivity(browserIntent)
                        }))
                    Text(
                        text = it.address
                    )
                }
            }
        }
    }
}
