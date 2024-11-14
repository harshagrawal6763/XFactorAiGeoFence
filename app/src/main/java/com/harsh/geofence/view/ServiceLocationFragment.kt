package com.harsh.geofence.view

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
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
import com.harsh.geofence.GeoFenceApplication
import com.harsh.geofence.R
import com.harsh.geofence.service.LocationService
import com.harsh.geofence.utils.hasPermissions
import com.harsh.geofence.utils.permissions
import com.harsh.geofence.utils.permissionsAll
import com.harsh.geofence.viewmodel.GeoFenceViewModel


class ServiceLocationFragment : Fragment() {

    private var btnLocation : AppCompatButton?=null
    private var txtLatitude : AppCompatTextView?=null
    private var txtLongitude : AppCompatTextView?=null

    private var geoFenceViewModel: GeoFenceViewModel? = null
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
            geoFenceViewModel?.sendError("Location was not enabled")
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
        initViewModel()
        observeGeoFenceViewModel()
        btnLocation = view.findViewById(R.id.btnLocation)
        txtLatitude = view.findViewById(R.id.txtLatitude)
        txtLongitude = view.findViewById(R.id.txtLongitude)
        btnLocation?.setOnClickListener {
            checkLocationPermission()
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

        geoFenceViewModel?.location?.observe(this.viewLifecycleOwner) {
            txtLatitude?.text = "Latitude :"+it?.latitude
            txtLongitude?.text = "Longitude :"+it?.longitude
        }
    }

    private fun checkLocationPermission() {
        if (!hasPermissions(permissionsAll)) {
            requestMultiplePermissionLauncher?.launch(permissions)
            return
        }else{
            checkIfGpsEnabledAndStartService()
        }
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




    private fun startLocationService() {
        clearText()

        val serviceIntent = Intent(this.context, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(serviceIntent)
        }
        Toast.makeText(this.context, "Service Started", Toast.LENGTH_SHORT).show()
    }

    private fun clearText() {
        txtLatitude?.text = ""
        txtLongitude?.text = ""
    }

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
                                    Toast.makeText(this.context,"Permission for location as Allow all the time is needed for GeoFencing",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    val builder: AlertDialog.Builder? = context?.let { AlertDialog.Builder(it) }
                    builder?.setMessage(getString(R.string.please_grant_permission))
                        ?.setPositiveButton(getString(R.string.common_ok), dialogClickListener)
                        ?.setNegativeButton(getString(R.string.common_cancel), dialogClickListener)?.show()
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

    override fun onStop() {
        super.onStop()
        val serviceIntent = Intent(this.context, LocationService::class.java)
        context?.stopService(serviceIntent)
    }
}