package com.harsh.geofence.viewmodel

import com.harsh.geofence.db.entity.EventDataEntity

open class EventDataRepository(private var eventDataSource: EventDataSource): EventDataSource {
    override fun updateEventData(eventDataEntity: EventDataEntity): Long {
        //passes the call to  the event data source
        //repository can decide whether we want to call the api or save the data to DB
        // in this case, i have used a single EventDataSource that would be a LocalEventDataSource
        //we can also use EventRemoteDataSource when api call is needed
        return eventDataSource.updateEventData(eventDataEntity)
    }

}
