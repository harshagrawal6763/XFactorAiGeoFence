package com.harsh.geofence.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.harsh.geofence.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class ServiceLocationFragmentTest {

    @get:Rule
    var intentsRule = IntentsRule()


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testButtonDisplayed() {
        // Check Button display correct values
        onView(withId(R.id.btnLocation)).check(matches(withText("Get Location")))
    }


}
