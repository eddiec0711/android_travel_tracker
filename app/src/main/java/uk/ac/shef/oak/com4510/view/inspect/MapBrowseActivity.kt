package uk.ac.shef.oak.com4510.view.inspect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.*
import uk.ac.shef.oak.com4510.R
import uk.ac.shef.oak.com4510.viewmodel.TripViewModel
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.maps.model.*
import uk.ac.shef.oak.com4510.database.relation.TripWithLocations
import uk.ac.shef.oak.com4510.utility.MapPlotter
import uk.ac.shef.oak.com4510.view.main.MainActivity


class MapBrowseActivity : AppCompatActivity(),
    GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var polyline1:Polyline? = null
    private var tripViewModel: TripViewModel? = null
    private var tripId: Int? = null
    private var locationId: Int? = null
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val imageId = result.data?.getIntExtra("imageId", -1)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_browse)
        this.tripViewModel = ViewModelProvider(this)[TripViewModel::class.java]

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            tripId = bundle.getInt("tripId")
            locationId = bundle.getInt("locationId")
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_browse) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // back to home page
        val btnEnd: Button = findViewById(R.id.button_end_browse)
        btnEnd.setOnClickListener(View.OnClickListener {
            val myIntent = Intent(this, MainActivity::class.java)
            this.startActivity(myIntent)
        })
    }

    /**
     * populate map with image data upon ready
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        // populate map with image marker
        if (tripId != null){
            this.tripViewModel!!.getTrip(tripId!!)!!.observe(this,
                { trip ->
                    renderMarker(trip)
                }
            )
        }
        else {
            this.tripViewModel!!.getTripsWithLocations()!!.observe(this,
                { trips ->
                    for (tripWithLocations in trips) {
                        renderMarker(tripWithLocations)
                    }
                }
            )
        }
        mMap.setOnMarkerClickListener(this)
    }

    /**
     * Called when the user clicks a marker
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        startForResult.launch(
            Intent( this, DisplayActivity::class.java).apply {
                putExtra("imageId", marker.tag.toString().toInt())
            }
        )
        return true
    }

    /**
     * call when a specific image is requested to be display
     * the whole associated trip will be displayed
     * with the target image in red and the rest in blue
     *
     * @param trip - trip that target image belongs to
     * @return void
     */
    private fun renderMarker(trip: TripWithLocations) {
        for (locationWithImages in trip.locations) {
            if (! locationWithImages.images.isNullOrEmpty()) {
                // retrieve location data
                val locationData = locationWithImages.location
                val location = LatLng(locationData.locationLat, locationData.locationLong)

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 30.0f))

                // set marker to blue for general marker and red for target marker
                val color = if (locationId != null && locationId == locationData.locationId) {
                    // add marker with bind image data
                    BitmapDescriptorFactory.HUE_RED
                } else {
                    BitmapDescriptorFactory.HUE_AZURE
                }

                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .icon(BitmapDescriptorFactory.defaultMarker(color)))
                marker?.tag = locationWithImages.images[0].imageId

                // plot trip journey if requested from display activity
                if (tripId != null) {
                    // update route
                    polyline1 = if (polyline1 != null) {
                        MapPlotter.updateRoute(location, polyline1!!)
                    } else {
                        MapPlotter.startRoute(location, mMap)
                    }
                }
            }
        }
    }
}