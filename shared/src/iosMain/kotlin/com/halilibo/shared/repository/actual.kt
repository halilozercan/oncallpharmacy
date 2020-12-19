package com.halilibo.shared.repository

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NSLogLogger
import com.halilibo.eczane.db.OnCallPharmacyDatabase
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual fun createDb(): OnCallPharmacyDatabase {
    val driver = NativeSqliteDriver(OnCallPharmacyDatabase.Schema, "pharmacy.db")
    return OnCallPharmacyDatabase(driver)
}

actual fun getLogger(): Logger = NSLogLogger()