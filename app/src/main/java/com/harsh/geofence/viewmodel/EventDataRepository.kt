package com.harsh.geofence.viewmodel

import com.harsh.geofence.db.entity.EventDataEntity

open class EventDataRepository(private var eventDataSource: EventDataSource): EventDataSource {
    override fun updateEventData(eventDataEntity: EventDataEntity): Long {
        return eventDataSource.updateEventData(eventDataEntity)
    }

}
