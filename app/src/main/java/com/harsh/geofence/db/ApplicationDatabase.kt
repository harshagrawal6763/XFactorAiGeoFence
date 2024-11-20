package com.harsh.geofence.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.harsh.geofence.db.dao.EventDataDao
import com.harsh.geofence.db.entity.EventDataEntity

//version 1 added initially, later on migrations can be added here
@Database(entities = [EventDataEntity::class], version = 1)
abstract class ApplicationDatabase :RoomDatabase(){
    abstract fun getEventDataDao(): EventDataDao
}
