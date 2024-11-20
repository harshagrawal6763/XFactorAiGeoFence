package com.harsh.geofence.viewmodel

import com.harsh.geofence.db.DbManager
import com.harsh.geofence.db.entity.EventDataEntity

class EventLocalDataSource: EventDataSource {
    override fun updateEventData(eventDataEntity: EventDataEntity): Long {
        //saves the data in Db and returns the db update id
        //return statement is not needed as such
        //but this is to denote that anything can be returned form this data source that can be consumed by repository and
        //passed on to view model
        return DbManager.getEventDataDao().insertEventData(eventDataEntity)
    }

}
