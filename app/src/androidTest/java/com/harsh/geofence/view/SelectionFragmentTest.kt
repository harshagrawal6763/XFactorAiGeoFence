package com.harsh.geofence.view
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.harsh.geofence.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelectionFragmentTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val activityScenario = ActivityScenario.launch(GeoFenceActivity::class.java)

        activityScenario.onActivity { activity ->
            activity.runOnUiThread {
                navController.setGraph(R.navigation.nav_geo_fence)  // Set your navigation graph here
            }
        }

    }

    @Test
    fun testSelectionFragment_UIElementsDisplayed() {

        launchFragmentInContainer<SelectionFragment>().onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        // Check if UI buttons are displayed
        onView(withId(R.id.btnGoogleMapsApproach)).check(matches(isDisplayed()))
        onView(withId(R.id.btnServiceApproach)).check(matches(isDisplayed()))
    }

    @Test
    fun testGoogleMapsApproachButton_NavigatesToLandingFragment() {

        launchFragmentInContainer<SelectionFragment>().onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Perform button click and check if navigation goes to LandingFragment
        onView(withId(R.id.btnGoogleMapsApproach)).perform(click())
        assert(navController.currentDestination?.id == R.id.landingFragment)
    }

    @Test
    fun testServiceApproachButton_NavigatesToServiceLocationFragment() {

        launchFragmentInContainer<SelectionFragment>().onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        // Perform button click and check if navigation goes to ServiceLocationFragment
        onView(withId(R.id.btnServiceApproach)).perform(click())
        assert(navController.currentDestination?.id == R.id.serviceLocationFragment)
    }
}
