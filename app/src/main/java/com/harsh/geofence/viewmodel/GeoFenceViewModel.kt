package com.harsh.geofence.viewmodel


import android.location.Location
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.harsh.geofence.db.entity.EventDataEntity
import com.harsh.geofence.model.GeoEvent
import com.harsh.geofence.utils.convertLongToTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class GeoFenceViewModel : ViewModel() {
    var geoEvents: MutableLiveData<EventDataEntity> = MutableLiveData()
    var error: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<Location> = MutableLiveData()
    val analytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }
    var eventRepository = EventDataRepository(EventLocalDataSource())
    fun sendError(errorMessage: String) {
        error.value = errorMessage
    }

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
                eventRepository.updateEventData(newEvent)

                withContext(Dispatchers.Main){
                    geoEvents.value = newEvent
                }
                logFirebaseEvent(newEvent)
            }
        }
    }

    private fun logFirebaseEvent(newEvent:EventDataEntity) {
        val bundle = Bundle()
        newEvent.time?.let {
            val time = convertLongToTime(it)
            bundle.putString("event_time",time)
        }
        bundle.putString("event_name",newEvent.eventName)

        newEvent.eventTypeId?.let {
            bundle.putInt("event_type", it)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT,bundle)
    }



    fun updateLocation(location: Location?) {
        this.location.value = location
    }

    fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // Set interval as needed
            fastestInterval = 5000
        }

    }
}

