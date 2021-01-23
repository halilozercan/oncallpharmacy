package com.halilibo.eczane.util

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn


// Send location updates to the consumer
// Taken from https://gist.github.com/manuelvicnt/558118684eb38af8a27c22f2e5291058
@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationFlow() = callbackFlow<Location> {
    // A new Flow is created. This code executes in a coroutine!

    // 1. Create callback and add elements into the flow
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result ?: return // Ignore null responses
            for (location in result.locations) {
                try {
                    offer(location) // Send location to the flow
                } catch (t: Throwable) {
                    // Location couldn't be sent to the flow
                }
            }
        }
    }

    // 2. Register the callback to get location updates by calling requestLocationUpdates
    try {
        requestLocationUpdates(
            LocationRequest.create(),
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e) // in case of error, close the Flow
        }
    } catch (e: Exception) {
        println("$e")
    }

    // 3. Wait for the consumer to cancel the coroutine and unregister
    // the callback. This suspends the coroutine until the Flow is closed.
    awaitClose {
        // Clean up code goes here
        removeLocationUpdates(callback)
    }
}.shareIn(
    // Make the flow follow the applicationScope
    ProcessLifecycleOwner.get().lifecycleScope,
    // Emit the last emitted element to new collectors
    replay = 1,
    // Keep the producer active while there are active subscribers
    started = SharingStarted.WhileSubscribed()
)

