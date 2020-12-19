package com.halilibo.shared.repository

import co.touchlab.kermit.Logger
import com.halilibo.eczane.db.OnCallPharmacyDatabase

expect fun createDb() : OnCallPharmacyDatabase

expect fun getLogger(): Logger