package com.harsh.geofence.db

import android.content.Context
import androidx.room.Room
import com.harsh.geofence.db.dao.EventDataDao



object DbManager {


    private lateinit var applicationDatabase: ApplicationDatabase
    fun initialize(context: Context) {
        applicationDatabase = Room
            .databaseBuilder(context, ApplicationDatabase::class.java, Database.NAME)
            .allowMainThreadQueries()
            .build()
    }

    fun getEventDataDao(): EventDataDao {
        return applicationDatabase.getEventDataDao()
    }
}