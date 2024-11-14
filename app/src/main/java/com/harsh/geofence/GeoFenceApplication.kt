package com.harsh.geofence

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.harsh.geofence.db.DbManager
import com.harsh.geofence.viewmodel.EventDataRepository
import com.harsh.geofence.viewmodel.EventLocalDataSource
import com.harsh.geofence.viewmodel.GeoFenceViewModel
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

open class GeoFenceApplication:Application() {



    val appModule = module {
        single { EventLocalDataSource() } // Singleton scope
        single { EventDataRepository(get()) } // Singleton scope
        single { GeoFenceViewModel() } // Singleton scope
    }

    val geoFenceViewModel : GeoFenceViewModel by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GeoFenceApplication)
            modules(appModule)
        }

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