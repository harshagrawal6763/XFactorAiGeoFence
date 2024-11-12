package com.harsh.geofence.viewmodel

import com.harsh.geofence.db.DbManager
import com.harsh.geofence.db.entity.EventDataEntity

class EventLocalDataSource: EventDataSource {
    override fun updateEventData(eventDataEntity: EventDataEntity) {
        DbManager.getEventDataDao().insertEventData(eventDataEntity)
    }

}
