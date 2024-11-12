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
fun getUniqueKeyInt(): Int {
    var udd = System.currentTimeMillis().toString()
    udd = Random().nextInt().toString() + udd
    return udd.substring(0, 5).toInt()
}

fun Fragment.hasPermissions(permissions: Array<String>): Boolean {
    var granted = true
    for (permission in permissions) {
        if (activity?.let { ActivityCompat.checkSelfPermission(it, permission) } != PackageManager.PERMISSION_GRANTED) {
            granted = false
        }
    }

    return granted
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    return format.format(date)
}
