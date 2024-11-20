package com.harsh.geofence.reciever

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.harsh.geofence.model.GeoEvent
import com.harsh.geofence.viewmodel.GeoFenceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class GeofenceBroadcastReceiverTest {

    private lateinit var context: Context
    private lateinit var mockViewModel: GeoFenceViewModel
    private val mockWrapper: GeofencingEventWrapper = mock(GeofencingEventWrapper::class.java)
    private val receiver: GeofenceBroadcastReceiver = mock(GeofenceBroadcastReceiver::class.java)
    private val geofencingEvent: GeofencingEvent = mock(GeofencingEvent::class.java)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Use ApplicationProvider to provide a context for tests
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(Dispatchers.Unconfined)
        context = ApplicationProvider.getApplicationContext()
        receiver.geofencingEventWrapper = mockWrapper
        mockViewModel = mock(GeoFenceViewModel::class.java)
        mockViewModel.error = MutableLiveData()
        receiver.geoFenceViewModel = mockViewModel
    }

    @Test
    fun testReceiveError() {
        // Create an intent with a GeofencingEvent that has an error
        val intent = Intent()
        `when`(mockWrapper.fromIntent(intent)).thenReturn(geofencingEvent)
        `when`(geofencingEvent.hasError()).thenReturn(true)
        receiver.onReceive(context, intent)
        verify(mockViewModel).sendError("Error receiving geofence event1...")
    }

    @Test
    fun testValidGeoFenceEnter() {
        // Create an intent with a valid GeofencingEvent
        val intent = Intent()

        val geofencingEvent = mock(GeofencingEvent::class.java)
        val mockGeofence = mock(Geofence::class.java)
        val mockLocation = mock(Location::class.java)
        `when`(mockWrapper.fromIntent(intent)).thenReturn(geofencingEvent)

        `when`(geofencingEvent.hasError()).thenReturn(false)
        `when`(geofencingEvent.triggeringGeofences).thenReturn(listOf(mockGeofence))
        `when`(geofencingEvent.triggeringLocation).thenReturn(mockLocation)
        `when`(geofencingEvent.geofenceTransition).thenReturn(Geofence.GEOFENCE_TRANSITION_ENTER)

        // Simulate latitude and longitude for the location
        `when`(mockLocation.latitude).thenReturn(37.4219983)
        `when`(mockLocation.longitude).thenReturn(-122.084)

        // Simulate onReceive call
        receiver.onReceive(context, intent)

        // Verify that triggerEvent was called in the ViewModel with expected GeoEvent values
        verify(mockViewModel).triggerEvent(
            GeoEvent(
                Geofence.GEOFENCE_TRANSITION_ENTER,
                37.4219983,
                -122.084,
                anyLong()
            )
        )
    }

    @Test
    fun testValidGeoFenceExit() {
        // Create an intent with a valid GeofencingEvent without location
        val intent = Intent()

        val geofencingEvent = mock(GeofencingEvent::class.java)
        val mockGeofence = mock(Geofence::class.java)
        val mockLocation = mock(Location::class.java)
        `when`(mockWrapper.fromIntent(intent)).thenReturn(geofencingEvent)

        `when`(geofencingEvent.hasError()).thenReturn(false)
        `when`(geofencingEvent.triggeringGeofences).thenReturn(listOf(mockGeofence))
        `when`(geofencingEvent.triggeringLocation).thenReturn(mockLocation)
        `when`(geofencingEvent.geofenceTransition).thenReturn(Geofence.GEOFENCE_TRANSITION_EXIT)

        // Simulate latitude and longitude for the location
        `when`(mockLocation.latitude).thenReturn(37.4219983)
        `when`(mockLocation.longitude).thenReturn(-122.084)

        // Simulate onReceive call
        receiver.onReceive(context, intent)

        // Verify that triggerEvent was called in the ViewModel with expected GeoEvent values
        verify(mockViewModel).triggerEvent(
            GeoEvent(
                Geofence.GEOFENCE_TRANSITION_EXIT,
                37.4219983,
                -122.084,
                anyLong()
            )
        )

    }
}
