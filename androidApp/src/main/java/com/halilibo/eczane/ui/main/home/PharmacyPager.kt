package com.halilibo.eczane.ui.main.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientAnimationClock
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.halilibo.eczane.R
import com.halilibo.eczane.ui.common.Pager
import com.halilibo.eczane.ui.common.PagerState
import com.halilibo.shared.model.Pharmacy

@Composable
fun PharmacyPager(
    pharmacyList: List<Pharmacy>,
    selectedPharmacy: Pharmacy?,
    onPharmacySelect: (Pharmacy) -> Unit,
    distance: (Pharmacy) -> String,
    modifier: Modifier = Modifier
) {
    val clock = AmbientAnimationClock.current
    val context = AmbientContext.current

    val pagerState = remember(pharmacyList, selectedPharmacy) {
        val index = pharmacyList.indexOf(selectedPharmacy)
        val currentPage = index.coerceAtLeast(0)
        val maxPage = (pharmacyList.size - 1).coerceAtLeast(0)
        PagerState(clock, currentPage = currentPage, maxPage = maxPage)
    }

    LaunchedEffect(pagerState.currentPage, pagerState.selectionState) {
        if (pagerState.selectionState == PagerState.SelectionState.Selected) {
            onPharmacySelect(pharmacyList[pagerState.currentPage])
        }
    }

    Pager(
        state = pagerState,
        modifier = modifier
    ) {
        val pharmacy = pharmacyList[page]

        Card(
            shape = RoundedCornerShape(8.dp),
            backgroundColor = MaterialTheme.colors.background,
            elevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.85f).padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pharmacy.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.W500,
                    )
                }

                Text(
                    text = pharmacy.address,
                    fontSize = 14.sp
                )

                if (pharmacy.notes.isNotBlank()) {
                    Text(
                        text = pharmacy.notes,
                        color = Color(0xFF28a745),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val intent =
                                Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.fromParts("tel", pharmacy.phone, null)
                                )
                            ContextCompat.startActivity(context, intent, null)
                        },
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Icon(Icons.Default.Phone)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pharmacy.phone,
                            fontSize = 16.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("google.navigation:q=${pharmacy.latitude},${pharmacy.longitude}")
                            )
                            ContextCompat.startActivity(context, browserIntent, null)
                        },
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Icon(Icons.Default.Directions)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stringResource(R.string.navigate)} ${distance(pharmacy)}",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}