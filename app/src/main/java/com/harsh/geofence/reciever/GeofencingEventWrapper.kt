package com.harsh.geofence.reciever

import android.content.Intent
import com.google.android.gms.location.GeofencingEvent

open class GeofencingEventWrapper {
    open fun fromIntent(intent: Intent): GeofencingEvent? {
        return GeofencingEvent.fromIntent(intent)
    }
}
