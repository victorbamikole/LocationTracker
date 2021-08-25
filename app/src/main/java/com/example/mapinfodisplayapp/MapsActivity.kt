package com.example.mapinfodisplayapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapinfodisplayapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    lateinit var databaseRef: DatabaseReference
    private lateinit var binding: ActivityMapsBinding
    private val LOCATION_PERMISSION_REQUEST = 1

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    /**This function checks if the user has granted pour app the permission to access location, if not,
    /the user is prompted to grant our app permission to access their location*/
    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdates()
        } else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
    }


    //This function request permission to access the user location
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    return
                }
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "User has not granted location access permission",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //We initialize the FireBase
        FirebaseApp.initializeApp(this)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        databaseRef = Firebase.database.reference
        databaseRef.addValueEventListener(logListener)

    }


    val logListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(applicationContext, "Could not read from database", Toast.LENGTH_LONG)
                .show()
        }

        //This function tracks my partner location by using the data read from FireBase
        //     @SuppressLint("LongLogTag")
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {

                val locationlogging = dataSnapshot.child("EmmanuelLocation").getValue(LocationLogging::class.java)
                var partnerLat = locationlogging?.latitude
                var partnerLong = locationlogging?.longitude

                if (partnerLat != null && partnerLong != null) {
                    val partnerLoc = LatLng(partnerLat, partnerLong)

                    val markerOptions =
                        MarkerOptions().position(partnerLoc).title("Emannuel").snippet(
                            "Emmanuel's current location details:\nLatitude: ${partnerLoc.latitude}" +
                                    "\nLongitude: ${partnerLoc.longitude}"
                        )
                    map.clear()
                    map.addMarker(
                        markerOptions.icon(
                            bitmapDescriptorFromVector(
                                this@MapsActivity,
                                R.drawable.ic_launcher_partner
                            )
                        )
                    )


                    //The custom window layout is set to the map that shows the appearance of the marker content
                    map.setInfoWindowAdapter(InforWindowAdapter(this@MapsActivity))
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(partnerLoc, 16f))
                    //Zoom level - 1: World, 5: Landmass/continent, 10: City, 15: Streets and 20: Buildings

                    Toast.makeText(
                        applicationContext,
                        "Locations accessed from the database",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    //This function reads the current user location in realtime every 30 secs and saves the location data to firebase
    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 20000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation

                    //An object of firebase is declared here
                    lateinit var databaseRef: DatabaseReference
                    //Our object of firebase is initialized here
                    databaseRef = Firebase.database.reference
                    val locationlogging = LatLng(location.latitude, location.longitude)
                    databaseRef.child("VictorLocation").setValue(locationlogging)
                        .addOnSuccessListener {
                            Toast.makeText(
                                applicationContext,
                                "Locations written into the database",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                applicationContext,
                                "Error occurred while writing the locations",
                                Toast.LENGTH_LONG
                            ).show()
                        }


                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        val markerOptions = MarkerOptions().position(latLng)
                        val markerOptions2 =
                            MarkerOptions().position(latLng).title("Victor").snippet(
                                "Victor's current location details:\nLatitude: ${latLng.latitude}" +
                                        "\nLongitude: ${latLng.longitude}"
                            )
                        map.addMarker(markerOptions2)
                    }
                }
            }
        }
    }

    //This function get location updates from the user by initializing the FusedLocationClient
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(InforWindowAdapter(this))
        getLocationAccess()
    }


    //This function customizes our default map marker
    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}
