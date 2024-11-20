package com.harsh.geofence.consts

import com.google.android.gms.maps.model.LatLng

object GeoConsts {
    //the default latitude and longitude for which the fence needs to setup.This also can be loaded from server/API
    const val defaultLat = 13.0122863 //replace the default latitude here
    const val defaultLong = 77.5562248 //replace the default longitude here
    val latLng = LatLng(defaultLat, defaultLong )

    //the default radius that we need to check user's activity
    const val GEOFENCE_RADIUS = 200f //in meters

    //the id for running the Location Service
    const val GEOFENCE_ID = "MyGeofenceService"
}