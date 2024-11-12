package com.harsh.geofence.view

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
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

import com.harsh.geofence.consts.AppConsts.latLng
import com.harsh.geofence.reciever.GeofenceHelper
import com.harsh.geofence.utils.hasPermissions
import com.harsh.geofence.utils.permissions
import com.harsh.geofence.utils.permissionsAll
import com.harsh.geofence.viewmodel.GeoFenceViewModel


class LandingFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private var mMap: GoogleMap? = null
    private var geofencingClient: GeofencingClient? = null
    private lateinit var geofenceHelper: GeofenceHelper

    private var geoFenceViewModel: GeoFenceViewModel? = null
    private var requestMultiplePermissionLauncher: ActivityResultLauncher<Array<String>?>? = null
    private var requestMultiplePermissionLauncherBefore: ActivityResultLauncher<Array<String>?>? = null



    init {
        initMultiplePermissionRequest()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()

        observeGeoFenceViewModel()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        geofencingClient = activity?.let { LocationServices.getGeofencingClient(it) }
        this.context?.let {
            geofenceHelper = GeofenceHelper(it)
        }
    }

    private fun initViewModel() {
        geoFenceViewModel = (activity?.application as GeoFenceApplication).geoFenceViewModel
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



    @SuppressLint("MissingPermission")
    private fun addGeofence(latLng: LatLng, radius: Float) {
        val geofence: Geofence = geofenceHelper.getGeofence(
            GEOFENCE_ID,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest: GeofencingRequest = geofenceHelper.getGeofencingRequest(geofence)
        val pendingIntent: PendingIntent = geofenceHelper.geofencePendingIntent

        geofencingClient?.addGeofences(geofencingRequest, pendingIntent)
            ?.addOnSuccessListener {

            }
            ?.addOnFailureListener { e ->
                val errorMessage: String = geofenceHelper.getErrorString(e)
                geoFenceViewModel?.sendError(errorMessage)
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


    companion object {
        private const val TAG = "LandingFragment"
        const val GEOFENCE_RADIUS = 200f
        const val GEOFENCE_ID = "MyGeofenceService"

    }
}
