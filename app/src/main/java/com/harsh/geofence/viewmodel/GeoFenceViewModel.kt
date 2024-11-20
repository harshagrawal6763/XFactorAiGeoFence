package com.harsh.geofence.viewmodel


import android.location.Location
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.harsh.geofence.consts.GeoConsts
import com.harsh.geofence.db.entity.EventDataEntity
import com.harsh.geofence.model.GeoEvent
import com.harsh.geofence.utils.convertLongToTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


open class GeoFenceViewModel : ViewModel() {
    var geoEvents: MutableLiveData<EventDataEntity> = MutableLiveData()
    var error: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<Location> = MutableLiveData()
    val analytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }
    var eventRepository: EventDataRepository = EventDataRepository(EventLocalDataSource())

    fun sendError(errorMessage: String) {
        //if the geofence event is unable to load location due to any reason
        //error is displayed
        viewModelScope.launch {
            withContext(Dispatchers.Main){
                error.value = errorMessage
            }

        }
    }

    /*save the event that is triggered by the broadcast reciever
    * A new entity as per the requirement is created from GeoEvent
    * Based on the eventType, the event name is given
    * the data is saved to database or API call is done based on the scenario/connectivity
    * The UI is updated about the event and geoEvents.value is updated that can be observed by Fragment/Activity
    * The event data is also sent to Firebase Analytics for logging
    * */
    fun triggerEvent(geoEvent: GeoEvent) {
        val newEvent = EventDataEntity()
        newEvent.eventTypeId = geoEvent.eventType
        newEvent.lat = geoEvent.latitude
        newEvent.longitude = geoEvent.long
        newEvent.time = geoEvent.time
        newEvent.eventName = when (geoEvent.eventType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                "GeoFence Entered"
            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                "GeoFence Stayed In Area"
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                "GeoFence Exited"
            }

            else -> {
                ""
            }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO){
                //the data is saved to database or API call is done based on the scenario/connectivity
                eventRepository.updateEventData(newEvent)

                withContext(Dispatchers.Main){
                    //The UI is updated about the event and
                    // geoEvents.value is updated that can be observed by Fragment/Activity
                    geoEvents.value = newEvent
                }

                //The event data is sent to Firebase Analytics for logging
                logFirebaseEvent(newEvent)
            }
        }
    }

    private fun logFirebaseEvent(newEvent:EventDataEntity) {
        val bundle = Bundle()
        newEvent.time?.let {
            //get the millis and convert it to a date
            val time = convertLongToTime(it)
            //put the date string into the bundle
            bundle.putString("event_time",time)
        }
        //put the event name into  bundle
        bundle.putString("event_name",newEvent.eventName)

        newEvent.eventTypeId?.let {
            //put the event type into bundle
            bundle.putInt("event_type", it)
        }

        //log the event to firebase
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT,bundle)
    }



    fun updateLocation(location: Location?,addedFence:Boolean) {
        //the event location is sent by LocationService to display/update to the UI
        viewModelScope.launch {
            withContext(Dispatchers.Main){
                this@GeoFenceViewModel.location.value = location
            }

            if (!addedFence && location?.longitude!=null){
                GeoConsts.GEOFENCE_RADIUS.toDouble().let {
                    val isInNBounds = isWithinRadius(location.latitude,location.longitude,GeoConsts.defaultLat,GeoConsts.defaultLong,
                        it
                    )
                    val newEvent = EventDataEntity()
                    newEvent.lat = location.latitude
                    newEvent.longitude = location.longitude
                    newEvent.time = System.currentTimeMillis()
                    //we can either add an enter event or an exit event.
                    if(isInNBounds){
                        //if it is in bounds we can add location event as in
                        //this would increase the no of events in database or api call, so need to set a threshold also
                        newEvent.eventTypeId = Geofence.GEOFENCE_TRANSITION_ENTER
                        newEvent.eventName = "GeoFence Entered"


                    }else{
                        newEvent.eventTypeId = Geofence.GEOFENCE_TRANSITION_EXIT
                        newEvent.eventName = "GeoFence Exited"

                    }
                    withContext(Dispatchers.IO){
                        eventRepository.updateEventData(newEvent)
                    }
                    withContext(Dispatchers.Main){
                        geoEvents.value = newEvent
                    }
                }
            }
        }

    }



    private fun isWithinRadius(
        targetLat: Double,
        targetLon: Double,
        fixedLat: Double,
        fixedLon: Double,
        radiusInMeters: Double
    ): Boolean {
        val earthRadius = 6371000.0 // Earth's radius in meters

        val dLat = Math.toRadians(targetLat - fixedLat)
        val dLon = Math.toRadians(targetLon - fixedLon)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(fixedLat)) * cos(Math.toRadians(targetLat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = earthRadius * c
        return distance <= radiusInMeters
    }


    fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Set the priority
            10000L // Interval in milliseconds
        ).apply {
            setMinUpdateIntervalMillis(5000L) // Fastest interval
        }.build()
        return locationRequest

    }
}

