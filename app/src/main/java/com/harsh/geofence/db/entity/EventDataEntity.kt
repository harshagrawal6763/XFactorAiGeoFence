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

    @ColumnInfo(name = "time")
    var time:Long?=null

    @ColumnInfo(name = "latitude")
    var lat:Double?=null

    @ColumnInfo(name = "longitude")
    var longitude:Double?=null

    @ColumnInfo(name = "eventName")
    var eventName:String?=null

    @ColumnInfo(name = "eventTypeId")
    var eventTypeId:Int?=null
}

