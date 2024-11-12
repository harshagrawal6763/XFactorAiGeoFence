package com.harsh.geofence.viewmodel

import com.harsh.geofence.db.entity.EventDataEntity

class EventDataRepository(var eventDataSource: EventDataSource): EventDataSource {
    override fun updateEventData(eventDataEntity: EventDataEntity) {
        eventDataSource.updateEventData(eventDataEntity)
    }

}
