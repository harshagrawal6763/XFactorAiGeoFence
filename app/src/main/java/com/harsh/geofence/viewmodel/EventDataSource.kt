package com.harsh.geofence.viewmodel

import com.harsh.geofence.db.entity.EventDataEntity

/*here
* all the actions that need to performed either on database or API call
* this interface just provides the blueprint that the datasources and repository must impelment
* */
interface EventDataSource {
    fun updateEventData(eventDataEntity: EventDataEntity):Long
}
