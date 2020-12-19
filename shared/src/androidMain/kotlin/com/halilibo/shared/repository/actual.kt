package com.halilibo.shared.repository

import android.content.Context
import co.touchlab.kermit.LogcatLogger
import co.touchlab.kermit.Logger
import com.halilibo.eczane.db.OnCallPharmacyDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver

lateinit var appContext: Context

actual fun createDb(): OnCallPharmacyDatabase {
    val driver = AndroidSqliteDriver(OnCallPharmacyDatabase.Schema, appContext, "pharmacy.db")
    return OnCallPharmacyDatabase(driver)
}

actual fun getLogger(): Logger = LogcatLogger()