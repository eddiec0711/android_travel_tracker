package uk.ac.shef.oak.com4510.view.inspect

import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import uk.ac.shef.oak.com4510.R
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import uk.ac.shef.oak.com4510.database.data.Image
import uk.ac.shef.oak.com4510.database.data.Location
import uk.ac.shef.oak.com4510.view.main.MainActivity
import uk.ac.shef.oak.com4510.viewmodel.TripViewModel

class DisplayActivity : AppCompatActivity() {

    private var tripViewModel: TripViewModel? = null
    private var parentLocation: Location? = null
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val imageId = result.data?.getIntExtra("imageId", -1)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        val bundle: Bundle? = intent.extras
        this.tripViewModel = ViewModelProvider(this)[TripViewModel::class.java]

        if (bundle != null) {
            val image = tripViewModel!!.getImage(bundle.getInt("imageId"))
            image!!.observe(this,
                { imageData ->
                    displayData(imageData)
                    addButtonListeners(imageData)
                }
            )
        }

        // add back button
        val toolbar = findViewById<View>(R.id.toolbar_display) as Toolbar
        toolbar.setNavigationOnClickListener {
            val myIntent = Intent(this, MainActivity::class.java)
            this.startActivity(myIntent)
        }
    }

    /**
     * display the image and associated location details
     *
     * @param image - image object to be displayed
     * @return void
     */
    private fun displayData(image: Image) {
        // access view objects for display
        val imageView = findViewById<ImageView>(R.id.image_display)
        val titleToolbar = findViewById<Toolbar>(R.id.toolbar_display)
        val descriptionTextView = findViewById<TextView>(R.id.text_description)
        val pressureTextView = findViewById<TextView>(R.id.text_pres)
        val temperatureTextView = findViewById<TextView>(R.id.text_temp)

        // get associated location data
        image.locationId?.let {
            tripViewModel?.getLocation(it)?.observe (this,
                { location ->
                    pressureTextView.text = location.locationPressure.toString()
                    temperatureTextView.text = location.locationTemp.toString()

                    parentLocation = location
                }
            )
        }

        // retrieve image data and display
        image.let {
            val bitmap = BitmapFactory.decodeFile(image.thumbnailUri)
            imageView.setImageBitmap(bitmap)

            titleToolbar.title = image.imageTitle
            descriptionTextView.text = image.imageDescription
        }


        // enable full screen on tap
        val fullScreenInd = intent.getStringExtra("fullScreenIndicator")
        if ("y" == fullScreenInd) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            supportActionBar!!.hide()
            imageView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            imageView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            imageView.adjustViewBounds = false
            imageView.scaleType = ImageView.ScaleType.FIT_XY
        }

        imageView.setOnClickListener {
            val intent = Intent(this, DisplayActivity::class.java)
            if ("y" == fullScreenInd) {
                intent.putExtra("fullScreenIndicator", "")
                intent.putExtra("imageId", image.imageId)
            } else {
                intent.putExtra("fullScreenIndicator", "y")
                intent.putExtra("imageId", image.imageId)
            }
            this.startActivity(intent)
        }
    }

    /**
     * add click listeners to all button with following actions
     * 1. edit - enter edit image interface to edit current image
     * 2. map - view image on map with associated path
     *
     * @param image - image to be bind with actions
     * @return void
     */
    private fun addButtonListeners(image: Image) {
        // initiate edit activity
        val fabEdit: FloatingActionButton = findViewById(R.id.fab_edit)
        fabEdit.setOnClickListener(View.OnClickListener {
            startForResult.launch(
                Intent( this, EditActivity::class.java).apply {
                    putExtra("imageId", image.imageId)
                }
            )
        })

        // initiate map browse activity
        val btnMap: Button = findViewById(R.id.button_map_browse)
        btnMap.setOnClickListener(View.OnClickListener {
            startForResult.launch(
                Intent( this, MapBrowseActivity::class.java).apply {
                    putExtra("locationId", parentLocation?.locationId)
                    putExtra("tripId", parentLocation?.tripId)
                }
            )
        })
    }

}