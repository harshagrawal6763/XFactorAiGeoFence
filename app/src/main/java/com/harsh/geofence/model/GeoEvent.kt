package com.harsh.geofence.model

//model class to communicate between service and viewmodel
//As this class is not used to send data to API/DB

data class GeoEvent(
    var eventType:Int,
    var latitude : Double?,
    var long: Double?,
    var time:Long
)