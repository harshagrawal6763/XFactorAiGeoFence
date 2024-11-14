package com.harsh.geofence.consts

import com.google.android.gms.maps.model.LatLng

object GeoConsts {
    const val defaultLat = 13.0122863 //replace the default latitude here
    const val defaultLong = 77.5562248 //replace the default longitude here
    val latLng = LatLng(defaultLat, defaultLong )
    const val GEOFENCE_RADIUS = 200f //in meters
    const val GEOFENCE_ID = "MyGeofenceService"
}