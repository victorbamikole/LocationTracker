package com.example.mapinfodisplayapp

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


/**This class extends the GoogleMap.InfoWindowAdapter
 * which is used to define custom info for the map marker*/
class InforWindowAdapter(context:Activity): GoogleMap.InfoWindowAdapter {


    //Here we inflate the map info display window with the custom layout
    private val contents: View = context.layoutInflater.inflate(
        R.layout.markerdetails_layout, null)
    override fun getInfoWindow(p0: Marker): View? {
        return null
    }


    //This function set the marker's details to our text view in the custom layout
    override fun getInfoContents(p0: Marker): View? {
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = p0.title ?: ""

        val phoneView = contents.findViewById<TextView>(R.id.details)
        phoneView.text = p0.snippet ?: ""

        return contents
    }
}