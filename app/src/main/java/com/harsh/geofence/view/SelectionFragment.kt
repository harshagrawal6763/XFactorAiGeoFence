package com.harsh.geofence.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.harsh.geofence.R



class SelectionFragment : Fragment() {

    private var btnGoogleMapsApproach:AppCompatButton?=null
    private var btnServiceApproach:AppCompatButton?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selection, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init views
        btnGoogleMapsApproach = view.findViewById(R.id.btnGoogleMapsApproach)
        btnServiceApproach = view.findViewById(R.id.btnServiceApproach)

        //set the click listeners
        setListeners()
    }

    private fun setListeners() {
        //if user clicks on btnGoogleMapsApproach it will navigate to GoogleMapsFragment
        //this approach takes help of Google Maps to show the geofence as well as for location updates
        btnGoogleMapsApproach?.setOnClickListener {
            findNavController().navigate(R.id.action_global_landingFragment)
        }

        //if user clicks on btnServiceApproach it will navigate to ServiceLocationFragment
        //this approach takes help of LocationService to set the geofence and for location updates
        btnServiceApproach?.setOnClickListener {
            findNavController().navigate(R.id.action_global_serviceLocationFragment)
        }
    }

}