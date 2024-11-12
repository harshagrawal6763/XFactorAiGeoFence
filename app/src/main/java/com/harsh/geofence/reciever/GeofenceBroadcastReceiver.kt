package com.harsh.geofence.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.google.android.gms.location.GeofencingEvent
import com.harsh.geofence.GeoFenceApplication
import com.harsh.geofence.model.GeoEvent
import com.harsh.geofence.viewmodel.GeoFenceViewModel


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private var geoFenceViewModel: GeoFenceViewModel? = null
    override fun onReceive(context: Context, intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geoFenceViewModel == null) {
            geoFenceViewModel =
                (context.applicationContext as? GeoFenceApplication)?.geoFenceViewModel
        }

        if (geofencingEvent?.hasError() == true) {
            geoFenceViewModel?.sendError("Error receiving geofence event...")
            return
        }

        val geofenceList = geofencingEvent?.triggeringGeofences

        val location = geofencingEvent?.triggeringLocation
        val transitionType = geofencingEvent?.geofenceTransition

        transitionType?.let {
            geoFenceViewModel?.triggerEvent(
                GeoEvent(
                    it,
                    location?.latitude ?: geofenceList?.get(0)?.latitude,
                    location?.longitude ?: geofenceList?.get(0)?.longitude,
                    System.currentTimeMillis()
                )
            )
        }


    }

    companion object {
        private const val TAG = "GeoFenceBroadcastReceiver"
    }
}