package com.harsh.geofence

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.harsh.geofence.db.DbManager
import com.harsh.geofence.viewmodel.GeoFenceViewModel

open class GeoFenceApplication:Application() {
    var geoFenceViewModel : GeoFenceViewModel?=null
    override fun onCreate() {
        super.onCreate()
        geoFenceViewModel = GeoFenceViewModel()
        DbManager.initialize(this)
        createNotificationChannel()
    }


    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "Location Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}