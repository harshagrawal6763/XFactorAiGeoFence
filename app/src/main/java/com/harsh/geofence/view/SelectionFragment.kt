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

    var btnGoogleMapsApproach:AppCompatButton?=null
    var btnServiceApproach:AppCompatButton?=null

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

        setListeners()
    }

    private fun setListeners() {
        btnGoogleMapsApproach?.setOnClickListener {
            findNavController().navigate(R.id.action_global_landingFragment)
        }

        btnServiceApproach?.setOnClickListener {
            findNavController().navigate(R.id.action_global_serviceLocationFragment)
        }
    }

}