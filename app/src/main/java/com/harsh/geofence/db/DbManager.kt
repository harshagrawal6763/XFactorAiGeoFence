package com.harsh.geofence.db

import android.content.Context
import androidx.room.Room
import com.harsh.geofence.db.dao.EventDataDao


/*this object is used to initialize and
    access database object through any repository call*/

object DbManager {

    private lateinit var applicationDatabase: ApplicationDatabase


    //the initialization of database
    fun initialize(context: Context) {
        applicationDatabase = Room
            .databaseBuilder(context, ApplicationDatabase::class.java, Database.NAME)
            .allowMainThreadQueries()
            .build()
    }

    //closing instance of database
    fun close(){
        applicationDatabase.close()
    }

    //getting the EventDataDao using the instance of applicationDatabase
    fun getEventDataDao(): EventDataDao {
        return applicationDatabase.getEventDataDao()
    }
}