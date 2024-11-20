package com.harsh.geofence.view

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
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
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.harsh.geofence.R
import com.harsh.geofence.service.LocationService
import com.harsh.geofence.utils.hasPermissions
import com.harsh.geofence.utils.permissions
import com.harsh.geofence.utils.permissionsAll
import com.harsh.geofence.viewmodel.GeoFenceViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ServiceLocationFragment : Fragment() {

    private var btnLocation : AppCompatButton?=null
    private var txtLatitude : AppCompatTextView?=null
    private var txtLongitude : AppCompatTextView?=null

    private var openedSettings : Boolean = false
    private val geoFenceViewModel: GeoFenceViewModel by sharedViewModel()
    private var requestMultiplePermissionLauncher: ActivityResultLauncher<Array<String>?>? = null
    private var requestMultiplePermissionLauncherBefore: ActivityResultLauncher<Array<String>?>? = null

    init {
        initMultiplePermissionRequest()
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_service_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //observe the view model
        observeGeoFenceViewModel()

        //init all the views
        btnLocation = view.findViewById(R.id.btnLocation)
        txtLatitude = view.findViewById(R.id.txtLatitude)
        txtLongitude = view.findViewById(R.id.txtLongitude)

        //set click listener for starting the flow
        btnLocation?.setOnClickListener {
            checkPermission()
        }
    }


    private fun observeGeoFenceViewModel() {
        geoFenceViewModel.geoEvents.observe(this.viewLifecycleOwner) {
            Toast.makeText(this.context, it?.eventName, Toast.LENGTH_SHORT).show()
        }

        geoFenceViewModel.error.observe(this.viewLifecycleOwner) {
            Toast.makeText(this.context, it, Toast.LENGTH_SHORT).show()
        }

        geoFenceViewModel.location.observe(this.viewLifecycleOwner) {
            txtLatitude?.text = "Latitude :"+it?.latitude
            txtLongitude?.text = "Longitude :"+it?.longitude
        }
    }


    /*this function checks if user has given all permissions
    if already granted it will check for GPS
    * */
    private fun checkPermission(openedOnce:Boolean=false) {
        //here permissions all refers to all permissions
        //if the permissions are not granted, first we will
        //demand for only permissions that is
        // ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION,POST_NOTIFICATIONS

        if (!hasPermissions(permissionsAll)) {
            if (!openedOnce) {
                requestMultiplePermissionLauncher?.launch(permissions)
            }
            return
        }else{
            //check for GPS
            checkIfGpsEnabledAndStartService()
        }
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
        val task: Task<LocationSettingsResponse>? = builder.build().let { settingsClient?.checkLocationSettings(it) }

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


    private fun startLocationService() {
        //clear the last lat long
        clearText()
        //start the service
        val serviceIntent = Intent(this.context, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(serviceIntent)
        }

        //show message to user
        Toast.makeText(this.context, "Service Started", Toast.LENGTH_SHORT).show()
    }

    private fun clearText() {
        txtLatitude?.text = ""
        txtLongitude?.text = ""
    }

    private fun initMultiplePermissionRequest() {
        /*this function checks if user has given permissions
            if already granted it will check for ACCESS_BACKGROUND_LOCATION permission check
         * */
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
                //if already granted it will check for ACCESS_BACKGROUND_LOCATION permission check
                //not required below 29
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
    //we need to take user to app settings
    private fun handlePermissionDenied() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialog.dismiss()
                        openedSettings = true
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", this@ServiceLocationFragment.context?.packageName, null)
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
        //remove observers
        geoFenceViewModel.error.removeObservers(viewLifecycleOwner)
        geoFenceViewModel.geoEvents.removeObservers(viewLifecycleOwner)
        geoFenceViewModel.location.removeObservers(viewLifecycleOwner)
        super.onDestroy()
    }
    override fun onStop() {
        //on stop
        //remove the service
        super.onStop()
        val serviceIntent = Intent(this.context, LocationService::class.java)
        context?.stopService(serviceIntent)
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