package uk.ac.shef.oak.com4510.view.trip

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pl.aprilapps.easyphotopicker.*
import uk.ac.shef.oak.com4510.R
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.database.data.Trip
import uk.ac.shef.oak.com4510.database.relation.LocationWithImages
import uk.ac.shef.oak.com4510.database.relation.TripWithLocations
import uk.ac.shef.oak.com4510.sensor.SensorViewModel
import uk.ac.shef.oak.com4510.service.LocationService
import uk.ac.shef.oak.com4510.viewmodel.TripViewModel
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import uk.ac.shef.oak.com4510.database.data.Location
import uk.ac.shef.oak.com4510.utility.BitmapGenerator
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import uk.ac.shef.oak.com4510.utility.MapPlotter
import uk.ac.shef.oak.com4510.utility.PermissionChecker
import java.time.LocalDateTime


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var polyline1:Polyline? =null

    private lateinit var easyImage: EasyImage
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var tripViewModel: TripViewModel? = null
    private var sensorViewModel: SensorViewModel? = null

    private var locationList: MutableList<LocationWithImages> = ArrayList()
    private var pressureList: MutableList<Float> = ArrayList()
    private var temperatureList: MutableList<Float> = mutableListOf(20.0f)
    private var currentImages: MutableList<Image> = ArrayList()

    private var locationView: TextView? = null
    private var startTime: TextView? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorViewModel = ViewModelProvider(this@MapActivity)[SensorViewModel::class.java]
        tripViewModel = ViewModelProvider(this)[TripViewModel::class.java]
        sensorViewModel?.startSensing()

        // Get a new or existing ViewModel using the ViewModelProvider.
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // setup receiver to retrieve location data from service
        val filter = IntentFilter()
        filter.addAction("uk.ac.shef.oak.com4510.LOCATION")
        val receiver = LocationReceiver()
        registerReceiver(receiver, filter)

        val mButtonStart = findViewById<View>(R.id.button_start_trip) as Button
        val mButtonStop = findViewById<View>(R.id.button_stop_trip) as Button
        val mButtonSave = findViewById<View>(R.id.button_saveTrip) as Button
        val fabGallery: FloatingActionButton = findViewById(R.id.fab_gallery)

        locationView = findViewById<View>(R.id.locationview) as TextView
        startTime = findViewById<View>(R.id.starttime) as TextView
        locationView!!.text = "location: \nwait for start"
        startTime!!.text = "Trip started at \n:${LocalDateTime.now()}"

        // 1. start location service
        // 2. enable stop button
        mButtonStart.setOnClickListener {
            Intent(this, LocationService::class.java).also { intent ->
                startService(intent)
            }
            mButtonStart.isEnabled = false
            mButtonStop.isEnabled = true
            mButtonSave.isEnabled = false
        }

        // 1. stop location service
        // 2. enable save button
        // 3. enable start button
        mButtonStop.setOnClickListener {
            Intent(this, LocationService::class.java).also { intent ->
                stopService(intent)
            }
            sensorViewModel?.stopSensing()
            mButtonStart.isEnabled = true
            mButtonStop.isEnabled = false
            mButtonSave.isEnabled = true
        }
        mButtonStop.isEnabled = false

        // save trip and exit
        mButtonSave.setOnClickListener {
            endTrip()
        }
        mButtonSave.isEnabled = false

        initEasyImage()
        // the floating button that will allow us to get the images from the Gallery
        fabGallery.setOnClickListener(View.OnClickListener {
            easyImage.openChooser(this)
        })
        fabGallery.isEnabled = false

        PermissionChecker.getLocationPermission(this@MapActivity)
    }

    /**
     * functions to collect data and save to db
     * saveTrip - save trip into db along with temporary location list
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun endTrip() {
        // get trip title from user input
        val mTripTitle: EditText = findViewById(R.id.editText_trip_title)
        if (mTripTitle.text.toString() != "") {
            Intent(this, LocationService::class.java).also { intent ->
                stopService(intent)
            }
            polyline1?.remove()
            saveTrip(mTripTitle.text.toString())
            finish()
        }
        else {
            AlertDialog.Builder(this)
                .setTitle("warning").setMessage("a trip title is required")
                .setPositiveButton("ok", null).show()
        }
    }

    /**
     * save location data with image
     * @param location - add the location temporary location list
     * @return void
     */
    private fun saveLocation(location: Location) {
        // refresh image list buffer upon new location
        currentImages.clear()

        val locationWithImages = LocationWithImages(
            location=location,
            images= mutableListOf()
        )
        locationList.add(locationWithImages)
    }

    /**
     * save trip data to database
     * @param tripTitle with  location data and photo
     * @return void
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTrip(tripTitle: String) {
        val trip = Trip(
            tripTitle = tripTitle,
            tripStartTime = LocalDate.now().toString()
        )
        val tripLocations = TripWithLocations(
            trip = trip,
            locations = locationList
        )
        tripViewModel?.insertTrip(tripLocations)
    }

    /**
     * setup map
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
    }

    /**
     * handle photos returned activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        easyImage.handleActivityResult(requestCode, resultCode, data, this,
            object : DefaultCallback() {
                override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                    onPhotosReturned(imageFiles)
                }
            })
    }

    /**
     * setup camera
     */
    private fun initEasyImage() {
        easyImage = EasyImage.Builder(this)
            .setChooserType(ChooserType.CAMERA_AND_GALLERY)
            .allowMultiple(true)
            .build()
    }

    /**
     * @param returnedPhotos save the photo data to last recorded location
     * @return void
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun onPhotosReturned(returnedPhotos: Array<MediaFile>?=null) {
        scope.launch {
            val imageList: MutableList<Image> = ArrayList()
            if (returnedPhotos != null) {
                for (mediaFile in returnedPhotos) {
                    val fileNameAsTempTitle = mediaFile.file.name

                    val bitmap =
                        BitmapGenerator.decodeSampledBitmapFromResource(mediaFile.file.absolutePath, 150, 150)
                    val imageThumbnail =
                        BitmapGenerator.bitmapToFile(bitmap, this@MapActivity, mediaFile.file.name)

                    var image = Image(
                        imageTitle = fileNameAsTempTitle,
                        imageUri = mediaFile.file.absolutePath,
                        thumbnailUri = imageThumbnail
                    )
                    imageList.add(image)
                }

                // image list buffer to save all images to last recorded location
                locationList.last().images.addAll(imageList)
                val currentLoc = locationList.last().location
                val marker = mMap.addMarker(MarkerOptions().position(LatLng(currentLoc.locationLat, currentLoc.locationLong)))
                if (marker != null) {
                    marker.tag = 0
                }
            }
        }
    }

    /**
     * receive broadcast intent from service
     * intent contains lat long of current location
     */
    inner class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // receive location update from service
            val latitude = intent.getDoubleExtra("lat", 0.0)
            val longitude = intent.getDoubleExtra("long", 0.0)

            // receive sensor update
            sensorViewModel!!.retrievePressureData().observe(this@MapActivity,
                { newValue ->
                    newValue?.also{
                        pressureList.add(newValue)
                    }
                })
            sensorViewModel!!.retrieveTemperatureData().observe(this@MapActivity,
                { newValue ->
                    newValue?.also{
                        temperatureList.add(newValue)
                    }
                })

            // save location object
            val location = Location(
                locationLat = latitude,
                locationLong = longitude,
                locationTemp = temperatureList.last(),
                locationPressure = pressureList.last(),
            )
            saveLocation(location)

            // update route in map
            val current = LatLng(latitude, longitude)
            polyline1 = if (polyline1 != null) {
                MapPlotter.updateRoute(current, polyline1!!)
            } else {
                MapPlotter.startRoute(current, mMap)
            }

            // readjust map view to current location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 30.0f))
            locationView!!.text = "location: \n${current.latitude},${current.longitude}"
        }
    }

    /**
     * setup app permission
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionChecker.ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED
                )  {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }
}