package com.harsh.geofence.model

data class GeoEvent(
    var eventType:Int,
    var latitude : Double?,
    var long: Double?,
    var time:Long
)