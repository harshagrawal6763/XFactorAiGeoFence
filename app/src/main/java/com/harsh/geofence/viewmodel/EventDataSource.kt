package com.harsh.geofence.viewmodel

import com.harsh.geofence.db.entity.EventDataEntity

interface EventDataSource {
    fun updateEventData(eventDataEntity: EventDataEntity)
}
