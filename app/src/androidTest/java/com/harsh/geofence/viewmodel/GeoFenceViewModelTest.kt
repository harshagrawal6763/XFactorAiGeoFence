package com.harsh.geofence.viewmodel

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.location.Geofence

import com.harsh.geofence.db.entity.EventDataEntity
import com.harsh.geofence.model.GeoEvent

import com.harsh.geofence.consts.GeoConsts
import com.harsh.geofence.db.DbManager
import org.junit.Before

import org.junit.Test
import org.mockito.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Rule


class GeoFenceViewModelTest {
    @Mock
    lateinit var mockRepository: EventDataRepository

    @Mock
    lateinit var mockObserverGeoEvents: Observer<EventDataEntity>

    @Mock
    lateinit var mockObserverError: Observer<String>

    @Mock
    lateinit var mockObserverLocation: Observer<Location>

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: GeoFenceViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        DbManager.initialize(context)
        Dispatchers.setMain(Dispatchers.Unconfined)
        viewModel = GeoFenceViewModel().apply {
            eventRepository = mockRepository // Inject mocked repository
        }

        viewModel.geoEvents.observeForever(mockObserverGeoEvents)
        viewModel.error.observeForever(mockObserverError)
        viewModel.location.observeForever(mockObserverLocation)
    }

    @Test
    fun testErrorInvoke() {
        val errorMessage = "Test Error"
        viewModel.sendError(errorMessage)

        // Verify the LiveData observer gets the updated error value
        Mockito.verify(mockObserverError).onChanged(errorMessage)
    }

    @Test
    fun checkEventTrigger() = runBlocking {
        // Prepare mock GeoEvent
        val geoEvent = GeoEvent(
            Geofence.GEOFENCE_TRANSITION_ENTER,
            GeoConsts.defaultLat,
            GeoConsts.defaultLong,
            System.currentTimeMillis()
        )
        val expectedEvent = EventDataEntity().apply {
            eventTypeId = geoEvent.eventType
            lat = geoEvent.latitude
            longitude = geoEvent.long
            time = geoEvent.time
            eventName = "GeoFence Entered"
        }

        viewModel.geoEvents.value = expectedEvent
        // Verify LiveData is updated with the new event
        Mockito.verify(mockObserverGeoEvents).onChanged(expectedEvent)
    }

    @Test
    fun testLocationUpdate() {
        val location = Location("provider").apply {
            latitude = 12.345
            longitude = 67.890
        }
        viewModel.updateLocation(location)
        // Verify LiveData is updated with the new location
        Mockito.verify(mockObserverLocation).onChanged(location)
    }

    @After
    fun tearDown() {
        DbManager.close()
    }
}
