package com.harsh.geofence.db.dao
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.platform.app.InstrumentationRegistry
import com.harsh.geofence.consts.GeoConsts
import com.harsh.geofence.db.ApplicationDatabase
import com.harsh.geofence.db.entity.EventDataEntity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventDataDaoTest {
    private lateinit var database: ApplicationDatabase
    private lateinit var eventDataDao: EventDataDao
    private val eventData: EventDataEntity = EventDataEntity().apply {
        lat = GeoConsts.defaultLat
        longitude = GeoConsts.defaultLong
        time = System.currentTimeMillis()
        eventName = "Geo Fence Entered"
        eventTypeId = 1
    }

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before fun createDb() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, ApplicationDatabase::class.java).build()
        eventDataDao = database.getEventDataDao()
    }

    @After fun closeDb() {
        database.close()
    }

    @Test fun testGetEvents() = runBlocking {
        val eventDataEntity = EventDataEntity().apply {
            lat = GeoConsts.defaultLat
            longitude = GeoConsts.defaultLong
            time = System.currentTimeMillis()
            eventName = "Geo Fence Entered"
            eventTypeId = 1
        }


        eventDataDao.insertEventData(eventDataEntity)
        assertThat(eventDataDao.getEventData().size, equalTo(1))
    }

    @Test fun testDeleteEvent() = runBlocking {
        val eventDataEntity = EventDataEntity().apply {
            lat = GeoConsts.defaultLat
            longitude = GeoConsts.defaultLong
            time = System.currentTimeMillis()
            eventName = "Geo Fence Entered"
            eventTypeId = 1
        }

        eventDataDao.insertEventData(eventData)
        eventDataDao.insertEventData(eventDataEntity)

        assertThat(eventDataDao.getEventData().size, equalTo(2))
        eventDataDao.deleteEventData(eventDataEntity)
        assertThat(eventDataDao.getEventData().size, equalTo(1))
    }
}
