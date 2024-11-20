package com.harsh.geofence.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.harsh.geofence.utils.getUniqueKeyInt

@Entity("event_data")
class EventDataEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    var autogenId : Long = getUniqueKeyInt().toLong()

    //the timestamp used for showing/calculating time spent
    @ColumnInfo(name = "time")
    var time:Long?=null

    //the latitude of event trigger
    @ColumnInfo(name = "latitude")
    var lat:Double?=null

    //the longitude of event trigger
    @ColumnInfo(name = "longitude")
    var longitude:Double?=null

    //the event name
    @ColumnInfo(name = "eventName")
    var eventName:String?=null

    //the event type
    @ColumnInfo(name = "eventTypeId")
    var eventTypeId:Int?=null
}

