package com.harsh.geofence.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.google.android.gms.location.GeofencingEvent
import com.harsh.geofence.GeoFenceApplication
import com.harsh.geofence.model.GeoEvent
import com.harsh.geofence.viewmodel.GeoFenceViewModel

/*GeofenceBroadcastReceiver
*
* GeofenceBroadcastReceiver will be used for receiving events when the user enters/exits or dwells into the geofence
* As we register the GeofenceBroadcastReceiver through geofencePendingIntent
* The geofence api triggers event and is recieved in onReceive()
*
* Then we need to check if there was error, if yes, then we would update the UI about it to trigger the error
*
* */

open class GeofenceBroadcastReceiver : BroadcastReceiver() {
    var geoFenceViewModel: GeoFenceViewModel? = null
    var geofencingEventWrapper = GeofencingEventWrapper()


    override fun onReceive(context: Context, intent: Intent) {

        //We need to pass the intent to GeofencingEvent using fromIntent() to extract the event information
        val geofencingEvent = geofencingEventWrapper.fromIntent(intent)


        //init the view model instance if not initialized yet
        if (geoFenceViewModel == null) {
            geoFenceViewModel =
                (context.applicationContext as? GeoFenceApplication)?.geoFenceViewModel
        }

        //check if the event had error,
        // if yes update the view model about error to be displayed on Screen
        //else continue with execution
        if (geofencingEvent?.hasError() == true) {
            geoFenceViewModel?.sendError("Error receiving geofence event...")
            return
        }

        //get the geofence list
        //Returns the list of geofences that triggered this  alert.
        val geofenceList = geofencingEvent?.triggeringGeofences

        //get the trigger location
        val location = geofencingEvent?.triggeringLocation

        //get the transition types out of below types
        /*
        GEOFENCE_TRANSITION_ENTER user enters the geofence.
        GEOFENCE_TRANSITION_EXIT user exits the geofence
        GEOFENCE_TRANSITION_DWELL user enters and dwells in geofences.
        */
        val transitionType = geofencingEvent?.geofenceTransition

        //check if the transtion type is not null, send the event to viewmodel for saving or for API call.
        //added time stamp of the event using System.currentTimeMillis()
        transitionType?.let {
            geoFenceViewModel?.triggerEvent(
                GeoEvent(
                    it,
                    location?.latitude,
                    location?.longitude,
                    System.currentTimeMillis()
                )
            )
        }

    }

}