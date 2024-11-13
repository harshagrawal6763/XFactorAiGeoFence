package com.harsh.geofence.consts

import com.google.android.gms.maps.model.LatLng

object GeoConsts {
    const val defaultLat = 18.6177102 //replace the default latitude here
    const val defaultLong = 73.7524581 //replace the default longitude here
    val latLng = LatLng(defaultLat, defaultLong )
    const val GEOFENCE_RADIUS = 200f
    const val GEOFENCE_ID = "MyGeofenceService"
}