package com.harsh.geofence.utils




import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random


//this is to check if user has already granted all of the permissions required
//if user has already granted, we can directly perform the next operation
 var permissionsAll = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         arrayOf(
             Manifest.permission.ACCESS_FINE_LOCATION,
             Manifest.permission.ACCESS_COARSE_LOCATION,
             Manifest.permission.ACCESS_BACKGROUND_LOCATION,
             POST_NOTIFICATIONS
         )
     } else {
         arrayOf(
             Manifest.permission.ACCESS_FINE_LOCATION,
             Manifest.permission.ACCESS_COARSE_LOCATION,
             Manifest.permission.ACCESS_BACKGROUND_LOCATION
         )
     }
 } else {
     arrayOf(
         Manifest.permission.ACCESS_FINE_LOCATION,
         Manifest.permission.ACCESS_COARSE_LOCATION
     )
 }


//this is to check if the user has granted location and notification permission
//if the user has granted all 3 of this
//we can move forward to check for ACCESS_BACKGROUND_LOCATION
var permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
} else {
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
}

//generates a unique int
fun getUniqueKeyInt(): Int {
    var udd = System.currentTimeMillis().toString()
    udd = Random().nextInt().toString() + udd
    return udd.substring(0, 5).toInt()
}


//extension func that checks if the user has granted all the permission sent in
//permissions as argument
//uses instance of any fragment to check the permission
fun Fragment.hasPermissions(permissions: Array<String>): Boolean {
    var granted = true
    for (permission in permissions) {
        if (activity?.let { ActivityCompat.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED) {
            granted = false
        }
    }

    return granted
}

//converts the date stored in Long to a particular format using device's local timezone
fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    return format.format(date)
}
