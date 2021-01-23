package com.halilibo.eczane.ui.main.locationlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.eczane.ui.common.statefulViewModel
import com.halilibo.shared.model.City
import dev.chrisbanes.accompanist.insets.AmbientWindowInsets
import dev.chrisbanes.accompanist.insets.toPaddingValues

@Composable
fun LocationListPage(
    onCityClick: (City) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (_, locationListState) = statefulViewModel<LocationListViewModel, LocationListState>()
    val cityList = locationListState.cityList

    LazyColumn(
        modifier = modifier,
        contentPadding = AmbientWindowInsets.current.navigationBars.toPaddingValues()
    ) {
        item {
            Row {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft.copy(
                            defaultWidth = 48.dp,
                            defaultHeight = 48.dp
                        ),
                    )
                }
            }
        }
        item {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = com.halilibo.eczane.R.string.cities),
                    style = MaterialTheme.typography.h3
                )
            }
        }
        itemsIndexed(cityList) { index, city ->
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable {
                        onCityClick(city)
                    }.padding(8.dp)
                ) {
                    Text(
                        "${if (city.id < 10) "0" else ""}${city.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        city.name,
                        fontSize = 20.sp
                    )
                }

                if (index != cityList.size - 1) {
                    Divider()
                }
            }
        }
    }
}
