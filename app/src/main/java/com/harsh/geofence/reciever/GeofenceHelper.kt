package com.harsh.geofence.reciever

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng


open class GeofenceHelper(context: Context){
    /*gets the GeofencingRequest and requires instance of Geofence
    * adds the initial trigger as INITIAL_TRIGGER_ENTER
    * and using builder patterns, builds the request and gives an instance of
    * GeofencingRequest
    * */
    fun getGeofencingRequest(geofence: Geofence?): GeofencingRequest {
        return GeofencingRequest.Builder()
            .addGeofence(geofence!!)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()
    }

    /*builds the geofence instance required for getGeofencingRequest()
    * here we can add the latitude and longitude for the circular region
    * the transition types that can be sent from server, if we specifically want enter/exit/dwell events
    * Sets the delay between Geofence ENTER and Geofence DWELL in milliseconds.5 seconds set here
    * setExpirationDuration sets the expiration duration for this geofence instance.
    * build the instance
    * */

    fun getGeofence(id: String, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence {
        return Geofence.Builder()
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setRequestId(id)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(5000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }


    //get the PendingIntent required to call the geofence api
    //this registers the GeofenceBroadcastReceiver to recieve the event
    //when triggered after setting the geofence
    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    //gives the appropriate message required to show the user about failure while adding geofence
    fun getErrorString(e: Exception): String {
        if (e is ApiException) {
            when (e.statusCode) {
                GeofenceStatusCodes
                    .GEOFENCE_NOT_AVAILABLE -> return "GEOFENCE_NOT_AVAILABLE"

                GeofenceStatusCodes
                    .GEOFENCE_TOO_MANY_GEOFENCES -> return "GEOFENCE_TOO_MANY_GEOFENCE"

                GeofenceStatusCodes
                    .GEOFENCE_TOO_MANY_PENDING_INTENTS -> return "GEOFENCE_TOO_MANY_PENDING_INTENTS"
            }
        }
        return e.localizedMessage ?:""
    }

}