package com.harsh.geofence.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harsh.geofence.db.entity.EventDataEntity



@Dao
interface EventDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEventData(eventDataEntity: EventDataEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEventData(eventDataEntity: ArrayList<EventDataEntity>)

    @Query("select * from event_data")
    fun getEventData(): List<EventDataEntity>

    @Query("DELETE FROM event_data")
    fun deleteEventData()
}