package com.harsh.geofence.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.harsh.geofence.R

class GeoFenceActivity : AppCompatActivity() {
    private var navController = lazy {
        findNavController(R.id.nav_host_container)
    }

    private fun getNavigationController(): NavController {
        return navController.value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geo_fence)
    }
}