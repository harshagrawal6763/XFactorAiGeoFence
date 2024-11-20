package com.harsh.geofence.view

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

import com.harsh.geofence.R
import com.harsh.geofence.consts.GeoConsts.GEOFENCE_ID
import com.harsh.geofence.consts.GeoConsts.GEOFENCE_RADIUS

import com.harsh.geofence.consts.GeoConsts.latLng
import com.harsh.geofence.reciever.GeofenceHelper
import com.harsh.geofence.utils.hasPermissions
import com.harsh.geofence.utils.permissions
import com.harsh.geofence.utils.permissionsAll
import com.harsh.geofence.viewmodel.GeoFenceViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class GoogleMapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private var mMap: GoogleMap? = null
    private var geofencingClient: GeofencingClient? = null
    private var geofenceHelper: GeofenceHelper?=null
    var openedSettings : Boolean = false

    private val geoFenceViewModel: GeoFenceViewModel by sharedViewModel()
    private var requestMultiplePermissionLauncher: ActivityResultLauncher<Array<String>?>? = null
    private var requestMultiplePermissionLauncherBefore: ActivityResultLauncher<Array<String>?>? = null



    init {
        initMultiplePermissionRequest()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //observe view model
        observeGeoFenceViewModel()

        //set map to the view and load it
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        //initialize the required data for geo fence
        //this can be also done once permissions are accepted
        initGeoFenceData()
    }

    private fun initGeoFenceData() {
        geofencingClient = activity?.let { LocationServices.getGeofencingClient(it) }
        this.context?.let {
            geofenceHelper = GeofenceHelper(it)
        }
    }


    private fun observeGeoFenceViewModel() {
        geoFenceViewModel.geoEvents.observe(this.viewLifecycleOwner) {
            Toast.makeText(this.context, it?.eventName, Toast.LENGTH_SHORT).show()
        }

        geoFenceViewModel.error.observe(this.viewLifecycleOwner) {
            Toast.makeText(this.context, it, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        //when google map is initiated get the instance of it
        mMap = googleMap

        //move the camera to the default lat long
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

        //set long click listener on the map
        mMap?.setOnMapLongClickListener(this)

        //check for permissions
        checkPermission()
    }

    @SuppressLint("MissingPermission")
    //added this as the check for permission is already added
    private fun checkPermission(openedOnce:Boolean=false) {
        //here permissions all refers to all permissions
        //if the permissions are not granted, first we will
        //demand for only permissions that is
        // ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION,POST_NOTIFICATIONS

        if (!hasPermissions(permissionsAll)) {
            if (!openedOnce){
                requestMultiplePermissionLauncher?.launch(permissions)
            }
            return
        }else{
            //if permissions are already granted, check for gps
            checkIfGpsEnabledAndStartService()
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        if (!hasPermissions(permissionsAll)){
            requestMultiplePermissionLauncher?.launch(permissions)
            return
        }else{
            if (mMap?.isMyLocationEnabled == false){
                checkPermission()
            }else{
                handleMapLongClick(latLng)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun handleMapLongClick(latLng: LatLng) {
        //whenever the user long presses on map, it adds a marker to that point,
        //creates a boundary/circle across it
        //and add the same boundary as geofence
        mMap?.clear()
        addMarker(latLng)
        addCircle(latLng, GEOFENCE_RADIUS)
        addGeofence(latLng, GEOFENCE_RADIUS)
    }

    /*this function checks if the gps is enabled or not
   * if the gps is enabled, it starts the service and if not
   * it asks the users to enable the GPS
   * */
    private fun checkIfGpsEnabledAndStartService() {
        val locationRequest = geoFenceViewModel.getLocationRequest()

        val builder = locationRequest.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }
        val settingsClient: SettingsClient? = activity?.let { LocationServices.getSettingsClient(it) }
        val task: Task<LocationSettingsResponse>? = builder.build()
            .let { settingsClient?.checkLocationSettings(it) }

        task?.addOnSuccessListener {
            // Location settings are already enabled
            // Start your location-related work here
            startLocationService()
        }?.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog to prompt the user to enable location
                    locationSettingsLauncher.launch(IntentSenderRequest.Builder(exception.resolution).build())
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationService() {
        //enable the current location icon on google maps
        mMap?.isMyLocationEnabled =  true

        //also notify the user about map long press
        Toast.makeText(context,"Please long press on map to enable gps around that area.",Toast.LENGTH_SHORT).show()
    }

    // Result handling for the location dialog
    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Location services are enabled by the user
            startLocationService()
        } else {
            // User did not enable location services
            // Handle accordingly, e.g., show a message
            geoFenceViewModel.sendError("Location was not enabled")
        }
    }


    var geoFence:Geofence?=null

    @SuppressLint("MissingPermission")
    private fun addGeofence(latLng: LatLng, radius: Float) {
        //getting the geofence
        geoFence = geofenceHelper?.getGeofence(
            GEOFENCE_ID,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT
        )

        //getting the geofence request
        val geofencingRequest: GeofencingRequest? = geofenceHelper?.getGeofencingRequest(geoFence)

        //get the geofencePendingIntent which will trigger the GeofenceBroadcastReceiver
        val pendingIntent: PendingIntent? = geofenceHelper?.geofencePendingIntent

        if (geofencingRequest != null) {
            if (pendingIntent != null) {
                //add the geofence with help of geofencingClient
                geofencingClient?.addGeofences(geofencingRequest, pendingIntent)
                    ?.addOnSuccessListener {
                        Toast.makeText(context,"Geo Fence Added",Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener { e ->
                        val errorMessage: String? = geofenceHelper?.getErrorString(e)
                        if (errorMessage != null) {
                            geoFenceViewModel.sendError(errorMessage)
                        }
                    }
            }
        }
    }

    private fun addMarker(latLng: LatLng) {
        //add the marker to the map
        val markerOptions = MarkerOptions().position(latLng)
        mMap?.addMarker(markerOptions)
    }

    private fun addCircle(latLng: LatLng, radius: Float) {
        //add a circle on the map with the radius specified
        //this will draw a circle around the latLng to help user see if s(he) is entering the area
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
        circleOptions.fillColor(Color.argb(64, 255, 0, 0))
        circleOptions.strokeWidth(4f)
        mMap?.addCircle(circleOptions)
    }



    @SuppressLint("MissingPermission")
    private fun initMultiplePermissionRequest() {
        /*this function checks if user has given permissions
            if already granted it will check for ACCESS_BACKGROUND_LOCATION permission check
         * */
        requestMultiplePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            //check for permissions
            var allPermissionGranted = true
            permissions.forEach {
                if (!it.value) {
                    allPermissionGranted = false
                }
            }

            if (allPermissionGranted) {
                //if already granted it will check for ACCESS_BACKGROUND_LOCATION permission check
                //it is not required for below 29
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val dialogClickListener =
                        DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    dialog.dismiss()
                                    requestMultiplePermissionLauncherBefore?.launch(arrayOf(
                                        ACCESS_BACKGROUND_LOCATION
                                    ))
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {
                                    dialog.dismiss()
                                    geoFenceViewModel.sendError("Permission for location as Allow all the time is needed for GeoFencing")
                                }
                            }
                        }

                    val builder: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
                    builder?.setMessage(getString(R.string.please_grant_permission))
                        ?.setPositiveButton(getString(R.string.common_ok), dialogClickListener)
                        ?.setNegativeButton(getString(R.string.common_cancel), dialogClickListener)?.show()
                }else{
                    //check for gps active or not
                    checkIfGpsEnabledAndStartService()
                }
            } else {
                //if the permissions are denied constantly, it will
                //default give false
                handlePermissionDenied()

            }
        }


        //this is required when requesting background location updates
        requestMultiplePermissionLauncherBefore = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var allPermissionGranted = true
            permissions.forEach {
                if (!it.value) {
                    allPermissionGranted = false
                }
            }
            if (allPermissionGranted) {
                //if the permissions are accepted, check for GPS
                checkIfGpsEnabledAndStartService()
            }else{
                //if the permissions are denied constantly, it will
                //default give false at that time take user to settings screen
                handlePermissionDenied()
            }
        }
    }


    //if the permissions are denied constantly, it will
    //default give false, to handle this
    //we need to take user to app's settings
    private fun handlePermissionDenied() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialog.dismiss()
                        openedSettings = true
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", this@GoogleMapsFragment.context?.packageName, null)
                        })
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                        geoFenceViewModel.sendError(getString(R.string.please_grant_permission_all))
                    }
                }
            }

        val builder: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
        builder?.setMessage(getString(R.string.please_grant_permission_all))
            ?.setPositiveButton(getString(R.string.common_ok), dialogClickListener)
            ?.setNegativeButton(getString(R.string.common_cancel), dialogClickListener)?.show()
    }


    override fun onDestroy() {
        //on destroy clear the map
        //also remove the geofence that was registered
        //remove helper and geoFence
        mMap?.clear()
        geofenceHelper?.geofencePendingIntent?.let { geofencingClient?.removeGeofences(it) }
        geofenceHelper = null
        geoFence = null

        super.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        if (openedSettings){
            //if the app settings was opened this will check the permissions once again and start the flow
            openedSettings =  false
            checkPermission(true)
        }
    }
}

