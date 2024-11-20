package com.harsh.geofence.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harsh.geofence.db.entity.EventDataEntity



@Dao
interface EventDataDao {

    //insert single Event
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEventData(eventDataEntity: EventDataEntity): Long

    //insert multiple Events
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEventData(eventDataEntity: ArrayList<EventDataEntity>)

    //get multiple Events
    @Query("select * from event_data")
    fun getEventData(): List<EventDataEntity>

    //delete Events
    @Query("DELETE FROM event_data")
    fun deleteEventData()

    //delete single event
    @Delete
    fun deleteEventData(eventDataEntity: EventDataEntity)
}