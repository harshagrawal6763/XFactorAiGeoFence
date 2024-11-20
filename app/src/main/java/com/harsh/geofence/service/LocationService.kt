package com.harsh.geofence.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.harsh.geofence.GeoFenceApplication
import com.harsh.geofence.reciever.GeofenceHelper
import com.harsh.geofence.R
import com.harsh.geofence.consts.GeoConsts
import com.harsh.geofence.consts.GeoConsts.GEOFENCE_ID
import com.harsh.geofence.consts.GeoConsts.GEOFENCE_RADIUS

import com.harsh.geofence.viewmodel.GeoFenceViewModel

/*this Location Service is used to keep the location updates enabled in background
* as the geofence api requires some kind of location updates, if we use no UI approach
* by only using button, it is needed that a service constantly checks for the location updates and
* handles the event broadcast when the geofence api triggers through GeofenceBroadcastReceiver
* */
class LocationService : Service() {

    //the location client responsible for location updates
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //the callback where the location updates are triggered
    //please note, this will be triggered even when the user moves,
    // not necessarily in or out of the fence
    private lateinit var locationCallback: LocationCallback

    //the viewmodel instance to handle error or location trigger events
    var viewModel: GeoFenceViewModel?=null

    //the helper used for generating geofencing request
    var geofenceHelper : GeofenceHelper?=null

    //the geofencing client that will add the geofence
    private lateinit var geofencingClient: GeofencingClient

    /*gets the GeofencingRequest and requires instance of Geofence
   * adds the initial trigger as INITIAL_TRIGGER_ENTER
   * and using builder patterns, builds the request and gives an instance of
   * GeofencingRequest
   * */
    private val geofenceRequest: GeofencingRequest by lazy {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(GeoConsts.defaultLat, GeoConsts.defaultLong, GEOFENCE_RADIUS)  // Example LatLng and radius of 100 meters
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        //init the geofence helper
        geofenceHelper = GeofenceHelper(this)

        //init the location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //init the geofence client
        geofencingClient = LocationServices.getGeofencingClient(this)

        //init the view model instance if not instantiated
        if (viewModel == null){
            viewModel = (this.applicationContext.applicationContext as? GeoFenceApplication)?.geoFenceViewModel
        }

        // Create a LocationRequest for periodic location updates
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update every 10 seconds
            fastestInterval = 5000 // Fastest interval for location updates
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Create a LocationCallback to handle location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.let {
                    val location = it.lastLocation
                    viewModel?.updateLocation(location)
                    // Use the location data (for example, log or send to a server)
                }
            }
        }



        // Start receiving location updates
        startLocationUpdates(locationRequest)
    }

    @SuppressLint("MissingPermission")
    //the suppress lint is added as the permission
    // check will be done by fragment and then the service could run
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    @SuppressLint("MissingPermission")
    private fun registerGeofence() {
        // Register the geofence with GeofencingClient
        //the suppress lint is added as the permission
        // check will be done by fragment and then the service could run
        geofenceHelper?.geofencePendingIntent?.let {
            geofencingClient.addGeofences(geofenceRequest, it).run {
                addOnSuccessListener {
                    Toast.makeText(applicationContext, "Geofence Added", Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener {
                    Toast.makeText(applicationContext, "Failed to add Geofence", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Register for geofence events
        registerGeofence()

        // Create a notification for the foreground service
        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Location Service")
            .setContentText("Tracking your location in the background")
            .setSmallIcon(R.drawable.ic_location)
            .build()

        // Start the service in the foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "Location Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        startForeground(System.currentTimeMillis().toInt(), notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        //no need for binder here
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        //once service is about to destroy

        //remove the location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        //remove the geofence
        geofenceHelper?.geofencePendingIntent?.let { geofencingClient.removeGeofences(it) }

        //set the geofenceHelper as null
        geofenceHelper = null

        //remove the service and dismiss notification
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
