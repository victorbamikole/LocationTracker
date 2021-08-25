package com.example.mapinfodisplayapp

import android.app.Activity
import android.view.View
import android.widget.TextView

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class InoWindowAdapter2(context: Activity): GoogleMap.InfoWindowAdapter {

    private val contents: View = context.layoutInflater.inflate(
        R.layout.marker2_details, null)
    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

    override fun getInfoContents(p0: Marker): View? {
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = p0.title ?: ""

        val phoneView = contents.findViewById<TextView>(R.id.details)
        phoneView.text = p0.snippet ?: ""

        return contents
    }
}