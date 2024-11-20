package com.harsh.geofence.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.harsh.geofence.R

class GeoFenceActivity : AppCompatActivity() {

    //the navigation controller which hosts all the fragments
    private var navController = lazy {
        findNavController(R.id.nav_host_container)
    }

    //if we need instance if this navController
    fun getNavigationController(): NavController {
        return navController.value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geo_fence)
    }
}