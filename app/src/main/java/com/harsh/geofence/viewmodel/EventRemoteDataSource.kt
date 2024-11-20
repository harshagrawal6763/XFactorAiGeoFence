package com.harsh.geofence.viewmodel


import com.harsh.geofence.db.entity.EventDataEntity

class EventRemoteDataSource: EventDataSource {
    override fun updateEventData(eventDataEntity: EventDataEntity): Long {
        //TODO
        //not written anything here
        //here we can build a network request that can be called and response/result of it
        //can be passed on to viewmodel
        return 0
    }

}
