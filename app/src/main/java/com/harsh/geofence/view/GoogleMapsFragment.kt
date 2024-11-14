package com.harsh.geofence.view

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.IntentSender
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.harsh.geofence.GeoFenceApplication

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


        observeGeoFenceViewModel()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        initGeoFenceData()
    }

    private fun initGeoFenceData() {
        geofencingClient = activity?.let { LocationServices.getGeofencingClient(it) }
        this.context?.let {
            geofenceHelper = GeofenceHelper(it)
        }
    }


    private fun observeGeoFenceViewModel() {
        geoFenceViewModel?.geoEvents?.observe(this.viewLifecycleOwner) {
            Toast.makeText(this.context, it?.eventName, Toast.LENGTH_SHORT).show()
        }

        geoFenceViewModel?.error?.observe(this.viewLifecycleOwner) {
            Toast.makeText(this.context, it, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val latLng = latLng
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        mMap?.setOnMapLongClickListener(this)
        checkLocationPermission()
    }

    @SuppressLint("MissingPermission")
    //added this as the check for permission is already added
    private fun checkLocationPermission() {
        if (!hasPermissions(permissionsAll)) {
            requestMultiplePermissionLauncher?.launch(permissions)
            return
        }else{
            checkIfGpsEnabledAndStartService()
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        if (!hasPermissions(permissionsAll)){
            requestMultiplePermissionLauncher?.launch(permissions)
            return
        }else{
            if (mMap?.isMyLocationEnabled == false){
                checkLocationPermission()
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

    private fun checkIfGpsEnabledAndStartService() {
        val locationRequest = geoFenceViewModel?.getLocationRequest()

        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }
        val settingsClient: SettingsClient? = activity?.let { LocationServices.getSettingsClient(it) }
        val task: Task<LocationSettingsResponse>? = builder?.build()
            ?.let { settingsClient?.checkLocationSettings(it) }

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
        mMap?.isMyLocationEnabled =  true
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
            geoFenceViewModel?.sendError("Location was not enabled")
        }
    }


    var geoFence:Geofence?=null

    @SuppressLint("MissingPermission")
    private fun addGeofence(latLng: LatLng, radius: Float) {
        geoFence = geofenceHelper?.getGeofence(
            GEOFENCE_ID,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest: GeofencingRequest? = geofenceHelper?.getGeofencingRequest(geoFence)
        val pendingIntent: PendingIntent? = geofenceHelper?.geofencePendingIntent

        if (geofencingRequest != null) {
            if (pendingIntent != null) {
                geofencingClient?.addGeofences(geofencingRequest, pendingIntent)
                    ?.addOnSuccessListener {

                    }
                    ?.addOnFailureListener { e ->
                        val errorMessage: String? = geofenceHelper?.getErrorString(e)
                        if (errorMessage != null) {
                            geoFenceViewModel?.sendError(errorMessage)
                        }
                    }
            }
        }
    }

    private fun addMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions().position(latLng)
        mMap?.addMarker(markerOptions)
    }

    private fun addCircle(latLng: LatLng, radius: Float) {
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
        requestMultiplePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var allPermissionGranted = true
            permissions.forEach {
                if (!it.value) {
                    allPermissionGranted = false
                }
            }
            if (allPermissionGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestMultiplePermissionLauncherBefore?.launch(arrayOf(ACCESS_BACKGROUND_LOCATION))
                }else{
                    checkIfGpsEnabledAndStartService()
                }
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(
                    context,
                    getString(R.string.txt_permission_denied_title),
                    Toast.LENGTH_LONG
                ).show()

            }
        }

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
                checkIfGpsEnabledAndStartService()
            }
        }
    }

    override fun onDestroy() {
        mMap?.clear()
        geofenceHelper?.geofencePendingIntent?.let { geofencingClient?.removeGeofences(it) }
        geofenceHelper = null
        geoFence = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "LandingFragment"


    }
}
